package br.com.joseiedo.chip8;

import javafx.scene.input.KeyCode;

import java.util.Map;
import java.util.Optional;

import static java.util.Map.entry;

public class Keypad {

    public static final Map<KeyCode, Integer> keyCodes = Map.ofEntries(
            entry(KeyCode.DIGIT1, 0x1),
            entry(KeyCode.DIGIT2, 0x2),
            entry(KeyCode.DIGIT3, 0x3),
            entry(KeyCode.DIGIT4, 0xC),

            entry(KeyCode.Q, 0x4),
            entry(KeyCode.W, 0x5),
            entry(KeyCode.E, 0x6),
            entry(KeyCode.R, 0xD),

            entry(KeyCode.A, 0x7),
            entry(KeyCode.S, 0x8),
            entry(KeyCode.D, 0x9),
            entry(KeyCode.F, 0xE),

            entry(KeyCode.Z, 0xA),
            entry(KeyCode.X, 0x0),
            entry(KeyCode.C, 0xB),
            entry(KeyCode.V, 0xF)
    );

    private boolean[] pressedKeys = new boolean[keyCodes.size()];

    private Integer lastPressedKeyCode = null;


    public void onKeyPressed(KeyCode key) {
        System.out.println("Key pressed: " + key);

        Integer chip8Key = keyCodes.get(key);
        if (chip8Key == null) return;

        System.out.println("Mapped CHIP-8 key: " + chip8Key);
        pressedKeys[chip8Key] = true;
        lastPressedKeyCode = chip8Key;
    }

    public void onKeyReleased(KeyCode key) {
        System.out.println("Key released: " + key);

        Integer chip8Key = keyCodes.get(key);
        if (chip8Key == null) return;

        System.out.println("Mapped CHIP-8 key: " + chip8Key);
        pressedKeys[chip8Key] = false;

        if (lastPressedKeyCode != null && lastPressedKeyCode.equals(chip8Key)) {
            lastPressedKeyCode = null;
        }
    }

    public boolean isKeyPressed(int keyCode) {
        return pressedKeys[keyCode];
    }

    public Optional<Integer> getLastPressedKey() {
        return Optional.ofNullable(lastPressedKeyCode);
    }
}
