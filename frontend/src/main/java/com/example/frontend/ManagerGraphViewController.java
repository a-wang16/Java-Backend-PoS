package com.example.frontend;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.*;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;

import java.time.LocalDate;
import java.util.Properties;
import java.util.ResourceBundle;
import javafx.animation.TranslateTransition;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;

import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.Connection;
import javafx.scene.layout.VBox;
import javafx.scene.control.TableView;
import javafx.collections.ObservableList;

/** 
 * Controller file for the Manager's Graph View.
 * Displays data from the database regarding sales trends, item usages, and other
 * important information.
 * @author Allen, Yohan, Gemma 
 */
public class ManagerGraphViewController implements Initializable{
    @FXML
    private Pane graphButtonContainer;
    @FXML
    private Pane graphContainer;
    @FXML
    private ImageView menu_close;
    @FXML
    private ImageView menu_open;
    @FXML
    private Button salesTrendButton;
    @FXML
    private AnchorPane slide_menu;
    @FXML
    private ImageView switchBtn;
    @FXML
    private Button productUsageButton;
    private Stage primaryStage;
    private Boolean employeeView;

    @FXML
    private Button whatSellsTogetherButton;
    @FXML 
    private DatePicker productEndDate1;
    @FXML 
    private DatePicker productStartDate1;

    @FXML
    private DatePicker productEndDate;
    @FXML
    private DatePicker productStartDate;
    @FXML 
    private Button backButton;
    @FXML
    private Label userProfile;
    
    @FXML 
    private DatePicker productStartDate2;
    @FXML
    private DatePicker productEndDate2;

    @FXML
    private Button excessButton;
    @FXML 
    private DatePicker productStartDate3;

    Connection conn;



    
    /** 
     * Grabs the PSQL login information from the 'config.properties' file. 
     * Returns a Property object definied in DatabaseConnectionManager.java.
     * @return Properties
     */
    private Properties readProperties() {
        Properties prop = new Properties();
        try (InputStream input = StartApplication.class.getResourceAsStream("com/example/frontend/config.properties")) {
            prop.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return prop;
    }
    
    
    /** 
     * Button control that returns back to the default manager view.
     * @param event
     */
    @FXML
    void backButtonHandle(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/frontend/manager-view.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("View Switch Failed");
            alert.setContentText("Unable to load the login view.");
            alert.showAndWait();
        }
    }

    /**
     * Controls the user-switch button that returns to the login page to switch views.
     * @param event
    */
    @FXML
    void switchButton(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/frontend/login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("View Switch Failed");
            alert.setContentText("Unable to load the login view.");
            alert.showAndWait();
        }
    }

    /**
     * One of the two controller function that closes the left sidebar.
     * @param event
     */
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

    /**
     * One of the two controller function that opens the left sidebar.
     * @param event
     */
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

    /**
     * Grabs the necessary Stage object for proper FXML control.
     * @param primaryStage
     */
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }


    /**
     * Generates the "Sales Trend" graph. Inserts FXML components into the designated graph area and
     * reads from the inputted date range. Displays the sales by item from the order history from the database.
     * @param event
     */
    public void showSalesTrend(ActionEvent event) {
        graphContainer.getChildren().clear();
        ScrollPane graphScroll = new ScrollPane();
        graphContainer.getChildren().add(graphScroll);
        try {
            LocalDate startDate = productStartDate2.getValue();
            String start = startDate.getMonthValue() + "/" + startDate.getDayOfMonth() + "/" + startDate.getYear();
            LocalDate endDate = productEndDate2.getValue();
            String end = endDate.getMonthValue() + "/" + endDate.getDayOfMonth() + "/" + endDate.getYear();

            Label tableName = new Label("Sales Trend From: " + start + " - " + end);
            tableName.setMinHeight(40);
            tableName.setMinWidth(790);
            tableName.setAlignment(Pos.CENTER);
            tableName.setFont(new Font(17));
            graphContainer.getChildren().add(tableName);

            String sqlStatement = "SELECT mi.Name AS Menu_Item_Name, " +
                    "SUM(oi.Quantity) AS Total_Quantity_Sold, " +
                    "SUM(oi.Quantity * mi.Price) AS Total_Revenue " +
                    "FROM Order_Items oi " +
                    "JOIN Menu_Item mi ON oi.Menu_Item_ID = mi.ID " +
                    "JOIN Customer_Order co ON oi.Order_ID = co.ID " +
                    "WHERE co.Created_At >= '" + start + "' AND co.Created_At < '" + end + "' " +
                    "GROUP BY mi.Name " +
                    "ORDER BY Total_Quantity_Sold DESC;";
    
            Statement stmt = conn.createStatement();
            ResultSet result = stmt.executeQuery(sqlStatement);
    
            TableView<ObservableList<String>> tableView = new TableView<>();
    
            while (result.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                row.add(result.getString("Menu_Item_Name"));
                row.add(result.getString("Total_Quantity_Sold"));
                row.add(result.getString("Total_Revenue")); 
                tableView.getItems().add(row);
            }
    
            TableColumn<ObservableList<String>, String> itemNameCol = new TableColumn<>("Menu Item Name");
            itemNameCol.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().get(0)));
    
            TableColumn<ObservableList<String>, String> totalQuantityCol = new TableColumn<>("Total Quantity Sold");
            totalQuantityCol.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().get(1)));
    
            TableColumn<ObservableList<String>, String> totalRevenueCol = new TableColumn<>("Total Revenue (USD)");
            totalRevenueCol.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().get(2))); 
    
            itemNameCol.setPrefWidth(450);
            totalQuantityCol.setPrefWidth(150);
            totalRevenueCol.setPrefWidth(150);
    
            tableView.getColumns().addAll(itemNameCol, totalQuantityCol, totalRevenueCol);
    
            graphScroll.setPadding(new Insets(40, 25, 20, 20));
            graphScroll.setContent(tableView);
    
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error accessing Database.");
        }
    }


    /**
     * Generates the "Sells Together Trend" table. Displays a list of pairs of 
     * menu items that sell together often, popular or not, sorted by most frequent.
     * @param event
     */
    @FXML
    public void showWhatSellsTogether(ActionEvent event){
        graphContainer.getChildren().clear();
        ScrollPane graphScroll = new ScrollPane();
        graphContainer.getChildren().add(graphScroll);
        
        

        try{
            LocalDate startDate = productStartDate1.getValue();
            String start = startDate.getMonthValue() + "/" + startDate.getDayOfMonth() + "/" + startDate.getYear();
            LocalDate endDate = productEndDate1.getValue();
            String end = endDate.getMonthValue() + "/" + endDate.getDayOfMonth() + "/" + endDate.getYear();
    
            Label tableName = new Label("Best Selling Combo From: " + start + " - " + end);
            tableName.setMinHeight(40);
            tableName.setMinWidth(790);
            tableName.setAlignment(Pos.CENTER);
            tableName.setFont(new Font(17));
            graphContainer.getChildren().add(tableName);

            String sqlStatement = "SELECT aMenu.name as Menu_Item_1, bMenu.name as Menu_Item_2, COUNT(*) AS Times_Ordered_Together FROM Order_Items a JOIN Order_Items b ON a.Order_ID = b.Order_ID AND a.Menu_Item_ID < b.Menu_Item_ID JOIN Menu_Item aMenu ON a.Menu_Item_ID = aMenu.ID JOIN Menu_Item bMenu ON b.Menu_Item_ID = bMenu.ID JOIN Customer_Order co ON co.ID = a.Order_ID WHERE Created_At >= '" + start + "' AND Created_At < '" + end + "' GROUP BY aMenu.name, bMenu.name ORDER BY Times_Ordered_Together DESC LIMIT 15;";
            
            Statement stmt = conn.createStatement();
            ResultSet result = stmt.executeQuery(sqlStatement);
    
            TableView<ObservableList<String>> tableView = new TableView<>();
            tableView.setLayoutY(20);
            tableView.setPrefSize(752, 400);
    
            while (result.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                row.add(result.getString("Menu_Item_1"));
                row.add(result.getString("Menu_Item_2"));
                row.add(result.getString("Times_Ordered_Together"));
                tableView.getItems().add(row);
            }
            

            TableColumn<ObservableList<String>, String> col1 = new TableColumn<>("Menu Item 1");
            col1.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().get(0)));
            TableColumn<ObservableList<String>, String> col2 = new TableColumn<>("Menu Item 2");
            col2.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().get(1)));
            TableColumn<ObservableList<String>, String> col3 = new TableColumn<>("Times Ordered Together");
            col3.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().get(2)));
    

            // Set preferred width for each column
            col1.setPrefWidth(300);
            col2.setPrefWidth(300);
            col3.setPrefWidth(150); 


            tableView.getColumns().addAll(col1, col2, col3);

            graphScroll.setPadding(new Insets(40, 25, 20, 20));

            graphScroll.setContent(tableView);
    
        } catch (Exception e){
            e.printStackTrace();
            System.out.println("Error accessing Database.");
        }
    }

    /**
     * Generates the "Product Usage" graph. Displays the amount of inventory used during that time period.
     * @param event
     */
    public void showProductUsage(ActionEvent event) {
        graphContainer.getChildren().clear();
        ScrollPane graphScroll = new ScrollPane();
        graphContainer.getChildren().add(graphScroll);
        
        try {
            LocalDate startDate = productStartDate.getValue();
            String start = startDate.getMonthValue() + "/" + startDate.getDayOfMonth() + "/" + startDate.getYear();
            LocalDate endDate = productEndDate.getValue();
            String end = endDate.getMonthValue() + "/" + endDate.getDayOfMonth() + "/" + endDate.getYear();

            String sqlStatement = "SELECT i.Name, SUM(r.qty * oi.quantity) AS inventory_used FROM Menu_Item mi JOIN Order_Items oi ON mi.ID = oi.Menu_Item_ID JOIN Customer_Order co ON co.ID = oi.Order_ID JOIN Recipe r ON mi.ID = r.Menu_item JOIN Inventory i ON r.Inventory_item = i.ID WHERE co.Created_At >= '" + start +"' AND co.Created_At < '" + end + "' GROUP BY  i.Name ORDER BY inventory_used DESC;";
            Statement stmt = conn.createStatement();
            ResultSet result = stmt.executeQuery(sqlStatement);

            CategoryAxis xAxis = new CategoryAxis();
            NumberAxis yAxis = new NumberAxis();
            BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
            barChart.setTitle("Product Usage From: " + start + " - " + end);
            
            barChart.setPrefSize(750, 400);
            xAxis.setLabel("Inventory Item");
            yAxis.setLabel("Amount Used");
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            barChart.setLegendVisible(false);
            
            while (result.next()) {
                String name = result.getString("name");
                int qty = result.getInt("inventory_used");
                series.getData().add(new XYChart.Data<>(name, qty));
            }

            barChart.getData().add(series);
            
            graphScroll.setPadding(new Insets(5, 25, 20, 20));
            graphScroll.setContent(barChart);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error accessing Database.");
        }
    }

    /**
     * Generates the "Excess Inventory" graph. Displays the list of items that only sold less than 10% 
     * of their inventory between the timestamp and the current time, assuming no restocks have happened.
     * @param event
     */
    public void showExcessInventory(ActionEvent event) {
        graphContainer.getChildren().clear();
        ScrollPane graphScroll = new ScrollPane();
        graphContainer.getChildren().add(graphScroll);
        
        try {
            LocalDate startDate = productStartDate3.getValue();
            String start = startDate.getMonthValue() + "/" + startDate.getDayOfMonth() + "/" + startDate.getYear();

            Label tableName = new Label("Excess Inventory Starting From: " + start);
            tableName.setMinHeight(40);
            tableName.setMinWidth(790);
            tableName.setAlignment(Pos.CENTER);
            tableName.setFont(new Font(17));
            graphContainer.getChildren().add(tableName);

            String sqlStatement = "SELECT i.Name, SUM(r.qty * oi.quantity) AS inventory_used, i.quantity AS current_inventory, (CAST (SUM(r.qty * oi.quantity) AS float) / (SUM(r.qty * oi.quantity) + i.quantity)) AS relative_performance FROM Menu_Item mi JOIN Order_Items oi ON mi.ID = oi.Menu_Item_ID JOIN Customer_Order co ON co.ID = oi.Order_ID JOIN Recipe r ON r.Menu_item = mi.ID JOIN Inventory i ON r.Inventory_item = i.ID WHERE co.Created_At >= '" + start + "' GROUP BY i.Name, i.quantity ORDER BY relative_performance ASC;";
            
            Statement stmt = conn.createStatement();
            ResultSet result = stmt.executeQuery(sqlStatement);
    
            TableView<ObservableList<String>> tableView = new TableView<>();
            tableView.setLayoutY(20);
            tableView.setPrefSize(752, 400);
    
            while (result.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                row.add(result.getString("name"));
                row.add(result.getString("inventory_used"));
                row.add(result.getString("current_inventory"));
                row.add((String.valueOf(result.getDouble("relative_performance")*100)));
                tableView.getItems().add(row);
            }
            

            TableColumn<ObservableList<String>, String> col1 = new TableColumn<>("Inventory Item");
            col1.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().get(0)));
            TableColumn<ObservableList<String>, String> col2 = new TableColumn<>("Amount Used");
            col2.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().get(1)));
            TableColumn<ObservableList<String>, String> col3 = new TableColumn<>("Current Amount");
            col3.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().get(2)));
            TableColumn<ObservableList<String>, String> col4 = new TableColumn<>("Relative Sold (%)");
            col4.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().get(3)));
    

            // Set preferred width for each column
            col1.setPrefWidth(350);
            col2.setPrefWidth(125);
            col3.setPrefWidth(125); 
            col4.setPrefWidth(150);

            tableView.getColumns().addAll(col1, col2, col3, col4);

            graphScroll.setPadding(new Insets(40, 25, 20, 20));

            graphScroll.setContent(tableView);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error accessing Database.");
        }
    }


    /**
     * Inital function that runs when the FXML is loaded. Sets the default values of the side bar and
     * prepares the buttons to generated different graphs.
     */
    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        menu_close.setVisible(false);
        menu_open.setVisible(true);
        slide_menu.setTranslateX(-100);

        salesTrendButton.setOnAction(this::showSalesTrend);
        productUsageButton.setOnAction((this::showProductUsage));
        excessButton.setOnAction((this::showExcessInventory));

        conn = DatabaseConnectionManager.getConnection();

        userProfile.setText("Profile: " + DatabaseOperations.currentEmployee.getName());
        userProfile.setFont(Font.font("verdana", FontWeight.BOLD, FontPosture.REGULAR, 12));

    }
}