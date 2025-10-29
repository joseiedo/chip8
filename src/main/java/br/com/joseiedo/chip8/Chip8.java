package br.com.joseiedo.chip8;

import static java.lang.System.arraycopy;
import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Paths.get;
import static java.util.Arrays.fill;

import java.io.IOException;
import java.util.Objects;
import java.util.Random;

public class Chip8 {

    private final Random random = new Random();
    private final Display display;
    private final Keypad keypad;
    private final ProgramCounter programCounter;
    private final Timers timer = new Timers();

    private static final int MEMORY_SIZE = 4096;
    private static final int REGISTER_COUNT = 16;
    private static final int STACK_SIZE = 16;
    private static final int FONT_SET_START = 0x50;
    private static final int PROGRAM_START = 0x200;

    private final byte[] memory = new byte[MEMORY_SIZE];
    private final int[] V = new int[REGISTER_COUNT];
    private final int[] stack = new int[STACK_SIZE];
    private int sp;
    private int I;
    private int opcode;

    // Decoding helpers
    private int X, Y, F, N, NN, NNN;

    public Chip8(Display display, Keypad keypad) {
        this.display = display;
        this.keypad = keypad;
        this.programCounter = new ProgramCounter(PROGRAM_START);
    }

    public void initialize(byte[] program) {
        Objects.requireNonNull(display);
        Objects.requireNonNull(programCounter);

        programCounter.jump(PROGRAM_START);
        opcode = 0;
        I = 0;
        sp = 0;

        fill(stack, 0);
        fill(V, 0);
        fill(memory, (byte) 0);

        arraycopy(Font.FONTSET, 0, memory, FONT_SET_START, Font.FONTSET.length);
        arraycopy(program, 0, memory, PROGRAM_START, program.length);

        timer.initialize();
    }

    /* ---------------- Execution Loop ---------------- */

    public void emulateCycle() {
        for (int i = 0; i < 10; i++) {
            fetchOpCode();
            decodeAndExecute();
            display.render();
        }
        timer.cycle();
    }

    private void fetchOpCode() {
        int pc = programCounter.getCurrent();
        opcode = ((memory[pc] & 0xFF) << 8) | (memory[pc + 1] & 0xFF);
        programCounter.next();
    }

    /* ---------------- Decoding ---------------- */

    private void decodeAndExecute() {
        decodeOpcode();
        executeOpcode();
    }

    private void decodeOpcode() {
        X = (opcode & 0x0F00) >> 8;
        Y = (opcode & 0x00F0) >> 4;
        F = 0xF;
        N = opcode & F;
        NN = opcode & 0x00FF;
        NNN = opcode & 0x0FFF;
    }

    private void executeOpcode() {
        switch (opcode & 0xF000) {
            case 0x0000 -> handle0x0000();
            case 0x1000 -> programCounter.jump(NNN);
            case 0x2000 -> callSubroutine();
            case 0x3000 -> { if (V[X] == NN) programCounter.next(); }
            case 0x4000 -> { if (V[X] != NN) programCounter.next(); }
            case 0x5000 -> { if (V[X] == V[Y]) programCounter.next(); }
            case 0x6000 -> V[X] = NN;
            case 0x7000 -> V[X] = (V[X] + NN) & 0xFF;
            case 0x8000 -> handle0x8000();
            case 0x9000 -> { if (V[X] != V[Y]) programCounter.next(); }
            case 0xA000 -> I = NNN;
            case 0xB000 -> programCounter.jump(NNN + V[0]);
            case 0xC000 -> V[X] = random.nextInt(256) & NN;
            case 0xD000 -> drawSprites();
            case 0xE000 -> handle0xE000();
            case 0xF000 -> handle0xF000();
            default -> unknownOpcode();
        }
    }

    /* ---------------- Opcode Families ---------------- */

    private void handle0x0000() {
        switch (NN) {
            case 0xE0 -> display.clearScreen();
            case 0xEE -> programCounter.jump(stack[--sp & 0xF]);
            default -> unknownOpcode();
        }
    }

    private void handle0x8000() {
        switch (N) {
            case 0x0 -> V[X] = V[Y];
            case 0x1 -> V[X] |= V[Y];
            case 0x2 -> V[X] &= V[Y];
            case 0x3 -> V[X] ^= V[Y];
            case 0x4 -> {
                int sum = V[X] + V[Y];
                V[F] = sum > 0xFF ? 1 : 0;
                V[X] = sum & 0xFF;
            }
            case 0x5 -> {
                V[F] = V[X] >= V[Y] ? 1 : 0;
                V[X] = (V[X] - V[Y]) & 0xFF;
            }
            case 0x6 -> {
                V[F] = V[Y] & 0x1;
                V[X] = (V[Y] >> 1) & 0xFF;
            }
            case 0x7 -> {
                V[F] = V[Y] >= V[X] ? 1 : 0;
                V[X] = (V[Y] - V[X]) & 0xFF;
            }
            case 0xE -> {
                V[F] = (V[Y] & 0x80) >> 7;
                V[X] = (V[Y] << 1) & 0xFF;
            }
            default -> unknownOpcode();
        }
    }

    private void handle0xE000() {
        switch (NN) {
            case 0x9E -> { if (keypad.isKeyPressed(V[X])) programCounter.next(); }
            case 0xA1 -> { if (!keypad.isKeyPressed(V[X])) programCounter.next(); }
            default -> unknownOpcode();
        }
    }

    private void handle0xF000() {
        switch (NN) {
            case 0x07 -> V[X] = timer.getDelayTimer();
            case 0x15 -> timer.setDelayTimer(V[X]);
            case 0x18 -> timer.setSoundTimer(V[X]);
            case 0x1E -> { V[F] = (I + V[X] > 0xFFF) ? 1 : 0; I = (I + V[X]) & 0xFFFF; }
            case 0x0A -> keypad.getLastPressedKey().ifPresentOrElse(k -> V[X] = k, programCounter::back);
            case 0x29 -> I = FONT_SET_START + V[X] * 5;
            case 0x33 -> storeBCD();
            case 0x55 -> storeRegisters();
            case 0x65 -> loadRegisters();
            default -> unknownOpcode();
        }
    }

    /* ---------------- Complex Operations ---------------- */

    private void callSubroutine() {
        stack[sp++ & 0xF] = programCounter.getCurrent();
        programCounter.jump(NNN);
    }

    private void drawSprites() {
        V[F] = 0;
        for (int row = 0; row < N; row++) {
            int spriteByte = memory[I + row] & 0xFF;
            for (int col = 0; col < 8; col++) {
                int spritePixel = (spriteByte >> (7 - col)) & 1;
                int xPos = (V[X] + col) % Display.WIDTH;
                int yPos = (V[Y] + row) % Display.HEIGHT;
                if (spritePixel == 1 && display.drawPixel(xPos, yPos)) {
                    V[F] = 1;
                }
            }
        }
    }

    private void storeRegisters() {
        for (int i = 0; i <= X; i++) {
            memory[I + i] = (byte) (V[i] & 0xFF);
        }
    }

    private void loadRegisters() {
        for (int i = 0; i <= X; i++) {
            V[i] = memory[I + i] & 0xFF;
        }
    }

    private void storeBCD() {
        int value = V[X];
        memory[I]     = (byte) (value / 100);
        memory[I + 1] = (byte) ((value / 10) % 10);
        memory[I + 2] = (byte) (value % 10);
    }

    private void unknownOpcode() {
        System.out.printf("Unknown opcode: 0x%04X%n", opcode);
    }

    public void init() throws IOException {
        byte[] program = readAllBytes(get("roms/3-corax+.ch8"));
        initialize(program);
    }
}
