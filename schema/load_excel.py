import pandas as pd
import sqlite3

# Function to load Excel sheet into SQLite table
def load_excel_to_sqlite(excel_file, sheet_name, db_file, table_name):
    # Step 1: Read the Excel file
    df = pd.read_excel(excel_file, sheet_name=sheet_name)

    # Step 2: Connect to SQLite database (or create it)
    conn = sqlite3.connect(db_file)
    cursor = conn.cursor()

    # Step 3: Write the DataFrame to the SQLite table
    df.to_sql(table_name, conn, if_exists='replace', index=False)

    # Step 4: Commit changes and close the connection
    conn.commit()
    conn.close()

    print(f"Data from {excel_file} (sheet: {sheet_name}) loaded into {db_file} (table: {table_name})")

# Example usage
excel_file = 'data.xlsx'       # Path to your Excel file
sheet_name = 'Sheet1'          # Name of the sheet to load
db_file = 'database.db'        # Path to your SQLite database file
table_name = 'my_table'        # Name of the SQLite table to create

load_excel_to_sqlite(excel_file, sheet_name, db_file, table_name)
