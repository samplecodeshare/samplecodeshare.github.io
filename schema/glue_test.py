from pyspark.context import SparkContext
from awsglue.context import GlueContext
from awsglue.job import Job
from awsglue.dynamicframe import DynamicFrame
from pyspark.sql.types import StructType, StructField, IntegerType, StringType, BooleanType
import boto3

# Initialize Spark and Glue contexts
sc = SparkContext()
glueContext = GlueContext(sc)
spark = glueContext.spark_session
job = Job(glueContext)

# Hardcoded parameters
job_name = "example_glue_job"  # Your Glue job name
source_s3_path = "s3://your-source-bucket/sample.json"  # Replace with your source S3 path
target_s3_path = "s3://your-target-bucket/output/"  # Replace with your target S3 path
source_bucket = "your-source-bucket"  # Replace with your source bucket name
source_key = "sample.json"  # Replace with the key of your JSON file

# Initialize the job with the hardcoded job name
job.init(job_name, {})

# Define the schema for the JSON file
schema = StructType([
    StructField("id", IntegerType(), True),
    StructField("name", StringType(), True),
    StructField("age", IntegerType(), True),
    StructField("email", StringType(), True),
    StructField("is_active", BooleanType(), True)
])

# Function to print the raw JSON file before loading into DataFrame
def print_raw_json(bucket, key):
    s3 = boto3.client('s3')
    response = s3.get_object(Bucket=bucket, Key=key)
    content = response['Body'].read().decode('utf-8')
    print("Raw JSON content:")
    print(content)

try:
    # Print the raw JSON content from S3
    print_raw_json(source_bucket, source_key)

    # Read JSON files from the source S3 bucket with the defined schema
    print(f"Reading JSON files from: {source_s3_path}")
    df = spark.read.schema(schema).json(source_s3_path)
    
    # Check if the DataFrame is empty
    if df.rdd.isEmpty():
        raise ValueError("The DataFrame is empty after reading the JSON file. Please check the source file and schema.")

    # Print the schema of the DataFrame
    print("Schema of the DataFrame:")
    df.printSchema()  # This line prints the schema of the DataFrame

    # Show the DataFrame to verify data before writing
    print("Data read from JSON:")
    df.show()

    # Convert the DataFrame to DynamicFrame
    dynamic_frame = DynamicFrame.fromDF(df, glueContext, "dynamic_frame")

    # Write the DynamicFrame as a CSV file to the target S3 path
    print(f"Writing CSV files to: {target_s3_path}")
    glueContext.write_dynamic_frame.from_options(
        frame=dynamic_frame,
        connection_type="s3",
        connection_options={"path": target_s3_path},
        format="csv",
        format_options={"separator": ",", "quoteChar": '"'}
    )

    print("CSV file written successfully.")

except Exception as e:
    print(f"An error occurred: {str(e)}")

# Commit the job
job.commit()
