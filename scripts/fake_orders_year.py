from faker import Faker
import psycopg2
from datetime import datetime, timedelta
import random

fake = Faker()

conn_params = {
    'dbname': 'csce315_902_02_db',
    'user': 'csce315_902_02_user',
    'password': 'password123',
    'host': 'csce-315-db.engr.tamu.edu',
    'port': '5432'
}

conn = psycopg2.connect(**conn_params)
cursor = conn.cursor()

cursor.execute("SELECT ID FROM Menu_Item;")
menu_items = [item[0] for item in cursor.fetchall()]

cursor.execute("SELECT ID FROM Employee;")
employees = [employee[0] for employee in cursor.fetchall()]

end_date = datetime.now()
start_date = end_date - timedelta(days=365)

customer_orders = []
order_items = []

for single_date in (start_date + timedelta(n) for n in range((end_date - start_date).days)):
    for _ in range(random.randint(350, 550)):
        customer_name = fake.name()
        employee_id = random.choice(employees)
        order_timestamp = datetime.combine(single_date, datetime.min.time()) + timedelta(hours=random.randint(8, 17), minutes=random.randint(0, 59))

        customer_orders.append((employee_id, order_timestamp, 'Completed', customer_name))

cursor.executemany("""
    INSERT INTO Customer_Order (Employee_ID, Created_At, Status, Name) 
    VALUES (%s, %s, %s, %s) RETURNING ID;
""", customer_orders)
order_ids = cursor.fetchall()

for order_id in order_ids:
    for _ in range(random.randint(1, 6)):
        menu_item_id = random.choice(menu_items)
        quantity = random.randint(1, 3)
        order_items.append((order_id[0], menu_item_id, quantity))

cursor.executemany("""
    INSERT INTO Order_Items (Order_ID, Menu_Item_ID, Quantity) 
    VALUES (%s, %s, %s);
""", order_items)

conn.commit()

cursor.close()
conn.close()

print("Fake orders have been generated and inserted into the database.")
