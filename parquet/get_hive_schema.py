import pyarrow as pa
import pyodbc

def get_hive_table_schema(database, table):
    # Connect to Hive using pyodbc
    conn = pyodbc.connect('DSN=hive_connection', autocommit=True)

    # Get cursor
    cursor = conn.cursor()

    # Execute query to fetch table schema
    cursor.execute(f"DESCRIBE {database}.{table}")

    # Fetch schema
    hive_schema = cursor.fetchall()

    # Close connection
    cursor.close()
    conn.close()

    # Map Hive data types to PyArrow data types
    pyarrow_type_map = {
        'boolean': pa.bool_(),
        'tinyint': pa.int8(),
        'smallint': pa.int16(),
        'int': pa.int32(),
        'bigint': pa.int64(),
        'float': pa.float32(),
        'double': pa.float64(),
        'decimal': pa.decimal128(18, 9),  # Adjust precision and scale as needed
        'string': pa.string(),
        'char': pa.string(),  # Treat char type as string
        'varchar': pa.string(),  # Treat varchar type as string
        'binary': pa.binary(),
        'timestamp': pa.timestamp('ns'),  # Adjust time unit as needed
        'date': pa.date32(),  # Adjust time unit as needed
        'array': pa.list_(pa.string()),  # Adjust inner type as needed
        'map': pa.map_(pa.string(), pa.string()),  # Adjust key and value types as needed
        'struct': pa.struct([]),  # Adjust fields as needed
        'uniontype': pa.null(),  # Treat union type as null
        'void': pa.null(),  # Treat void type as null
    }

    # Convert Hive schema to PyArrow schema
    fields = []
    for col_name, data_type, _, _ in hive_schema:
        pyarrow_type = pyarrow_type_map.get(data_type.lower(), pa.string())
        fields.append((col_name, pyarrow_type))

    return pa.schema(fields)

# Example usage
hive_database = 'your_database'
hive_table = 'your_table'
arrow_schema = get_hive_table_schema(hive_database, hive_table)
print(arrow_schema)
