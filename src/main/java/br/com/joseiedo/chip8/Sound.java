package br.com.joseiedo.chip8;


import javafx.scene.media.AudioClip;

import java.io.File;

public class Sound {

    private final AudioClip ALERT_AUDIOCLIP = new AudioClip(new File("assets/beep.mp3").toURI().toString());

    public Sound() {
        ALERT_AUDIOCLIP.setRate(5.0);
    }

    public void beep() {
        if (!ALERT_AUDIOCLIP.isPlaying()){
            ALERT_AUDIOCLIP.play();
        }
    }
}
