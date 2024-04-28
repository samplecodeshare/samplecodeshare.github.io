import pandas as pd

def compare_parquet_files(file1_path, file2_path, output_file, num_rows=100, tolerance=0.001):
    # Load the Parquet files into DataFrames
    df1 = pd.read_parquet(file1_path)
    df2 = pd.read_parquet(file2_path)

    # Limit the comparison to the first 'num_rows' rows of each DataFrame
    df1 = df1.head(num_rows)
    df2 = df2.head(num_rows)

    # Create a DataFrame to store comparison results
    comparison_results = []

    # Compare the two DataFrames
    for col in df1.columns:
        for row_idx in range(num_rows):
            val1 = df1.loc[row_idx, col]
            val2 = df2.loc[row_idx, col]
            dtype1 = df1.dtypes[col]
            dtype2 = df2.dtypes[col]

            if pd.isna(val1) and pd.isna(val2):
                comparison_results.append([col, dtype1, dtype2, row_idx + 1, 'N/A', 'N/A', 'SAME'])
            elif pd.api.types.is_numeric_dtype(val1) and pd.api.types.is_numeric_dtype(val2):
                val1 = pd.to_numeric(val1, errors='coerce')
                val2 = pd.to_numeric(val2, errors='coerce')
                if abs(val1 - val2) <= tolerance:
                    comparison_results.append([col, dtype1, dtype2, row_idx + 1, val1, val2, 'CLOSE'])
                else:
                    comparison_results.append([col, dtype1, dtype2, row_idx + 1, val1, val2, 'DIFFERENT'])
            else:
                if val1 == val2:
                    comparison_results.append([col, dtype1, dtype2, row_idx + 1, val1, val2, 'SAME'])
                else:
                    comparison_results.append([col, dtype1, dtype2, row_idx + 1, val1, val2, 'DIFFERENT'])

    # Convert the comparison results to a DataFrame
    df_comparison = pd.DataFrame(comparison_results, columns=['Column', 'DataType1', 'DataType2', 'Row', 'Value1', 'Value2', 'Comparison'])

    # Write the comparison results to a CSV file with pipe delimiter
    df_comparison.to_csv(output_file, sep='|', index=False)

# Example usage
compare_parquet_files('file1.parquet', 'file2.parquet', 'comparison_output.csv', num_rows=100, tolerance=0.001)
