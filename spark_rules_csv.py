# condition_1,condition_2,priority,action
# age > 25,salary > 50000,1,approve
# age <= 25,salary <= 50000,2,reject

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
        conditions = rule.drop(["priority", "action"]).items()
        if all(drools.evaluateRule(f"{col} {condition}", {"age": age, "salary": salary}) for col, condition in conditions):
            return rule["action"]

    return "unknown"

# Define a UDF for PySpark
evaluate_rules_udf = spark.udf.register("evaluate_rules", evaluate_rules)

# Sample data for demonstration
data = [(30, 60000), (25, 45000), (35, 70000)]
columns = ["age", "salary"]

# Create a DataFrame from the sample data
sample_data_df = spark.createDataFrame(data, columns)

# Apply Drools rules using the UDF
result_df = sample_data_df.withColumn("action", evaluate_rules_udf(col("age"), col("salary")))

# Show the result
result_df.show()

# Stop the Spark session
spark.stop()
