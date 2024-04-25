import pandas as pd
import pyarrow.parquet as pq
import pyarrow as pa

# Mapping from Parquet data types to Pandas data types
parquet_to_pandas_types = {
    pa.bool_(): 'bool',
    pa.int8(): 'int8',
    pa.uint8(): 'uint8',
    pa.int16(): 'int16',
    pa.uint16(): 'uint16',
    pa.int32(): 'int32',
    pa.uint32(): 'uint32',
    pa.int64(): 'int64',
    pa.uint64(): 'uint64',
    pa.float16(): 'float16',
    pa.float32(): 'float32',
    pa.float64(): 'float64',
    pa.string(): 'object',
    pa.binary(): 'bytes',
    pa.date32(): 'datetime64[ns]',
    pa.date64(): 'datetime64[ns]',
    pa.timestamp('ns'): 'datetime64[ns]',
    # Add more mappings as needed
}

def convert_to_parquet_type(series, column_mappings):
    # Get the target Parquet type for the current column
    parquet_type = column_mappings.get(series.name, pa.string())
    return parquet_type

def create_parquet_files(dataframe, column_mappings):
    for column in dataframe.columns:
        # Convert column data to target Parquet type
        parquet_type = convert_to_parquet_type(dataframe[column], column_mappings)
        
        # Convert Pandas dtype to match the Parquet data type
        pandas_dtype = parquet_to_pandas_types.get(parquet_type, 'object')
        dataframe[column] = dataframe[column].astype(pandas_dtype)
        
        # Convert the DataFrame to PyArrow Table
        table = pa.Table.from_pandas(dataframe[[column]], schema=pa.schema([(column, parquet_type)]))
        
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
   
