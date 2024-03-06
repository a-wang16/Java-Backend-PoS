package com.example.frontend;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

/**
 * This class serves as the controller for the login view in a JavaFX application.
 * It is responsible for initializing the login components, handling user authentication,
 * and navigating to different views based on the user role.
 * @author Jin Seok Oh
 */
public class LoginController {

    @FXML
    private ComboBox<DatabaseOperations.Employee> employeeComboBox;

    @FXML
    private PasswordField passwordField;

    /**
     * Initializes the login form by populating the employee combo box with all available employees.
     * It is called automatically after the FXML fields have been populated.
     * This fetches all employees using Database operations, and populates the employeeComboBox.
     */
    @FXML
    public void initialize() {
        List<DatabaseOperations.Employee> employees = DatabaseOperations.fetchAllEmployees();
        employeeComboBox.getItems().addAll(employees);
    }

    /**
     * Handles the login action triggered by the user.
     * It authenticates the user based on the selected employee and password.
     * If authentication is successful, it navigates to either the manager view or the employee entry view.
     * Otherwise, it shows an error alert.
     * This fetches all employees using Database operations, and populates the employeeComboBox.
     */
    @FXML
    private void handleLoginAction() {
        DatabaseOperations.Employee selectedEmployee = employeeComboBox.getValue();
        String password = passwordField.getText();

        if (selectedEmployee != null && DatabaseOperations.authenticate(selectedEmployee.getName(), password)) {
            DatabaseOperations.setCurrentEmployee(selectedEmployee);

            if (selectedEmployee.isManager()) {
                openView("manager-view.fxml");
            } else {
                openView("employee-entry-view.fxml");
            }
        } else {
            showAlert("Login Failed", "Invalid Credentials", "Please try again.");
        }
    }

    /**
     * This loads the given .fxml file given as a string. The .fxml file needs to be located within the resources
     * directory.
     * @param fxmlFile The name of the FXML file to load, located in the resources directory.
     * @throws IOException If the specified FXML file cannot be loaded.
     */
    private void openView(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();
            Stage stage = (Stage) employeeComboBox.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open the view", e.getMessage());
        }
    }

     /**
     * Displays an alert dialog to the user. Typically triggered by an incorrect password. It accepts
     * @param title The title of the alert dialog.
     * @param header The header text of the alert dialog.
     * @param content The content text of the alert dialog.
     */
    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
