Homework 5 has been completed and uploaded in the following directory structure.

** Sourcecode: This directory contains 8 files
 
1)Makefile: This file is used for compiling java code.

2) 4 java files:-  MySort.java, ReadInputFile.java, WriteOutputFile.java, MergeSort.java.
MySort.java: This file takes user commands and is used to perform sort (in-memory & external memory) on  different types of datasets of 1GB, 4GB, 16GB, 64GB of data with multiple threads for I/O operations and for sorting and returns a sorted file with time taken taken to complete the entire operation. 
We will be limiting the memory usage of the application to 8GB.

Note: You must install jdk before running java code using following code:
Sudo apt install default-jdk

First, we will compile using following command:
javac MySort.java ReadInputFile.java WriteOutputFile.java MergeSort.java

Second, we will run MySort benchmark
Input Arguments: For in-memory sort:-
Sort_thread_Count, I/O_threadCount, Input_file, Output_file

We can run MySort benchmark using following format
Java -Xmx8g MySort Sort_thread_Count, I/O_threadCount, Input_file, Output_file
Note: -Xmx8g is used to limit memory to 8GB.

Example:
java -Xmx8g MySort 46 46 input1GB.txt output1GB.txt
This will perform in-memory sort for file input1GB.txt using 46 threads for sorting, 46 threads for I/O and store sorted output in file output1GB.txt. 
Similarly, you can perform in-memory sort for 4GB input.

Input Arguments: For external sort:-
Sort_thread_Count, I/O_threadCount, Input_file, Output_file, chunk_size

Example:
java -Xmx8g MySort 5 40 input16GB.txt output16GB.txt 2
This will perform external sort for file input16GB.txt using 5 threads used for sorting, 40 threads used for I/O, chunk_size of 2 and store sorted output in file output16GB.txt. 
Similarly, you can perform external sort for 64GB input.

Once the output files are generated, you can verify that file is correctly sorted using Valsort utility.
Example:
./valsort output1GB.txt

3)gensort_setup.sh:  This file is used to set up gensort and valsort utility. The gensort program can be used to generate input records of any data size:
Example:
./gensort -a 10 input.txt
This will generate a file of 1000 bytes in ascii format. Each row in the file is 100 bytes.

4)MySort_Script.sh: This bash script performs following task:
→ generate input file
→ Run MySort benchmark. Store the result in a log file.
→ Run Valsort for sorted output file. Store the result in a log file.
→ The script will perform all above operations for 1GB, 4GB, 16GB and 64GB.

5) LinuxSort.sh: Linux sort has to be done for 4 datasets - 1GB, 4GB, 16GB, 64GB and this is done by  using the basic sort command. A bash script (LinuxSort.sh) is written to do the sort operation in an automated manner one after the other for all the 4 datasets mentioned. 

Example: sort  -S<limit_memory_size> --parallel=<thread_count> -o <output_file> <input_file>
 sort -S 8G --parallel=24 -o sorted.txt input.txt

The memory usage can be limited with -S command and giving the buffer size. The # of threads to use for sorting can be mentioned using the --parallel command. The sorted content can be written to an output file using the -o command. Finally, the generated output file can be validated using the valsort mentioned before.

The Bash script does the following tasks:
→ Generates the input file of 1GB data using gensort.
→ Runs the sort command and generates the sorted output file and prints the time taken to the .log file.
→ Verifies the output file using valsort and prints the result to the .log file.
→ Removes the generated input and output files and clears the cache.


** Output: This directory contains 8 output files of the source code execution for MySort and Linux Sort.
mysort1GB.log, mysort4GB.log, mysort16GB.log, mysort64GB.log → It contains logs from the application as well as valsort. It clearly shows the completion of the sort invocations with clear timing information and experiment details. 

linsort1GB.log, linsort4GB.log, linsort16GB.log, linsort64GB.log → It contains logs that shows the completion of the linux sort with clear timing information and valsort verification.

** Documentation:
This directory contains HW5-report.pdf which describes the overall assignment.
