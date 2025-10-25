package br.com.joseiedo.chip8;

public class Timers {
    private int delayTimer;

    private int soundTimer;

    private final Sound sound = new Sound();

    public void cycle() {
        if (delayTimer > 0) delayTimer--;
        if (soundTimer > 0) {
            sound.beep();
            soundTimer--;
        }
    }

    public void initialize() {
        delayTimer = 0;
        soundTimer = 0;
    }
}
