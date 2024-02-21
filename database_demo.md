Query 1
This finds the day of most revenue from the customer_order table.
```postgresql
SELECT 
    DATE(Created_At) as OrderDate,
    SUM(mi.Price * oi.Quantity) as TotalRevenue
FROM 
    Customer_Order co
INNER JOIN 
    Order_Items oi ON co.ID = oi.Order_ID
INNER JOIN 
    Menu_Item mi ON oi.Menu_Item_ID = mi.ID
GROUP BY 
    OrderDate
ORDER BY 
    TotalRevenue DESC
LIMIT 1;
```

Query 2
This finds the number of orders per week from the customer_order table.
```postgresql
SELECT 
  EXTRACT(WEEK FROM Created_At) AS week_number,
  COUNT(*) AS order_count
FROM 
  Customer_Order
WHERE 
  Created_At >= CURRENT_DATE - INTERVAL '1 year'
GROUP BY 
  week_number
ORDER BY 
  week_number;
```

Query 3
This finds the number of orders per hour from the customer_order table.
```postgresql
SELECT
  EXTRACT(HOUR FROM co.Created_At) AS order_hour,
  COUNT(DISTINCT co.ID) AS order_count,
  SUM(oi.Quantity * mi.Price) AS total_sales
FROM
  Customer_Order co
INNER JOIN
  Order_Items oi ON co.ID = oi.Order_ID
INNER JOIN
  Menu_Item mi ON oi.Menu_Item_ID = mi.ID
GROUP BY
  order_hour
ORDER BY
  order_hour;
```

Query 4
This finds the top 10 days with the most sales from the customer_order table
```postgresql
SELECT
  DATE(co.Created_At) AS order_day,
  SUM(oi.Quantity * mi.Price) AS total_sales
FROM
  Customer_Order co
JOIN
  Order_Items oi ON co.ID = oi.Order_ID
JOIN
  Menu_Item mi ON oi.Menu_Item_ID = mi.ID
GROUP BY
  order_day
ORDER BY
  total_sales DESC
LIMIT 10;
```

Query 5
This command lists the top 20 menu items by the number of different inventory ingredients they use, showing the ones with the most ingredients first.
```postgressql
SELECT
  mi.Name AS menu_item_name,
  COUNT(DISTINCT r.Inventory_item) AS inventory_item_count
FROM
  Menu_Item mi
JOIN
  Recipe r ON mi.ID = r.Menu_item
JOIN
  Inventory i ON r.Inventory_item = i.ID
GROUP BY
  mi.Name
ORDER BY
  inventory_item_count DESC
LIMIT 20;
```

Query 6
Finding the average values of menu items
```postgresql
SELECT 
  AVG(Calories) as avg_calories, 
  AVG(Price) as avg_price
FROM 
  Menu_Item;
```

Query 7
Counting the number of orders in the database
```postgresql
SELECT COUNT(*) FROM Customer_Order;
```

Query 8
Total revenue made
```postgresql
SELECT SUM(oi.Quantity * mi.Price) AS TotalRevenue
FROM Order_Items oi
JOIN Menu_Item mi ON oi.Menu_Item_ID = mi.ID;
```

Query 9
Week-by-week revenue
```postgresql
SELECT 
  EXTRACT(WEEK FROM co.Created_At) AS OrderWeek,
  SUM(oi.Quantity * mi.Price) AS TotalRevenue
FROM 
  Order_Items oi
JOIN 
  Menu_Item mi ON oi.Menu_Item_ID = mi.ID
JOIN 
  Customer_Order co ON oi.Order_ID = co.ID
GROUP BY 
  OrderWeek
ORDER BY 
  OrderWeek;
```

Query 10
Employees ranked by Orders Placed
```postgresql
SELECT e.Name, COUNT(co.ID) AS OrdersHandled
FROM Employee e
JOIN Customer_Order co ON e.ID = co.Employee_ID
GROUP BY e.Name
ORDER BY OrdersHandled DESC;
```

Query 11
Employees ranked by Revenue Made
```postgresql
SELECT 
  e.Name, 
  SUM(oi.Quantity * mi.Price) AS revenue_made
FROM 
  Employee e 
  JOIN 
    Customer_Order co ON e.ID = co.Employee_ID
JOIN 
  Order_Items oi ON oi.Order_ID = co.ID
JOIN 
  Menu_Item mi ON oi.Menu_Item_ID = mi.ID
GROUP BY 
  e.Name
ORDER BY 
  revenue_made DESC;
```

Query 12
Most Popular Menu Items by Quantity Sold
```postgresql
SELECT mi.Name, SUM(oi.Quantity) AS TotalSold
FROM Menu_Item mi
JOIN Order_Items oi ON mi.ID = oi.Menu_Item_ID
GROUP BY mi.Name
ORDER BY TotalSold DESC
LIMIT 10;
```

Query 13
Average Order Value
```postgresql
SELECT AVG(oi.Quantity * mi.Price) AS AverageOrderValue
FROM Order_Items oi
JOIN Menu_Item mi ON oi.Menu_Item_ID = mi.ID;
```

Query 14
List Employees in the system
```postgresql
SELECT * FROM employee;
```

Query 15
Order of most used inventory item based on recipe
```postgresql
SELECT 
  i.Name, 
  SUM(r.qty) AS inventory_used
FROM
  Menu_Item mi
JOIN
  Recipe r ON mi.ID = r.Menu_item
JOIN
  Inventory i ON r.Inventory_item = i.ID
GROUP BY 
  i.Name
ORDER BY 
  inventory_used DESC;
```

Query 16
Displays unit of measurement for inventory items
```postgresql
SELECT name, unit FROM inventory;
```

Query 17
Price to buy all menu items at once
```postgresql
SELECT SUM(mi.price) as price FROM Menu_item mi;
```

Query 18
Getting low stock items
```postgresql
SELECT name, quantity FROM inventory WHERE quantity < 30;
```

