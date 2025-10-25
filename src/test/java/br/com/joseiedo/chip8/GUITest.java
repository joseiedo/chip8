package br.com.joseiedo.chip8;

import org.junit.jupiter.api.Test;

class GUITest {

    @Test
    public void drawPixel() {
        Display gui = new Display();

        gui.drawPixel(20, 20);
    }
}