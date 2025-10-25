package br.com.joseiedo.chip8;

import java.util.Arrays;
import java.util.function.Consumer;

public class Display {

    public static final int WIDTH = 64;
    public static final int HEIGHT = 32;

    public boolean dirtyScreen = false;

    private byte[] screen = new byte[WIDTH * HEIGHT];

    public Display(Consumer<byte[]> render) {
        this.renderizer = render;
    }

    private Consumer<byte[]> renderizer;

    public void clearScreen() {
        Arrays.fill(screen, (byte) 0);
        dirtyScreen = true;
    }

    // Draw a pixel using XOR, return true if it erased a pixel (collision)
    public boolean drawPixel(int x, int y) {
        int index = (y % HEIGHT) * WIDTH + (x % WIDTH);
        boolean erased = screen[index] == 1;
        screen[index] ^= 1;
        dirtyScreen = true;
        return erased;
    }
    public void render() {
        if (!dirtyScreen) return;
        renderizer.accept(screen);
        dirtyScreen = false;
    }
}
