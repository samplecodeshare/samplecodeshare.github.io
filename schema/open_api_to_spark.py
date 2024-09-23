from pyspark.sql.types import StructType, StructField, StringType, IntegerType, FloatType, ArrayType, TimestampType

# Map OpenAPI data types to PySpark types
def openapi_to_spark_type(openapi_type, format=None):
    if openapi_type == "integer":
        return IntegerType()
    elif openapi_type == "number":
        if format == "float":
            return FloatType()
        # You can add more number formats here if needed
    elif openapi_type == "string":
        if format == "date-time":
            return TimestampType()
        return StringType()
    elif openapi_type == "array":
        return ArrayType(StringType())  # Assuming array of strings for simplicity; extend as needed
    # Add other types (boolean, object, etc.) if necessary
    return StringType()  # Default to StringType for unknown types

# Convert OpenAPI schema to Spark StructType
def openapi_to_struct_type(openapi_schema):
    fields = []
    for field_name, field_info in openapi_schema.get("properties", {}).items():
        field_type = field_info.get("type")
        field_format = field_info.get("format", None)
        spark_type = openapi_to_spark_type(field_type, field_format)
        fields.append(StructField(field_name, spark_type))
    return StructType(fields)

# Example OpenAPI schema
openapi_schema = {
    "type": "object",
    "properties": {
        "id": {
            "type": "integer",
            "format": "int64"
        },
        "name": {
            "type": "string"
        },
        "price": {
            "type": "number",
            "format": "float"
        },
        "tags": {
            "type": "array",
            "items": {
                "type": "string"
            }
        },
        "created_at": {
            "type": "string",
            "format": "date-time"
        }
    }
}

# Convert OpenAPI schema to Spark StructType
spark_schema = openapi_to_struct_type(openapi_schema)

# Output Spark schema
print(spark_schema)
