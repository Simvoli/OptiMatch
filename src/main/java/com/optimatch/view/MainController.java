package com.optimatch.view;

import com.optimatch.dao.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

// top-level controller, swaps the inner FXML based on the chosen tab
public class MainController {

    private static final Logger LOGGER = Logger.getLogger(MainController.class.getName());

    @FXML
    private StackPane contentArea;

    @FXML
    private Label statusLabel;

    @FXML
    private Label connectionStatus;

    // FXML init: probe the db and show students by default
    @FXML
    public void initialize() {
        if (DatabaseConnection.testConnection()) {
            connectionStatus.setText("Database: Connected");
            connectionStatus.setStyle("-fx-text-fill: #27ae60;");
        } else {
            connectionStatus.setText("Database: Disconnected");
            connectionStatus.setStyle("-fx-text-fill: #e74c3c;");
        }

        showStudents();
    }

    // show the students screen
    @FXML
    public void showStudents() {
        loadContent("students");
        statusLabel.setText("Student Management");
    }

    // show the projects screen
    @FXML
    public void showProjects() {
        loadContent("projects");
        statusLabel.setText("Project Management");
    }

    // show the algorithm screen
    @FXML
    public void showAlgorithm() {
        loadContent("algorithm");
        statusLabel.setText("Algorithm Configuration");
    }

    // show the results screen
    @FXML
    public void showResults() {
        loadContent("results");
        statusLabel.setText("Results View");
    }

    // load the named FXML into the central area
    private void loadContent(String fxmlName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/" + fxmlName + ".fxml"));
            contentArea.getChildren().clear();
            contentArea.getChildren().add(loader.load());
        } catch (IOException e) {
            statusLabel.setText("Error loading " + fxmlName + ": " + e.getMessage());
            LOGGER.log(Level.SEVERE, "Failed to load FXML: " + fxmlName, e);
        }
    }
}
