from pyspark.sql import SparkSession

# Initialize Spark session
spark = SparkSession.builder \
    .appName("ReadFromSqlServerWriteToParquet") \
    .getOrCreate()

# Define the SQL Server connection properties
jdbc_url = "jdbc:sqlserver://your_server:1433;databaseName=your_database"
db_properties = {
    "user": "your_username",
    "password": "your_password",
    "driver": "com.microsoft.sqlserver.jdbc.SQLServerDriver"
}

# Define the SQL query to select data from the table
table_name = "your_table"
sql_query = f"(SELECT * FROM {table_name}) AS table_alias"

# Read data from SQL Server table
df = spark.read \
    .format("jdbc") \
    .option("url", jdbc_url) \
    .option("dbtable", sql_query) \
    .option("user", db_properties["user"]) \
    .option("password", db_properties["password"]) \
    .option("driver", db_properties["driver"]) \
    .load()

# Display the schema and sample data
df.printSchema()
df.show()

# Write data to Parquet file
parquet_output_path = "path/to/output/parquet"
df.write.parquet(parquet_output_path, mode="overwrite")

# Stop the Spark session
spark.stop()
