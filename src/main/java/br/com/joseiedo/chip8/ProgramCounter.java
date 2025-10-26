package br.com.joseiedo.chip8;

public class ProgramCounter {

    // We could make this as a short
    // But it would require me to mask everytime...
    private int pc;

    private ProgramCounter() {}

    public ProgramCounter(int initialPosition) {
        this.pc = initialPosition;
    }

    public void next() {
        pc += 2;
    }

    public void back() {
        pc -= 2;
    }

    public int getCurrent() {
        return pc;
    }

    public void jump(int value) {
        this.pc = value;
    }
}
