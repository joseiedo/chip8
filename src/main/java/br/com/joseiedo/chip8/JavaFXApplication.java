package br.com.joseiedo.chip8;

import static br.com.joseiedo.chip8.Display.HEIGHT;
import static br.com.joseiedo.chip8.Display.WIDTH;

import javafx.animation.AnimationTimer;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

public class JavaFXApplication extends Application {

    private static final int SCALE = 10;

    @Override
    public void start(Stage stage) throws Exception {
        Canvas canvas = new Canvas(WIDTH * SCALE, HEIGHT * SCALE);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        Display display = new Display(screen -> render(gc, screen));
        Keypad keypad = new Keypad();
        Chip8 chip8 = new Chip8(display, keypad);
        chip8.init();

        Scene scene = new Scene(new javafx.scene.Group(canvas));
        scene.addEventHandler(KeyEvent.KEY_PRESSED, (key) -> keypad.onKeyPressed(key.getCode()));
        scene.addEventHandler(KeyEvent.KEY_RELEASED, (key) -> keypad.onKeyReleased(key.getCode()));

        new AnimationTimer() {
            @Override
            public void handle(long now) {
                // Need to consider delta in the future, but I'm too lazy at the moment
                chip8.emulateCycle();
            }
        }.start();

        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle("CHIP-8 Emulator");
        stage.show();
    }

    private void render(GraphicsContext gc, byte[] screen) {
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                int index = y * WIDTH + x;
                if (screen[index] == 1) {
                    gc.setFill(Color.WHITE);
                } else {
                    gc.setFill(Color.BLACK);
                }
                gc.fillRect(x * SCALE, y * SCALE, SCALE, SCALE);
            }
        }
    }
}
