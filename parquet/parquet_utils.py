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


def compare_parquet_files_1(file1_path, file2_path, num_rows=100):
    # Load the Parquet files into DataFrames
    df1 = pd.read_parquet(file1_path)
    df2 = pd.read_parquet(file2_path)

    # Get the number of rows in each file
    num_rows_df1 = len(df1)
    num_rows_df2 = len(df2)

    # Limit the comparison to the first 'num_rows' rows of each DataFrame
    df1 = df1.head(num_rows)
    df2 = df2.head(num_rows)

    # Display the number of rows in each file
    print(f"Number of rows in {file1_path}: {num_rows_df1}")
    print(f"Number of rows in {file2_path}: {num_rows_df2}")
    print()

    # Get column names and data types
    columns_df1 = df1.columns
    columns_df2 = df2.columns
    dtypes_df1 = df1.dtypes
    dtypes_df2 = df2.dtypes

    # Compare column names and data types
    for col in columns_df1:
        print(f"Column: {col}")
        print(f"  Data type: File 1 - {dtypes_df1[col]}, File 2 - {dtypes_df2[col]}")
        print("  Values:")
        for row_idx in range(num_rows):
            val1 = df1.loc[row_idx, col]
            val2 = df2.loc[row_idx, col]
            if val1 != val2:
                print(f"    Row {row_idx + 1}: File 1 - {val1}, File 2 - {val2} (DIFFERENT)")
            else:
                print(f"    Row {row_idx + 1}: File 1 - {val1}, File 2 - {val2}")
        print()

# Example usage
compare_parquet_files('file1.parquet', 'file2.parquet', num_rows=100)
