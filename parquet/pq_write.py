import pandas as pd
import pyarrow.parquet as pq
import pyarrow as pa

def convert_to_parquet_type(series, column_mappings):
    # Get the target Parquet type for the current column
    parquet_type = column_mappings.get(series.name, pa.string())
    return parquet_type

def create_parquet_files(dataframe, column_mappings):
    for column in dataframe.columns:
        # Convert column data to target Parquet type
        parquet_type = convert_to_parquet_type(dataframe[column], column_mappings)
        
        # Create a DataFrame with just the current column
        column_df = pd.DataFrame(dataframe[column])
        
        # Convert the DataFrame to PyArrow Table
        table = pa.Table.from_pandas(column_df, schema=pa.schema([(column, parquet_type)]))
        
        # Write the Table to a Parquet file
        file_name = f"{column}.parquet"
        with pq.ParquetWriter(file_name, table.schema) as writer:
            writer.write_table(table)
        
        print(f"Parquet file '{file_name}' created successfully.")

# Read data from CSV file
def read_data_from_csv(csv_file):
    df = pd.read_csv(csv_file)
    return df

# Example usage
if __name__ == "__main__":
    # Read data from CSV file (replace 'data.csv' with your CSV file path)
    csv_file = 'data.csv'  # Replace 'data.csv' with your CSV file path
    df = read_data_from_csv(csv_file)
    
    # Define column mappings (replace with your specific mappings)
    column_mappings = {
        "Column1": pa.int64(),
        "Column2": pa.string(),
        "Column3": pa.float64()
        # Add more mappings as needed
    }
    
    # Call the function to create Parquet files for each column
    create_parquet_files(df, column_mappings)
