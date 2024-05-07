import pandas as pd
import pyarrow.csv as pv
import pyarrow.hdfs as hdfs

def write_df_to_hdfs(df, hdfs_path):
    try:
        # Convert the DataFrame to a PyArrow Table
        table = pv.write_csv(df)

        # Write the PyArrow Table to HDFS
        with hdfs.connect() as fs:
            with fs.open(hdfs_path, 'wb') as f:
                table.write_csv(f)

        print(f"DataFrame written to HDFS at {hdfs_path}")

    except Exception as e:
        print("Error:", str(e))


# Example usage: Replace 'your_df' with your DataFrame and 'your_hdfs_path' with your desired HDFS path
your_df = pd.DataFrame({'col1': [1, 2, 3], 'col2': ['a', 'b', 'c']})
your_hdfs_path = '/user/hive/warehouse/your_file.csv'

write_df_to_hdfs(your_df, your_hdfs_path)
