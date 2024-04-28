import pandas as pd

def compare_parquet_files(file1_path, file2_path, num_rows=100):
    # Load the Parquet files into DataFrames
    df1 = pd.read_parquet(file1_path)
    df2 = pd.read_parquet(file2_path)

    # Iterate through the first 'num_rows' rows of both DataFrames simultaneously
    for index, (row1, row2) in enumerate(zip(df1.head(num_rows).iterrows(), df2.head(num_rows).iterrows())):
        index1, data1 = row1
        index2, data2 = row2

        # Compare the values in corresponding rows
        if not data1.equals(data2):
            print(f"Differences found in row {index}:")
            print(f"File 1: {data1.values}")
            print(f"File 2: {data2.values}")

# Example usage
compare_parquet_files('file1.parquet', 'file2.parquet', num_rows=100)
