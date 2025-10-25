package br.com.joseiedo.chip8;

import javafx.animation.Animation;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

import static br.com.joseiedo.chip8.Display.HEIGHT;
import static br.com.joseiedo.chip8.Display.WIDTH;

public class Chip8Application extends Application {

    private static final int SCALE = 10;



    @Override
    public void start(Stage stage) throws Exception {
        Canvas canvas = new Canvas(WIDTH * SCALE, HEIGHT * SCALE);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        Display display = new Display();
        Chip8 chip8 = new Chip8(display);
        chip8.init();

        Scene scene = new Scene(new javafx.scene.Group(canvas));
        new AnimationTimer() {
            private long lastTimerUpdate = 0;

            @Override
            public void handle(long now) {
                long lastTimerUpdate = System.nanoTime();
                final double nsPerTick = 1_000_000_000.0 / 60; // 60 Hz
                chip8.emulateCycle();   // fetch-decode-execute
            }
        }.start();

        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle("CHIP-8 Emulator");
        stage.show();
    }
//
//    private void render(GraphicsContext gc) {
//        for (int y = 0; y < HEIGHT; y++) {
//            for (int x = 0; x < WIDTH; x++) {
//                int index = y * WIDTH + x;
//                if (display.screen[index] == 1) {
//                    gc.setFill(Color.WHITE);
//                } else {
//                    gc.setFill(Color.BLACK);
//                }
//                gc.fillRect(x * SCALE, y * SCALE, SCALE, SCALE);
//            }
//        }
//    }
//
//    public static void main(String[] args) {
//        launch(args);
//    }
}
