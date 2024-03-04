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

    @FXML
    private ComboBox<DatabaseOperations.Employee> employeeComboBox;

    @FXML
    private PasswordField passwordField;

    @FXML
    public void initialize() {
        List<DatabaseOperations.Employee> employees = DatabaseOperations.fetchAllEmployees();
        employeeComboBox.getItems().addAll(employees);
    }

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

    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
