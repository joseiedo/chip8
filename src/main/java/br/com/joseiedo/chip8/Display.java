package br.com.joseiedo.chip8;

import javafx.scene.canvas.Canvas;

import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.Function;

public class Display {

    public static final int WIDTH = 64;
    public static final int HEIGHT = 32;

    public boolean dirtyScreen = false;

    private byte[] screen = new byte[WIDTH * HEIGHT];

    // Clear the screen
    public void clearScreen() {
        Arrays.fill(screen, (byte) 0);
        dirtyScreen = true;
    }

    // Draw a pixel using XOR, return true if it erased a pixel (collision)
    public boolean drawPixel(int x, int y) {
        x = x % WIDTH;
        y = y % HEIGHT;
        int index = y * WIDTH + x;
        boolean erased = screen[index] == 1;
        screen[index] ^= 1;
        dirtyScreen = true;
        return erased;
    }

    // Print the screen to the terminal
    public void render() {
        if (!dirtyScreen) return;
        System.out.print("\033[H\033[2J");
        System.out.flush();
        StringBuilder sb = new StringBuilder();

        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                sb.append(screen[y * WIDTH + x] == 1 ? '#' : ' ');
            }
            sb.append('\n');
        }

        System.out.print(sb);
        dirtyScreen = false;
    }

    // Optional beep (just print a message)
    public void beep() {
        System.out.println("BEEP!");
    }
}
