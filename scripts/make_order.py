import psycopg2
import datetime

try:
    conn = psycopg2.connect(
        dbname='csce315_902_02_db',
        user='csce315_902_02_user',
        password='password123',
        host='csce-315-db.engr.tamu.edu',
        port='5432'
    )
    cursor = conn.cursor()

    cursor.execute("""
        INSERT INTO Customer (Name, Phone) VALUES (%s, %s) RETURNING ID
    """, ('Karlos Z', '123-456-7890'))
    customer_id = cursor.fetchone()[0]

    cursor.execute("""
        INSERT INTO Employee (Name, Position) VALUES (%s, %s) RETURNING ID
    """, ('Gemma', 'Cashier'))
    employee_id = cursor.fetchone()[0]

    menu_item_id = 1

    cursor.execute("""
        INSERT INTO Customer_Order (Employee_ID, Customer_ID, Created_At, Status, Name) VALUES (%s, %s, %s, %s, %s) RETURNING ID
    """, (employee_id, customer_id, datetime.datetime.now(), 'Complete', 'Order for Karlos Z'))
    order_id = cursor.fetchone()[0]

    cursor.execute("""
        INSERT INTO Order_Items (Order_ID, Menu_Item_ID, Quantity) VALUES (%s, %s, %s)
    """, (order_id, menu_item_id, 1))

    conn.commit()
    print("Customer, employee, and customer order with items have been successfully added to the database.")
except Exception as e:
    print("An error occurred:", e)
finally:
    cursor.close()
    conn.close()
