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

print("Fetching menu items and employees...")
cursor.execute("SELECT ID FROM Menu_Item;")
menu_items = [item[0] for item in cursor.fetchall()]

cursor.execute("SELECT ID FROM Employee;")
employees = [employee[0] for employee in cursor.fetchall()]

end_date = datetime.now()
start_date = end_date - timedelta(days=365)

print("Generating orders...")
order_count = 0

for single_date in (start_date + timedelta(n) for n in range((end_date - start_date).days)):
    cursor.execute("""
        SELECT COUNT(*) FROM Customer_Order 
        WHERE DATE(Created_At) = %s;
    """, (single_date,))
    if cursor.fetchone()[0] > 0:
        print(f"Orders already exist for {single_date.strftime('%Y-%m-%d')}. Skipping.")
        continue

    order_items = []
    daily_order_count = random.randint(350, 550)
    for _ in range(daily_order_count):
        customer_name = fake.name()
        employee_id = random.choice(employees)
        order_timestamp = datetime.combine(single_date, datetime.min.time()) + timedelta(hours=random.randint(8, 17),
                                                                                         minutes=random.randint(0, 59))

        cursor.execute("""
            INSERT INTO Customer_Order (Employee_ID, Created_At, Status, Name)
            VALUES (%s, %s, %s, %s) RETURNING ID;
        """, (employee_id, order_timestamp, 'Completed', customer_name))
        order_id = cursor.fetchone()[0]

        for _ in range(random.randint(1, 6)):
            menu_item_id = random.choice(menu_items)
            quantity = random.randint(1, 3)
            order_items.append((order_id, menu_item_id, quantity))

    # Insert order items in batches
    if order_items:
        cursor.executemany("""
            INSERT INTO Order_Items (Order_ID, Menu_Item_ID, Quantity) 
            VALUES (%s, %s, %s);
        """, order_items)

    order_count += daily_order_count
    conn.commit()
    print(f"Processed {single_date.strftime('%Y-%m-%d')}: {daily_order_count} orders.")

cursor.close()
conn.close()

print(f"Completed. Generated a total of {order_count} orders and inserted them into the database.")
