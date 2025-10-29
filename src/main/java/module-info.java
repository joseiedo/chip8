module br.com.joseiedo.chip8 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires javafx.graphics;
    requires javafx.media;


    opens br.com.joseiedo.chip8 to javafx.fxml;
    exports br.com.joseiedo.chip8;
}