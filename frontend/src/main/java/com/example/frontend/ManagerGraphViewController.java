package com.example.frontend;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.*;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.DatePicker;

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
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextBoundsType;
import javafx.stage.Stage;
import javafx.util.Duration;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import javafx.scene.text.Text;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.Connection;
import javafx.scene.layout.VBox;
import javafx.scene.control.TableView;
import javafx.collections.ObservableList;


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
    Connection conn;

    private Properties readProperties() {
        Properties prop = new Properties();
        try (InputStream input = StartApplication.class.getResourceAsStream("com/example/frontend/config.properties")) {
            prop.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return prop;
    }

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

    public void showSalesTrend(ActionEvent event){
        graphContainer.getChildren().clear();
        ScrollPane graphScroll = new ScrollPane();
        graphScroll.setPrefWidth(750);
        try{
            String sqlStatement = "SELECT EXTRACT(WEEK FROM co.Created_At) AS OrderWeek, SUM(oi.Quantity * mi.Price) AS TotalRevenue FROM Order_Items oi JOIN Menu_Item mi ON oi.Menu_Item_ID = mi.ID JOIN Customer_Order co ON oi.Order_ID = co.ID GROUP BY OrderWeek ORDER BY OrderWeek;";
            Statement stmt = conn.createStatement();
            ResultSet result = stmt.executeQuery(sqlStatement);

            XYChart.Series series = new XYChart.Series();
            int week = 0;
            Double revenue = 0.0;

            Double maxRevenue = 0.0;

            while(result.next()){
                week = result.getInt("orderweek");
                revenue = result.getDouble("totalrevenue");

                if (revenue > maxRevenue){
                    maxRevenue = revenue;
                }

                series.getData().add(new XYChart.Data(week, revenue));
            }
            NumberAxis xAxis = new NumberAxis(0, 52, 1);
            NumberAxis yAxis = new NumberAxis(0, (maxRevenue * 1.10), ((maxRevenue * 1.10) / 50));
            LineChart linechart = new LineChart(xAxis, yAxis);
            linechart.getData().add(series);
            linechart.setPrefWidth(900);
            graphScroll.setContent(linechart);
            graphContainer.getChildren().add(graphScroll);

        } catch (Exception e){
            e.printStackTrace();
            System.out.println("Error accessing Database.");
        }



    }

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
    
            
            String sqlStatement = "SELECT a.Menu_Item_ID AS Menu_Item_ID1, b.Menu_Item_ID AS Menu_Item_ID2, COUNT(*) AS Times_Ordered_Together FROM Order_Items a JOIN Order_Items b ON a.Order_ID = b.Order_ID AND a.Menu_Item_ID < b.Menu_Item_ID JOIN Customer_Order co ON a.Order_ID = co.ID WHERE co.Created_At >= '" + start +"' AND co.Created_At < '" + end +"' GROUP BY a.Menu_Item_ID, b.Menu_Item_ID ORDER BY Times_Ordered_Together DESC;";
            
            Statement stmt = conn.createStatement();
            ResultSet result = stmt.executeQuery(sqlStatement);
    
            TableView<ObservableList<String>> tableView = new TableView<>();
    
            while (result.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                row.add(result.getString("Menu_Item_ID1"));
                row.add(result.getString("Menu_Item_ID2"));
                row.add(result.getString("Times_Ordered_Together"));
                tableView.getItems().add(row);
            }
    
            TableColumn<ObservableList<String>, String> col1 = new TableColumn<>("Menu Item ID 1");
            col1.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().get(0)));
            TableColumn<ObservableList<String>, String> col2 = new TableColumn<>("Menu Item ID 2");
            col2.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().get(1)));
            TableColumn<ObservableList<String>, String> col3 = new TableColumn<>("Times Ordered Together");
            col3.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().get(2)));
    

            // Set preferred width for each column
            col1.setPrefWidth(250);
            col2.setPrefWidth(250);
            col3.setPrefWidth(250); 

            
            tableView.getColumns().addAll(col1, col2, col3);
    
            graphScroll.setContent(tableView);
    
        } catch (Exception e){
            e.printStackTrace();
            System.out.println("Error accessing Database.");
        }
    }

    public void showProductUsage(ActionEvent event) {
        graphContainer.getChildren().clear();
        ScrollPane graphScroll = new ScrollPane();
        graphContainer.getChildren().add(graphScroll);
        
        try {
            LocalDate startDate = productStartDate.getValue();
            String start = startDate.getMonthValue() + "/" + startDate.getDayOfMonth() + "/" + startDate.getYear();
            LocalDate endDate = productEndDate.getValue();
            String end = endDate.getMonthValue() + "/" + endDate.getDayOfMonth() + "/" + endDate.getYear();

            String sqlStatement = "SELECT i.Name, SUM(r.qty) AS inventory_used FROM Customer_Order co, Menu_Item mi JOIN Recipe r ON mi.ID = r.Menu_item JOIN Inventory i ON r.Inventory_item = i.ID WHERE Created_At >= '" + start +"' AND Created_At < '" + end +"' GROUP BY i.Name ORDER BY inventory_used DESC;";
            Statement stmt = conn.createStatement();
            ResultSet result = stmt.executeQuery(sqlStatement);

            CategoryAxis xAxis = new CategoryAxis();
            NumberAxis yAxis = new NumberAxis();
            BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
            barChart.setTitle("Product Usage From: " + start + " - " + end);
            
            barChart.setPrefSize(700, 400);
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
            
            graphScroll.setPadding(new Insets(20, 25, 20, 50));
            graphScroll.setContent(barChart);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error accessing Database.");
        }
    }






    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        menu_close.setVisible(false);
        menu_open.setVisible(true);
        slide_menu.setTranslateX(-100);

        salesTrendButton.setOnAction(this::showSalesTrend);
        productUsageButton.setOnAction((this::showProductUsage));

        conn = DatabaseConnectionManager.getConnection();


//       try{
//
//        } catch (Exception e){
//            e.printStackTrace();
//            System.out.println("Error accessing Database.");
//        }
    }
    
}