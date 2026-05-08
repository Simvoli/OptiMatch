package com.optimatch;

import com.optimatch.dao.DatabaseConnection;
import com.optimatch.util.AppLifecycle;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.NodeOrientation;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main entry point for the OptiMatch application.
 *
 * UI orientation can be switched to right-to-left for Hebrew users
 * by setting the system property {@code -Doptimatch.orientation=rtl}.
 */
public class App extends Application {

    private static final Logger LOGGER = Logger.getLogger(App.class.getName());

    private static final String ORIENTATION_PROPERTY = "optimatch.orientation";

    private static Scene scene;

    @Override
    public void start(Stage stage) {
        scene = new Scene(loadFXML("main"), 1200, 800);
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

    @Override
    public void stop() {
        AppLifecycle.shutdown();
        DatabaseConnection.closeConnection();
    }

    /**
     * Sets the root of the scene to the specified FXML file.
     *
     * @param fxml the FXML file name (without extension)
     */
    public static void setRoot(String fxml) {
        scene.setRoot(loadFXML(fxml));
    }

    /**
     * Loads an FXML file and returns its root node.
     *
     * @param fxml the FXML file name (without extension)
     * @return the root node of the FXML
     */
    private static Parent loadFXML(String fxml) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/fxml/" + fxml + ".fxml"));
            return fxmlLoader.load();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to load FXML: " + fxml, e);
            throw new IllegalStateException("Failed to load FXML: " + fxml, e);
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
