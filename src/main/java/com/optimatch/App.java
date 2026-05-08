package com.optimatch;

import com.optimatch.util.AppLifecycle;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.NodeOrientation;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.logging.Level;
import java.util.logging.Logger;

// app entry point
// RU: системное свойство optimatch.orientation=rtl включает направление справа налево
// (нужно для иврита)
public class App extends Application {

    private static final Logger LOGGER = Logger.getLogger(App.class.getName());

    private static final String ORIENTATION_PROPERTY = "optimatch.orientation";

    // load main FXML and show the window
    @Override
    public void start(Stage stage) {
        Scene scene = new Scene(loadFXML("main"), 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

        if ("rtl".equalsIgnoreCase(System.getProperty(ORIENTATION_PROPERTY, "ltr"))) {
            scene.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        }

        stage.setTitle("OptiMatch - Student-Project Matching System");
        stage.setScene(scene);
        stage.setMinWidth(800);
        stage.setMinHeight(600);
        stage.show();
    }

    // shut down the background executor on exit
    @Override
    public void stop() {
        AppLifecycle.shutdown();
    }

    // load an FXML file by name
    private static Parent loadFXML(String fxml) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/fxml/" + fxml + ".fxml"));
            return fxmlLoader.load();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to load FXML: " + fxml, e);
            throw new IllegalStateException("Failed to load FXML: " + fxml, e);
        }
    }

    // hand off to JavaFX runtime
    public static void main(String[] args) {
        launch();
    }
}
