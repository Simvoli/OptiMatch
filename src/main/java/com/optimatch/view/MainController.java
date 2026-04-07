package com.optimatch.view;

import com.optimatch.dao.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.io.IOException;

/**
 * Controller for the main application window.
 * Handles navigation between different screens.
 */
public class MainController {

    @FXML
    private StackPane contentArea;

    @FXML
    private Label statusLabel;

    @FXML
    private Label connectionStatus;

    @FXML
    public void initialize() {
        // Test database connection
        if (DatabaseConnection.testConnection()) {
            connectionStatus.setText("Database: Connected");
            connectionStatus.setStyle("-fx-text-fill: #27ae60;");
        } else {
            connectionStatus.setText("Database: Disconnected");
            connectionStatus.setStyle("-fx-text-fill: #e74c3c;");
        }

        // Load students view by default
        showStudents();
    }

    @FXML
    public void showStudents() {
        loadContent("students");
        statusLabel.setText("Student Management");
    }

    @FXML
    public void showProjects() {
        loadContent("projects");
        statusLabel.setText("Project Management");
    }

    @FXML
    public void showAlgorithm() {
        loadContent("algorithm");
        statusLabel.setText("Algorithm Configuration");
    }

    @FXML
    public void showResults() {
        loadContent("results");
        statusLabel.setText("Results View");
    }

    /**
     * Loads an FXML file into the content area.
     *
     * @param fxmlName the name of the FXML file (without extension)
     */
    private void loadContent(String fxmlName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/" + fxmlName + ".fxml"));
            contentArea.getChildren().clear();
            contentArea.getChildren().add(loader.load());
        } catch (IOException e) {
            statusLabel.setText("Error loading " + fxmlName + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}
