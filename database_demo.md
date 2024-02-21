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

Finding the Average
```postgresql
SELECT AVG(Calories) FROM Menu_Item;
```

Counting the number of orders
```postgresql
SELECT COUNT(*) FROM Customer_Order;
```

Total revenue
```postgresql
SELECT SUM(oi.Quantity * mi.Price) AS TotalRevenue
FROM Order_Items oi
JOIN Menu_Item mi ON oi.Menu_Item_ID = mi.ID;
```


Weekly revenue
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

Employees by Orders Placed
```postgresql
SELECT e.Name, COUNT(o.ID) AS OrdersHandled
FROM Employee e
JOIN Customer_Order o ON e.ID = o.Employee_ID
GROUP BY e.Name
ORDER BY OrdersHandled DESC;
```

Most Popular Menu Items by Quantity Sold
```postgresql
SELECT mi.Name, SUM(oi.Quantity) AS TotalSold
FROM Menu_Item mi
JOIN Order_Items oi ON mi.ID = oi.Menu_Item_ID
GROUP BY mi.Name
ORDER BY TotalSold DESC
LIMIT 10;
```

Average Order Value
```postgresql
SELECT AVG(oi.Quantity * mi.Price) AS AverageOrderValue
FROM Order_Items oi
JOIN Menu_Item mi ON oi.Menu_Item_ID = mi.ID;
```

List Employees
```postgresql
SELECT * FROM employee;
```