import sys
from awsglue.transforms import *
from awsglue.utils import getResolvedOptions
from pyspark.context import SparkContext
from awsglue.context import GlueContext
from awsglue.job import Job
from awsglue.dynamicframe import DynamicFrame
from pyspark.sql.types import StructType, StructField, IntegerType, StringType, BooleanType

# Retrieve job arguments
args = getResolvedOptions(sys.argv, ['JOB_NAME', 'SOURCE_S3_PATH', 'TARGET_S3_PATH'])

# Initialize Glue context and job
sc = SparkContext()
glueContext = GlueContext(sc)
spark = glueContext.spark_session
job = Job(glueContext)
job.init(args['JOB_NAME'], args)

# Define source and target S3 paths
source_s3_path = args['SOURCE_S3_PATH']  # e.g., 's3://your-source-bucket/sample.json'
target_s3_path = args['TARGET_S3_PATH']  # e.g., 's3://your-target-bucket/output/'

# Define the schema for the JSON file
schema = StructType([
    StructField("id", IntegerType(), True),
    StructField("name", StringType(), True),
    StructField("age", IntegerType(), True),
    StructField("email", StringType(), True),
    StructField("is_active", BooleanType(), True)
])

# Read JSON files from the source S3 bucket with schema
df = spark.read.schema(schema).json(source_s3_path)

# Convert the DataFrame to DynamicFrame
dynamic_frame = DynamicFrame.fromDF(df, glueContext, "dynamic_frame")

# Write the DynamicFrame as a Parquet file to the target S3 path
glueContext.write_dynamic_frame.from_options(
    frame=dynamic_frame,
    connection_type="s3",
    connection_options={"path": target_s3_path},
    format="parquet"
)

# Commit the job
job.commit()



# [
#   {
#     "id": 1,
#     "name": "Alice",
#     "age": 30,
#     "email": "alice@example.com",
#     "is_active": true
#   },
#   {
#     "id": 2,
#     "name": "Bob",
#     "age": 25,
#     "email": "bob@example.com",
#     "is_active": false
#   }
# ]
