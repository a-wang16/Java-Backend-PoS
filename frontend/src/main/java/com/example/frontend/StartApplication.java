package com.example.frontend;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * This class extends the JavaFX Application class.
 * It serves as the main entry point for the JavaFX application.
 * @author Jin Seok Oh
 */
public class StartApplication extends Application {

    /**
     * Starts the primary stage for this application.
     *
     * @param stage The primary stage for this application, onto which
     *              the application scene can be set. The primaryStage is created by the JavaFX platform.
     * @throws IOException if loading the fxml file fails
     */
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(StartApplication.class.getResource("login.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle("Login");
        stage.show();
    }
    
    /**
     * The main method to launch the application.
     * This method is the entry point when the application is launched from the command line.
     *
     * @param args Command line arguments passed to the launch method.
     */
    public static void main(String[] args) {
        launch();
    }
}