import psycopg2

try:
    conn = psycopg2.connect(
        dbname='csce315_902_02_db',
        user='csce315_902_02_user',
        password='password123',
        host='csce-315-db.engr.tamu.edu',
        port='5432'
    )
    print("Connected to the database.")
    cursor = conn.cursor()

    cursor.execute("""
    CREATE TABLE Inventory (
        ID SERIAL PRIMARY KEY,
        Name VARCHAR(255) NOT NULL,
        Quantity INTEGER NOT NULL,
        Unit VARCHAR(50) NOT NULL
    );
    """)

    cursor.execute("""
    CREATE TABLE Menu_Item (
        ID SERIAL PRIMARY KEY,
        Name VARCHAR(255) NOT NULL,
        Price DECIMAL(10, 2) NOT NULL,
        Calories INTEGER NOT NULL,
        Category VARCHAR(255) NOT NULL
    );
    """)

    cursor.execute("""
    CREATE TABLE Customer (
        ID SERIAL PRIMARY KEY,
        Name VARCHAR(255) NOT NULL,
        Phone VARCHAR(50) NOT NULL
    );
    """)

    cursor.execute("""
    CREATE TABLE Employee (
        ID SERIAL PRIMARY KEY,
        Name VARCHAR(255) NOT NULL,
        Position VARCHAR(255) NOT NULL
    );
    """)

    cursor.execute("""
    CREATE TABLE Recipe (
        ID SERIAL PRIMARY KEY,
        Menu_item INTEGER REFERENCES Menu_Item(ID),
        Inventory_item INTEGER REFERENCES Inventory(ID),
        Qty INTEGER NOT NULL
    );
    """)

    cursor.execute("""
    CREATE TABLE Customer_Order (
        ID SERIAL PRIMARY KEY,
        Employee_ID INTEGER REFERENCES Employee(ID),
        Customer_ID INTEGER REFERENCES Customer(ID),
        Created_At TIMESTAMP NOT NULL,
        Status VARCHAR(50) NOT NULL,
        Name VARCHAR(255) NOT NULL
    );
    """)

    cursor.execute("""
    CREATE TABLE Order_Items (
        ID SERIAL PRIMARY KEY,
        Order_ID INTEGER REFERENCES Customer_Order(ID),
        Menu_Item_ID INTEGER REFERENCES Menu_Item(ID),
        Quantity INTEGER NOT NULL CHECK (Quantity > 0)
    );
    """)

    conn.commit()
    conn.close()

except Exception as e:
    print("Unable to connect to the database.")
    print(e)

