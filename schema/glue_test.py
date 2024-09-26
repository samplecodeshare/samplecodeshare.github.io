import json
from pyspark import SparkContext
from pyspark.streaming import StreamingContext
from pyspark.sql.types import *
from pyspark.streaming.kinesis import KinesisUtils
from pyspark.streaming.flume import FlumeUtils
from pyspark.sql.functions import *

# Create a SparkContext
sc = SparkContext("local[2]", "S3Stream")

# Create a StreamingContext
ssc = StreamingContext(sc, 10)

# Create a DStream from S3
s3Stream = S3Source.inputStream(ssc, "s3://your-bucket/your-prefix")

# Define the schema for the data in S3
schema = StructType([
    StructField("field1", StringType()),
    # ... other fields
])

# Convert DStream to DataFrame
df = s3Stream.map(lambda x: json.loads(x)).toDF(schema)

# Process the DataFrame
df.show(truncate=False)

# Start the streaming context
ssc.start()
ssc.awaitTermination()

# {
#     "field1": "value1",
#     "field2": 123,
#     "field3": [
#         "item1",
#         "item2"
#     ]
# }
