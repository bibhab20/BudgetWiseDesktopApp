#!/bin/bash

work_dir="/Users/bibhabpattnayak/Downloads"  # Define your work directory here
dest_dir="/Users/bibhabpattnayak/Documents/Transactions/Midfirst/"

cd "$work_dir" || { echo "Directory not found"; exit 1; }

# List files starting with "transaction"
files=$(find . -maxdepth 1 -type f -name "transaction*.csv")

for file in $files; do
    # Find the line number where "<Date>" appears
    line_number=$(grep -n '<Date>' "$file" | cut -d ':' -f 1)

    # Increment line number to read the next line after "<Date>"
    next_line=$((line_number + 1))

    # Read the date in dd/mm/yyyy format
    date_line=$(sed -n "${next_line}p" "$file")
    # Extract the date in mm/dd/yyyy format
    date=$(echo "$date_line" | grep -oE '[0-9]{2}/[0-9]{2}/[0-9]{4}' | head -n 1)

    echo "Date line $date_line"
    echo "Date  $date"
    # Extract month and year
    month=$(date -d "$date" "+%b")
    year=$(date -d "$date" "+%Y")

    # Extract month and year from the date in dd/mm/yyyy format
    IFS='/' read -r -a date_parts <<< "$date"
    month=${date_parts[0]}
    year=${date_parts[2]}
    echo "$month"
    echo "$year"


    # Rename the file to MMM-YYYY.csv format
    new_filename="${month}-${year}.csv"
    #mv "$file" "$dest_dir$new_filename"
    echo "Renamed $file to $new_filename and moved it to $dest_dir"
done
