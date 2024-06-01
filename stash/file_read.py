import os

def find_files_with_extension(directory, extension):
    matching_files = []
    for root, dirs, files in os.walk(directory):
        for file in files:
            if file.endswith(extension):
                matching_files.append(os.path.join(root, file))
    return matching_files

def read_file_contents(file_path):
    with open(file_path, 'r', encoding='utf-8') as file:
        return file.read()

if __name__ == "__main__":
    directory = 'path/to/your/directory'  # Replace with your directory path
    extension = '.txt'  # Replace with the desired file extension

    matching_files = find_files_with_extension(directory, extension)
    
    for file in matching_files:
        print(f"File: {file}")
        contents = read_file_contents(file)
        print("Contents:")
        print(contents)
        print("\n" + "-"*80 + "\n")
