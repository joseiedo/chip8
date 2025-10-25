package br.com.joseiedo.chip8;

import java.io.IOException;

import static br.com.joseiedo.chip8.Font.FONTSET;
import static java.lang.System.arraycopy;
import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Paths.get;
import static java.util.Arrays.fill;

public class Chip8 {

    public Chip8(Display display) {
        this.display = display;
    }

    private Display display;
    private int opcode;

    private byte[] memory = new byte[4096];
    private int[] V = new int[16];

    private int I;

    // We could make this as a short
    // But it would require me to mask everytime...
    private int pc = 512; // 0x200

    // We could make this stack as a short[]
    // But let's make things simple for now...
    private int[] stack = new int[16];

    private int sp;

    private Timers timer = new Timers();

    private static final int FONT_SET_START = 0x50;
    private static final int PROGRAM_START = 0x200;

    public void initialize(byte[] program) {
        pc = 0x200;
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
        opcode = ((memory[pc] & 0xFF) << 8) | (memory[pc + 1] & 0xFF);
        pc += 2;
    }

    public void emulateCycle() {
        fetchOpCode();
        decodeAndExecute();
        timer.cycle();
        display.render();
    }

    private void decodeAndExecute() {
        int X = (opcode & 0x0F00) >> 8;
        int Y = (opcode & 0x00F0) >> 4;
        int N = opcode & 0x000F;
        int NN = opcode & 0x00FF;
        int NNN = opcode & 0x0FFF;

        switch (opcode & 0xF000) {
            case 0x0000:
                switch (N) {
                    case 0x0: // 0x00E0: clear screen
                        display.clearScreen();
                        break;
                    case 0xE: // 0x00EE: return from subroutine
                        pc = stack[--sp];
                        break;
                }
                break;
            case 0x1000: // 1NNN: jump
                pc = NNN;
                break;
            case 0x2000: // 2NNN: call subroutine
                stack[sp++] = pc;
                pc = NNN;
                break;
            case 0x6000: // 6XNN: Set
                V[X] = NN & 0xFF;
                break;
            case 0x7000: // 7XNN: Add
                V[X] = (V[X] + NN) & 0xFF;
                break;
            case 0xA000: // ANNN: Set index
                I = NNN;
                break;
            case 0xD000: // DXYN: Display
                V[0xF] = 0;  // reset collision flag
                for (int row = 0; row < N; row++) {
                    int spriteByte = memory[I + row];
                    for (int col = 0; col < 8; col++) {
                        int spritePixel = (spriteByte >> (7 - col)) & 1; // extract each bit
                        int xPos = (V[X] + col) % Display.WIDTH;
                        int yPos = (V[Y] + row) % Display.HEIGHT;
                        if (spritePixel == 1) {
                            if (display.drawPixel(xPos, yPos)) {
                                V[0xF] = 1; // collision
                            }
                        }
                    }
                }
                break;
            default:
                System.out.printf("Unknown opcode: 0x%04X\n", opcode);
        }
    }

    public void init() throws IOException {
        byte[] program = readAllBytes(get("roms/2-ibm-logo.ch8"));
        //byte[] program = readAllBytes(get("roms/1-chip8-logo.ch8"));
        initialize(program);
    }
}
