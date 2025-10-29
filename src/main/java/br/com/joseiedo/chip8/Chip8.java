package br.com.joseiedo.chip8;

import static br.com.joseiedo.chip8.Font.FONTSET;
import static java.lang.System.arraycopy;
import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Paths.get;
import static java.util.Arrays.fill;

import java.io.IOException;
import java.util.Objects;
import java.util.Random;

public class Chip8 {

    private Random random = new Random();
    private Display display;
    private Keypad keypad;
    private int opcode;

    private byte[] memory = new byte[4096];
    private int[] V = new int[16];

    private int I;

    // We could make this as a short
    // But it would require me to mask everytime...
    // private int pc = 512; // 0x200

    private ProgramCounter programCounter;

    // We could make this stack as a short[]
    // But let's make things simple for now...
    private int[] stack = new int[16];

    private int sp;

    private Timers timer = new Timers();

    private static final int FONT_SET_START = 0x50;
    private static final int PROGRAM_START = 0x200;

    public Chip8(Display display, Keypad keypad) {
        this.display = display;
        this.keypad = keypad;
        this.programCounter = new ProgramCounter(PROGRAM_START);
    }

    public void initialize(byte[] program) {
        Objects.requireNonNull(display, "Field \"display\" is null");
        Objects.requireNonNull(programCounter, "Field \"programCounter\" is null");

        programCounter.jump(PROGRAM_START);
        opcode = 0;
        I = 0;
        sp = 0;

        fill(stack, 0);
        fill(V, (byte) 0);
        fill(memory, (byte) 0);

        arraycopy(FONTSET, 0, memory, FONT_SET_START, FONTSET.length);
        arraycopy(program, 0, memory, PROGRAM_START, program.length);

        timer.initialize();
    }

    private void fetchOpCode() {
        int pc = programCounter.getCurrent();
        opcode = ((memory[pc] & 0xFF) << 8) | (memory[pc + 1] & 0xFF);
        programCounter.next();
    }

    public void emulateCycle() {
        for (int i = 0; i < 10; i++) { // Trick to have more cycles in a single frame hehe
            fetchOpCode();
            decodeAndExecute();
            display.render();
        }
        timer.cycle();
    }

    private void decodeAndExecute() {
        int X = (opcode & 0x0F00) >> 8;
        int Y = (opcode & 0x00F0) >> 4;
        int F = 0xF;
        int N = opcode & F;
        int NN = opcode & 0x00FF;
        int NNN = opcode & 0x0FFF;

        switch (opcode & 0xF000) {
            case 0x0000:
                switch (N) {
                    case 0x0: // 0x00E0: clear screen
                        display.clearScreen();
                        break;
                    case 0xE: // 0x00EE: return from subroutine
                        programCounter.jump(stack[--sp]);
                        break;
                    default:
                        System.out.printf("Unknown 0x0000 opcode: 0x%04X\n", opcode);
                }
                break;

            case 0x1000: programCounter.jump(NNN); break;
            case 0x2000:
                stack[sp++] = programCounter.getCurrent();
                programCounter.jump(NNN);
                break;
            case 0x3000: if (V[X] == NN) programCounter.next(); break;
            case 0x4000: if (V[X] != NN) programCounter.next(); break;
            case 0x5000: if (V[X] == V[Y]) programCounter.next(); break;
            case 0x9000: if (V[X] != V[Y]) programCounter.next(); break;
            case 0x6000: V[X] = NN & 0xFF; break;
            case 0x7000: V[X] = (V[X] + NN) & 0xFF; break;

            case 0x8000:
                switch (N) {
                    case 0x0: V[X] = V[Y]; break;
                    case 0x1: V[X] |= V[Y]; break;
                    case 0x2: V[X] &= V[Y]; break;
                    case 0x3: V[X] ^= V[Y]; break;
                    case 0x4:
                        int sum = V[X] + V[Y];
                        V[X] = sum & 0xFF;
                        V[F] = sum > 0xFF ? 1 : 0;
                        break;
                    case 0x5:
                        V[F] = V[X] >= V[Y] ? 1 : 0;
                        V[X] = (V[X] - V[Y]) & 0xFF;
                        break;
                    case 0x6:
                        V[F] = V[Y] & 0x1;
                        V[X] = (V[Y] >> 1) & 0xFF;
                        break;
                    case 0x7:
                        V[F] = V[Y] >= V[X] ? 1 : 0;
                        V[X] = (V[Y] - V[X]) & 0xFF;
                        break;
                    case 0xE:
                        V[F] = (V[Y] & 0x80) >> 7;
                        V[X] = (V[Y] << 1) & 0xFF;
                        break;
                    default:
                        System.out.printf("Unknown 0x8000 opcode: 0x%04X\n", opcode);
                }
                break;

            case 0xA000: I = NNN; break;
            case 0xB000: programCounter.jump(NNN + V[0]); break;
            case 0xC000: V[X] = random.nextInt() & NN; break;

            case 0xD000:
                V[F] = 0;
                for (int row = 0; row < N; row++) {
                    int spriteByte = memory[I + row];
                    for (int col = 0; col < 8; col++) {
                        int spritePixel = (spriteByte >> (7 - col)) & 1;
                        int xPos = (V[X] + col) % Display.WIDTH;
                        int yPos = (V[Y] + row) % Display.HEIGHT;
                        if (spritePixel == 1 && display.drawPixel(xPos, yPos)) {
                            V[F] = 1;
                        }
                    }
                }
                break;

            case 0xE000:
                switch (N) {
                    case 0xE: if (keypad.isKeyPressed(V[X])) programCounter.next(); break;
                    case 0x1: if (!keypad.isKeyPressed(V[X])) programCounter.next(); break;
                    default: System.out.printf("Unknown 0xE000 opcode: 0x%04X\n", opcode);
                }
                break;

            case 0xF000:
                switch (NN) {
                    case 0x07: V[X] = timer.getDelayTimer() & 0xFF; break;
                    case 0x15: timer.setDelayTimer(V[X]); break;
                    case 0x18: timer.setSoundTimer(V[X]); break;
                    case 0x1E: V[F] = (I + V[X] > 0xFFF) ? 1 : 0; I = (I + V[X]) & 0xFFFF; break;
                    case 0x0A:
                        keypad.getLastPressedKey().ifPresentOrElse(
                                keyCode -> V[X] = keyCode,
                                programCounter::back
                        );
                        break;
                    case 0x29: I = FONT_SET_START + V[X] * 5; break;
                    case 0x55:
                        for (int i = 0; i <= X; i++) memory[I + i] = (byte) (V[i] & 0xFF);
                        break;
                    case 0x65:
                        for (int i = 0; i <= X; i++) V[i] = memory[I + i] & 0xFF;
                        break;
                    case 0x33:
                        int number = V[X];
                        int[] digits = extractDigits(number);
                        for (int i = 0; i < digits.length; i++) memory[I + i] = (byte) digits[i];
                        break;
                    default:
                        System.out.printf("Unknown 0xF000 opcode: 0x%04X\n", opcode);
                }
                break;

            default:
                System.out.printf("Unknown opcode: 0x%04X\n", opcode);
        }
    }

    public static int[] extractDigits(int number) {
        int remaining = number;
        int[] digits = new int[3];
        int position = 2;

        while (remaining > 0 && position >= 0) {
            digits[position--] = remaining % 10;
            remaining /= 10;
        }

        return digits;
    }

    public void init() throws IOException {
        byte[] program = readAllBytes(get("roms/6-keypad.ch8"));
        //byte[] program = readAllBytes(get("roms/4-flags.ch8"));
        //byte[] program = readAllBytes(get("roms/3-corax+.ch8"));
        //byte[] program = readAllBytes(get("roms/2-ibm-logo.ch8"));
        //byte[] program = readAllBytes(get("roms/1-chip8-logo.ch8"));
        initialize(program);
    }
}
