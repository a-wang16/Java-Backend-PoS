package com.example.frontend;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseOperations {
    public static Employee currentEmployee;

    public static void setCurrentEmployee(Employee employee) {
        currentEmployee = employee;
    }

    public static Employee getCurrentEmployee() {
        return currentEmployee;
    }

    public static void createOrderAndUpdateInventory(int employeeId, List<OrderItem> orderItems, String orderCustomerName) {
        Connection conn = DatabaseConnectionManager.getConnection();
        try {
            conn.setAutoCommit(false);
            System.out.println("Successfully connected to the database.");
            System.out.println("Starting transaction for creating order and updating inventory.");

            // check if inventory is sufficient for all order items based on the recipe
            Map<Integer, Integer> totalRequiredInventory = new HashMap<>();
            for (OrderItem item : orderItems) {
                String recipeRequirementSQL =
                        "SELECT Inventory_item, SUM(Qty * ?) as TotalRequired " +
                                "FROM Recipe WHERE Menu_item = ? GROUP BY Inventory_item";
                PreparedStatement recipeRequirementStmt = conn.prepareStatement(recipeRequirementSQL);
                recipeRequirementStmt.setInt(1, item.getQuantity());
                recipeRequirementStmt.setInt(2, item.getMenuItemId());
                ResultSet recipeRs = recipeRequirementStmt.executeQuery();

                while (recipeRs.next()) {
                    int inventoryItemId = recipeRs.getInt("Inventory_item");
                    int totalRequired = recipeRs.getInt("TotalRequired");
                    totalRequiredInventory.put(inventoryItemId, totalRequiredInventory.getOrDefault(inventoryItemId, 0) + totalRequired);
                }
            }

            boolean inventorySufficient = true;
            for (Map.Entry<Integer, Integer> entry : totalRequiredInventory.entrySet()) {
                int inventoryItemId = entry.getKey();
                int requiredQuantity = entry.getValue();

                String checkInventorySQL = "SELECT Quantity FROM Inventory WHERE ID = ?";
                PreparedStatement checkInventoryStmt = conn.prepareStatement(checkInventorySQL);
                checkInventoryStmt.setInt(1, inventoryItemId);
                ResultSet inventoryRs = checkInventoryStmt.executeQuery();

                if (inventoryRs.next()) {
                    int availableQuantity = inventoryRs.getInt("Quantity");
                    if (requiredQuantity > availableQuantity) {
                        System.out.println("Insufficient inventory for Inventory Item ID: " + inventoryItemId);
                        inventorySufficient = false;
                        break;
                    }
                }
            }

            if (!inventorySufficient) {
                conn.rollback();
                System.out.println("Not enough inventory!");
                return;
            }

            String insertOrderSQL = "INSERT INTO Customer_Order (Employee_ID, Created_At, Status, Name) VALUES (?, ?, 'NEW', ?) RETURNING ID";
            PreparedStatement insertOrderStmt = conn.prepareStatement(insertOrderSQL);
            insertOrderStmt.setInt(1, employeeId);
            insertOrderStmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            insertOrderStmt.setString(3, orderCustomerName);
            ResultSet orderRs = insertOrderStmt.executeQuery();
            orderRs.next();
            int orderId = orderRs.getInt(1);
            System.out.println("Order created with ID: " + orderId);

            for (OrderItem item : orderItems) {
                String insertItemSQL = "INSERT INTO Order_Items (Order_ID, Menu_Item_ID, Quantity) VALUES (?, ?, ?)";
                PreparedStatement insertItemStmt = conn.prepareStatement(insertItemSQL);
                insertItemStmt.setInt(1, orderId);
                insertItemStmt.setInt(2, item.getMenuItemId());
                insertItemStmt.setInt(3, item.getQuantity());
                insertItemStmt.executeUpdate();
                System.out.println("Order item added. Menu Item ID: " + item.getMenuItemId() + ", Quantity: " + item.getQuantity());

                String updateInventorySQL = "UPDATE Inventory SET Quantity = GREATEST(0, Quantity - ?) WHERE ID IN (SELECT Inventory_item FROM Recipe WHERE Menu_item = ?)";
                PreparedStatement updateInventoryStmt = conn.prepareStatement(updateInventorySQL);
                updateInventoryStmt.setInt(1, item.getQuantity());
                updateInventoryStmt.setInt(2, item.getMenuItemId());
                updateInventoryStmt.executeUpdate();
                System.out.println("Inventory updated for Menu Item ID: " + item.getMenuItemId());
            }

            conn.commit();
            System.out.println("Order created and inventory updated.");

        } catch (SQLException e) {
            System.out.println("Exception during order creation and inventory update: " + e.getMessage());
            try {
                conn.rollback();
            } catch (SQLException ex) {
                System.out.println("Exception during rollback: " + ex.getMessage());
            }
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public static boolean authenticate(String username, String password) {
        String sql = "SELECT * FROM Employee WHERE Name = ? AND Password = ?";
        try (Connection conn = DatabaseConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return true;
            }
        } catch (SQLException e) {
            System.out.println("Authentication error: " + e.getMessage());
        }
        return false;
    }

    public static List<Employee> fetchAllEmployees() {
        List<Employee> employees = new ArrayList<>();
        String sql = "SELECT ID, Name, Position FROM Employee";

        try (Connection conn = DatabaseConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("ID");
                String name = rs.getString("Name");
                String position = rs.getString("Position");
                Employee employee = new Employee(id, name, position);
                employees.add(employee);
            }
        } catch (SQLException e) {
            System.out.println("Error fetching employees: " + e.getMessage());
        }
        return employees;
    }

    public static class OrderItem {
        private final int menuItemId;
        private final int quantity;
        private final String name;
        private final Double price;


        public OrderItem(int menuItemId, int quantity, String name, Double price) {
            this.menuItemId = menuItemId;
            this.quantity = quantity;
            this.name = name;
            this.price = price;
        }

        public int getMenuItemId() {
            return menuItemId;
        }

        public int getQuantity() {
            return quantity;
        }

        public String getName() {
            return name;
        }

        public Double getPrice() {
            return price;
        }
    }

    public static class Inventory {
        private final int id;
        private final String name;
        private int quantity;
        private final String unit;


        public Inventory(int menuItemId, String name, int quantity, String unit) {
            this.id = menuItemId;
            this.quantity = quantity;
            this.name = name;
            this.unit = unit;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public String getName() {
            return name;
        }

        public int getQuantity() {
            return quantity;
        }

        public String getUnit() {
            return unit;
        }

        public int getId() {
            return id;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public static class Employee {
        private final int id;
        private final String name;
        private final String position;

        public Employee(int id, String name, String position) {
            this.id = id;
            this.name = name;
            this.position = position;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getPosition() {
            return position;
        }

        public boolean isManager() {
            return "Manager".equalsIgnoreCase(position);
        }

        @Override
        public String toString() {
            return name + " - " + position;
        }
    }

}
