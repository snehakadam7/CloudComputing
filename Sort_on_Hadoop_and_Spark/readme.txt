Homework 6 has been completed and uploaded in the following directory structure.

** Sourcecode: This directory contains 12 files

1)Makefile: This file is used for compiling java code for MySort benchmark.

2)gensort_setup.sh:  This file is used to set up gensort and valsort utility. 
The gensort program can be used to generate input records of any data size:
Example:
./gensort -a 10 input.txt
This will generate a file of 1000 bytes in ascii format. Each row in the file is 100 bytes.

3)MySort_1_small_Script.sh
The bash performs following tasks:
→ generate input file
→ Run MySort benchmark. Store the result in a log file.
→ Run Valsort for sorted output file. Store the result in a log file.
→ The script will perform all above operations for 1GB, 4GB, 16GB on 1 small instance.

4)MySort_1_large_Script.sh
The bash performs following tasks:
→ generate input file
→ Run MySort benchmark. Store the result in a log file.
→ Run Valsort for sorted output file. Store the result in a log file.
→ The script will perform all above operations for 1GB, 4GB, 16GB and 24GB on 1 large instance.

5)LinuxSort_1_small_Script.sh
Linux sort has to be done on 1 small instance for 3 datasets - 1GB, 4GB, 16GB and this is done by  using the basic sort command. 
A bash script (LinuxSort_1_small_Script.sh) is written to do the sort operation in an automated manner one after the other for 
all the 3 datasets mentioned. 

Example: sort   --parallel=<thread_count> -o <output_file> <input_file>
 sort  --parallel=8 -o sorted.txt input.txt

The # of threads to use for sorting can be mentioned using the --parallel command. The sorted content can be written to an output file 
using the -o command. Finally, the generated output file can be validated using the valsort mentioned before.

The Bash script does the following tasks:
→ Generates the input file  using gensort.
→ Runs the sort command and generates the sorted output file and prints the time taken to the .log file.
→ Verifies the output file using valsort and prints the result to the .log file.
→ Removes the generated input and output files and clears the cache.
→ The script will perform all above operations for 1GB, 4GB, 16GB on 1 small instance.


6)LinuxSort_1_large_Script.sh
Linux sort has to be done on 1 large instance for 4 datasets - 1GB, 4GB, 16GB, 24GB and this is done by  using the basic sort command.
A bash script (LinuxSort_1_large_Script.sh) is written to do the sort operation in an automated manner one after the other for all 
the 4 datasets mentioned. 

Example: sort   --parallel=<thread_count> -o <output_file> <input_file>
 sort  --parallel=8 -o sorted.txt input.txt

The # of threads to use for sorting can be mentioned using the --parallel command. 
The sorted content can be written to an output file using the -o command. 
Finally, the generated output file can be validated using the valsort mentioned before.

The Bash script does the following tasks:
→ Generates the input file of 1GB data using gensort.
→ Runs the sort command and generates the sorted output file and prints the time taken to the .log file.
→ Verifies the output file using valsort and prints the result to the .log file.
→ Removes the generated input and output files and clears the cache.
→ The script will perform all above operations for 1GB, 4GB, 16GB and 24GB on 1 large instance.

7)Java code files: 6 Java code file:- 
4 java code files for SharedMemory(My Sort benchmark) program.
1 java code file for HadoopSort program.
1 java code file for SparkSort program.

** Output: This directory contains log files of the source code execution for MySort, Linux Sort, Hadoop Sort and Spark Sort.
It clearly shows the completion of the sort invocations with clear timing information and experiment details.

** Documentation:
This directory contains HW6-report.pdf which describes the overall assignment.

******************************************
Shared Memory Sort (My Sort):
Files:-  MySort.java, ReadInputFile.java, WriteOutputFile.java, MergeSort.java.

MySort.java: This file takes user commands and is used to perform sort (in-memory & external memory) on  different 
types of datasets with multiple threads for I/O operations and for sorting and returns a sorted file with time taken 
to complete the entire operation. 

Note: You must install jdk before running java code using following code:
Sudo apt install default-jdk

Step to compile and execute MySort on virtual cluster: 
1.Generate input file using gensort
2.First, we will compile using following command:
javac MySort.java ReadInputFile.java WriteOutputFile.java MergeSort.java
3.Second, we will run MySort benchmark

Input Arguments: For in-memory sort:-
Sort_thread_Count, I/O_threadCount, Input_file, Output_file

We can run MySort benchmark using following format
Java MySort Sort_thread_Count, I/O_threadCount, Input_file, Output_file

Example:
java MySort 46 46 input1GB.txt output1GB.txt
This will perform in-memory sort for file input1GB.txt using 46 threads for sorting, 46 threads for I/O and store sorted output
in file output1GB.txt. 
Similarly, you can perform in-memory sort for 4GB input.

Input Arguments: For external sort:-
Sort_thread_Count, I/O_threadCount, Input_file, Output_file, chunk_size

Example:
java MySort 5 40 input16GB.txt output16GB.txt 2
This will perform external sort for file input16GB.txt using 5 threads used for sorting, 40 threads used for I/O, chunk_size of 2GB 
and store sorted output in file output16GB.txt. 
Similarly, you can perform external sort for 64GB input.

4.Once the output files are generated, you can verify that file is correctly sorted using Valsort utility.
Example:
./valsort output1GB.txt

Linux Sort:

Step to compile and execute Linux Sort on virtual cluster: 
1.Generate the input file of 1GB data using gensort.
2.Run sort command and generates the sorted output file and prints the time taken.
Example: sort   --parallel=<thread_count> -o <output_file> <input_file>
 sort  --parallel=8 -o sorted.txt input.txt
3.Verify the output file using valsort.

Hadoop Sort:
Files: HadoopSort.java

Step to compile and execute Hadoop Sort on virtual cluster: 
1.Create input directory
$ cd /exports/projects/hadoop-3.2.1/
$ bin/hadoop fs -mkdir /home
$ bin/hadoop fs -mkdir /home/input
2.Generate input file using gensort.
./gensort -a 10000000 input1GB.txt → this will generate a file of 1GB size.
3.Move input file to HDFS input directory created.
$ bin/hadoop fs -put input1GB.txt /home/input/
4.Compile and create jar
$ export HADOOP_CLASSPATH=${JAVA_HOME}/lib/tools.jar
$ bin/hadoop com.sun.tools.javac.Main HadoopSort.java
$ jar cf HadoopSort.jar HadoopSort*.class
5.Run code using the following command.
$ bin/hadoop jar wc.jar HadoopSort /home/input/ /home/output 2
→ This will take input file input1GB.txt from input directory location /home/input/, run sort application with number of reducers=2 
and store result at /home/output/ 
6.Sorted output files can be found at the following path:
 	$ bin/hadoop fs -ls /home/output/
7.Get output files from HDFS.
 	$ bin/hadoop fs -get /home/output/ output
8.Concatenate all output files into 1 file. 
 	$ cat part-r-00000 part-r-00001  >> output.txt
9.Verify if the output file is correctly sorted using valsort.
$ ./valsort output.txt

Spark Sort:
Files: SparkKeySort.java

Step to compile and execute Spark Sort on virtual cluster: 
1.Create an input file using gensort command.
Example: ./gensort -a 10000000 1gbfile.txt
2.Move the input file (1gbfile.txt) to the input directory of hadoop file system using the put command.
$ bin/hadoop fs -put  /exports/projects/64/1gbfile.txt  /home/input/.
3.Update the input path of the input file in SparkKeySort.java file.
4.Update the output path in SparkSort.java file where the output folder must be created in hadoop file system.
5.Make sure the output directory where the file needs to be saved is removed from the hadoop file system,  since the Spark sort will 
create an output directory and place the sorted files in it.
6.Run the below command to compile the .java file. To create .class files.
$ javac -cp jars/spark-core_2.12-3.0.0-preview2.jar:jars/scala-library-2.12.10.jar SparkKeySort.java
7.Create a .jar file for all the .class files created from the above step.
$ jar cvf SparkKeySort.jar SparkKeySort*.class
8.Execute the spark -submit command from where the .jar file is saved. This command sorts the input file and creates an output folder with sorted files in it.
$ bin/spark-submit --class SparkKeySort --master yarn --deploy-mode cluster --driver-memory 4g --executor-memory 2g --executor-cores 1 SparkKeySort.jar
9.Move the output file to the local directory and to run the valsort command
$ bin/hadoop fs -get /home/output/data.out/part-00000 /exports/projects/64/.
10.Run the valsort command to verify the output file
$ ./valsort part-00000
