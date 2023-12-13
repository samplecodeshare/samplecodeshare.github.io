from pyspark.sql import SparkSession
from pyspark.sql.functions import col

# Create a Spark session
spark = SparkSession.builder.appName("ParquetCoalesceExample").getOrCreate()

# Read Parquet files into a DataFrame
parquet_file_path = "path/to/your/parquet/files"
df = spark.read.parquet(parquet_file_path)

# Show the original DataFrame
print("Original DataFrame:")
df.show()

# Coalesce the DataFrame before writing to Parquet files
coalesced_df = df.coalesce(1)  # Change the argument to the desired number of partitions

# Write the coalesced DataFrame into Parquet files
output_path = "path/to/your/output"
coalesced_df.write.mode("overwrite").parquet(output_path)

# Show the result
print("Data written to Parquet files with coalescing:")
result_df = spark.read.parquet(output_path)
result_df.show()

# Stop the Spark session
spark.stop()
