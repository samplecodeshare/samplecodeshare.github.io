import pandas as pd
import concurrent.futures

def write_chunk_to_csv(chunk_tuple):
    chunk, csv_path = chunk_tuple
    try:
        chunk.to_csv(csv_path, index=False)
        print(f"Chunk written to CSV at {csv_path}")
    except Exception as e:
        print(f"Error writing chunk to CSV at {csv_path}: {str(e)}")

def split_and_write_df_to_csv_parallel(df, csv_paths, num_threads):
    # Split the DataFrame into chunks
    chunks = [(df_chunk, csv_paths[i % len(csv_paths)]) for i, df_chunk in enumerate(np.array_split(df, len(csv_paths)))]

    # Write each chunk to a separate CSV file in parallel
    with concurrent.futures.ThreadPoolExecutor(max_workers=num_threads) as executor:
        futures = [executor.submit(write_chunk_to_csv, chunk) for chunk in chunks]
        for future in concurrent.futures.as_completed(futures):
            # Process the result of each task if needed
            pass

# Example usage: Replace 'your_df' with your DataFrame, 'your_csv_paths' with a list of CSV file paths,
# and 'num_threads' with the number of threads to use for parallel execution
your_df = pd.DataFrame({'col1': [1, 2, 3, 4, 5, 6, 7, 8, 9, 10], 'col2': ['a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j']})
your_csv_paths = ['file1.csv', 'file2.csv', 'file3.csv']
num_threads = 3

split_and_write_df_to_csv_parallel(your_df, your_csv_paths, num_threads)
