package com.example.frontend;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.Properties;
import java.util.ResourceBundle;
//
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

public class MainSceneController implements Initializable{

    @FXML
    private ImageView menu_close;
    @FXML
    private ImageView menu_open;
    @FXML
    private AnchorPane slide_menu;
    @FXML
    private AnchorPane menu_pane;
    @FXML
    private ScrollPane menu_scroll;

    @FXML
    private ImageView switchBtn;
    private Stage primaryStage;
    private Boolean employeeView;

    private Properties readProperties() {
        Properties prop = new Properties();
        try (InputStream input = new FileInputStream("frontend/src/main/config.properties")) {
            prop.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return prop;
    }

    @FXML
    void switchButton(MouseEvent event) {
        try {
            Stage stage = (Stage) switchBtn.getScene().getWindow();

            String name = "";
            if (employeeView){
                System.out.println("Switching to manager");
                name = "manager-view.fxml";
                employeeView = false;
            }
            else{
                System.out.println("Switching to employee");
                name = "gemma.fxml";
                employeeView = true;
            }
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource(name));
            Parent root = loader.load();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @FXML
    void close_menu(MouseEvent event) {
        TranslateTransition slide =  new TranslateTransition();
        slide.setDuration(Duration.seconds(0.5));
        slide.setNode(slide_menu);

        slide.setToX(-100);
        slide.play();

        slide_menu.setTranslateX(0);

        slide.setOnFinished((ActionEvent e) ->{
            menu_close.setVisible(false);
            menu_open.setVisible(true);
        });
    }

    @FXML
    void open_menu(MouseEvent event) {
        TranslateTransition slide =  new TranslateTransition();
        slide.setDuration(Duration.seconds(0.5));
        slide.setNode(slide_menu);

        slide.setToX(0);
        slide.play();

        slide_menu.setTranslateX(-100);

        slide.setOnFinished((ActionEvent e) ->{
            menu_close.setVisible(true);
            menu_open.setVisible(false);
        });
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    @FXML
    void complete_order(MouseEvent event) {
        // Create a new Stage for the dialog
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(primaryStage);

        // Create the layout for the dialog
        VBox dialogVBox = new VBox(20);
        dialogVBox.setAlignment(Pos.CENTER);

        // Add components to the layout
        Label nameLabel = new Label("Name on Order");
        TextField nameField = new TextField();
        Button continueButton = new Button("Continue to Payment");
        continueButton.setOnAction(e -> {
            // Handle the continue action
            dialogStage.close();
        });

        dialogVBox.getChildren().addAll(nameLabel, nameField, continueButton);

        // Set the scene and show the stage
        Scene dialogScene = new Scene(dialogVBox, 300, 200);
        dialogStage.setScene(dialogScene);
        dialogStage.showAndWait();
    }

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        employeeView = true;

        // initializing the vertical menu
        menu_close.setVisible(false);
        menu_open.setVisible(true);
        slide_menu.setTranslateX(-100);

        Properties prop = readProperties();

        // connecting to the database
        Connection conn = null;

        String database_name = prop.getProperty("database.name");
        String database_user = prop.getProperty("database.user");
        String database_password = prop.getProperty("database.password");
        String database_host = prop.getProperty("database.host");
        String database_url = String.format("jdbc:postgresql://%s/%s", database_host, database_name);
        
        try {
            conn = DriverManager.getConnection(database_url, database_user, database_password);
            System.out.println("Successfully connected to database.");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName()+": "+e.getMessage());
            System.exit(0);
            System.out.println("Could not connect to database.");
        }


        String name = "";
        String category = "";
        ArrayList<String> categories = new ArrayList<String>();
        ArrayList<VBox> menu_categories = new ArrayList<VBox>();
        ArrayList<TilePane> menu_tilePanes = new ArrayList<TilePane>();
        VBox menu_layout = new VBox();
        try{
            //Asking for all of the menu items from the database
            Statement stmt = conn.createStatement();
            String sqlStatement = "SELECT * FROM menu_item;";
            ResultSet result = stmt.executeQuery(sqlStatement);

            while (result.next()) {
                name = result.getString("name");
                category = result.getString("category");

                // seeing if we already have the category that this menu item is a part of
                int index = categories.indexOf(category);

                // we do not have the category made, make a new one and add it to the list
                if (index == -1){
                    categories.add(category);

                    // New Vbox to hold the ti
                    VBox add_VBox = new VBox();
                    add_VBox.setPrefWidth(650);// prefWidth
                    add_VBox.setSpacing(10);

                    // New label displaying which category it is
                    Label add_Label = new Label(category);
                    add_Label.setFont(Font.font("verdana", FontWeight.BOLD, FontPosture.REGULAR, 16));

                    // New tile pane to hold food items within
                    TilePane new_pane = new TilePane();
                    new_pane.setPrefColumns(4);
                    new_pane.setHgap(40);
                    new_pane.setVgap(20);

                    // adding the children to the container
                    add_VBox.getChildren().add(add_Label);
                    add_VBox.getChildren().add(new_pane);

                    // adding the container to the page
                    menu_categories.add(add_VBox);
                    menu_tilePanes.add(new_pane);
                    index = categories.indexOf(category);
                    menu_layout.getChildren().add(add_VBox);
                }

                // creating a new button with the menu item and adding it to the appropriate category
                Button btn = new Button(name);
                btn.setPrefHeight(120);
                btn.setPrefWidth(120);
                btn.wrapTextProperty().setValue(true);
                btn.setTextAlignment(TextAlignment.CENTER);
                btn.setFont(Font.font("verdana", FontWeight.BOLD, FontPosture.REGULAR, 12));
                menu_tilePanes.get(index).getChildren().add(btn);
            }

            menu_scroll.setContent(menu_layout);
            menu_layout.setSpacing(70);
            menu_layout.setPadding(new Insets(60, 60, 60, 60));
        } catch (Exception e){
            e.printStackTrace();
            System.out.println("Error accessing Database.");
        }

        try {
            conn.close();
            System.out.println("Database closed successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error in closing database.");
        }
    }
}