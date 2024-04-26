import pyarrow.parquet as pq

def convert_parquet_to_csv(parquet_file, csv_file):
    # Read Parquet file into a DataFrame
    df = pd.read_parquet(parquet_file)

    # Write DataFrame to CSV
    df.to_csv(csv_file, index=False)

def compare_parquet_files(file1, file2):
    # Read Parquet files into pyarrow Tables
    table1 = pq.read_table(file1)
    table2 = pq.read_table(file2)

    # Extract column names and data types
    schema1 = table1.schema
    schema2 = table2.schema

    columns1 = [(field.name, field.type) for field in schema1]
    columns2 = [(field.name, field.type) for field in schema2]

    # Compare columns
    common_columns = set([col[0] for col in columns1]).intersection(set([col[0] for col in columns2]))
    differing_columns = set([col[0] for col in columns1]).symmetric_difference(set([col[0] for col in columns2]))

    print("Common columns with the same data types:")
    for column in common_columns:
        type1 = [col[1] for col in columns1 if col[0] == column][0]
        type2 = [col[1] for col in columns2 if col[0] == column][0]
        if type1.equals(type2):
            print(f"Column '{column}': {type1}")
    print("\nCommon columns with different data types:")
    for column in common_columns:
        type1 = [col[1] for col in columns1 if col[0] == column][0]
        type2 = [col[1] for col in columns2 if col[0] == column][0]
        if not type1.equals(type2):
            print(f"Column '{column}':")
            print(f"  - File 1 data type: {type1}")
            print(f"  - File 2 data type: {type2}")

    print("\nDifferent columns:", differing_columns)

# Example usage
file1 = "file1.parquet"
file2 = "file2.parquet"
compare_parquet_files(file1, file2)

# Example usage
parquet_file = "input.parquet"
csv_file = "output.csv"
convert_parquet_to_csv(parquet_file, csv_file)
