import threading
import pyodbc
import time

# Database connection settings
dsn = 'my_dsn_name'
uid = 'my_username'
pwd = 'my_password'

# Number of threads to run
num_threads = 10

# Function to perform database query
def query_database(thread_id):
    conn = pyodbc.connect('DSN=' + dsn + ';UID=' + uid + ';PWD=' + pwd)
    cursor = conn.cursor()
    try:
        cursor.execute("SELECT * FROM your_table_name")  # Replace with your query
        rows = cursor.fetchall()
        print(f"Thread {thread_id}: Retrieved {len(rows)} rows.")
    except Exception as e:
        print(f"Thread {thread_id}: Error - {e}")
    finally:
        cursor.close()
        conn.close()

# Function to create and start threads
def run_load_test():
    threads = []
    for i in range(num_threads):
        thread = threading.Thread(target=query_database, args=(i,))
        threads.append(thread)
        thread.start()

    # Wait for all threads to finish
    for thread in threads:
        thread.join()

if __name__ == "__main__":
    start_time = time.time()
    run_load_test()
    end_time = time.time()
    print(f"Load test completed in {end_time - start_time} seconds.")
