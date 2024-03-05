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

public class LoginController {
    /**
     * This manages the actions that can be performed via login.fxml.
     */

    @FXML
    private ComboBox<DatabaseOperations.Employee> employeeComboBox;

    @FXML
    private PasswordField passwordField;

    @FXML
    public void initialize() {
        /**
         * This fetches all employees using Database operations, and populates the employeeComboBox.
         */
        List<DatabaseOperations.Employee> employees = DatabaseOperations.fetchAllEmployees();
        employeeComboBox.getItems().addAll(employees);
    }

    @FXML
    private void handleLoginAction() {
        /**
         * This fetches all employees using Database operations, and populates the employeeComboBox.
         */
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


    private void openView(String fxmlFile) {
        /**
         * This loads the given .fxml file given as a string. The .fxml file needs to be located within the resources
         * directory.
         */
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

    private void showAlert(String title, String header, String content) {
        /**
         * This shows an alert, typically triggered by an incorrect password.
         * It accepts
         */
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
