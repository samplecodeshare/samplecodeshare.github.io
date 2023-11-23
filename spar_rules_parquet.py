from pyspark.sql import SparkSession
from pyspark.sql.functions import col
from py4j.java_gateway import java_import

# Initialize Spark session
spark = SparkSession.builder.appName("DroolsPySparkIntegration").getOrCreate()

# Define UDF to evaluate Drools rules
def evaluate_rules(age, salary):
    # Load Drools rules from CSV file
    rules_csv_path = "path/to/rules.csv"
    rules_df = spark.read.option("header", "true").csv(rules_csv_path)

    # Sort rules by priority in descending order
    rules_df = rules_df.orderBy(col("priority").desc())

    # Initialize Drools
    java_import(spark._jvm, "org.drools.core.rule.Drools")
    drools = spark._jvm.Drools()

    # Evaluate each rule and return the corresponding action
    for rule in rules_df.collect():
        if drools.evaluateRule(rule["condition"], {"age": age, "salary": salary}):
            return rule["action"]

    return "unknown"

# Define a UDF for PySpark
evaluate_rules_udf = spark.udf.register("evaluate_rules", evaluate_rules)

# Read data from Parquet file
parquet_input_path = "path/to/parquet"
data = spark.read.parquet(parquet_input_path)

# Apply Drools rules using the UDF
result_df = data.withColumn("action", evaluate_rules_udf(col("age"), col("salary")))

# Show the result
result_df.show()

# Stop the Spark session
spark.stop()
