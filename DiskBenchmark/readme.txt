ReadMe
Homework 3 has been completed and uploaded in the following directory structure.

** Sourcecode: This directory contains 5 files
 
Makefile: This file is used for compiling C code.

MyDiskBenchmark.c: This file takes user commands and is used to do different types of workloads on 10GB of data with multiple threads and multiple buffers and return the throughput (in MBps) and the time taken to complete the operation (in sec).
 
Disk_Script.sh: This file is used to automatically run all the experiments at once without user commands.
 	
          iozone_setup.sh:  This file is used to automatically set up an IOZone benchmark in your system.
 
iozone.sh: This file has commands to run all the experiments on IOZone benchmark.

* MyDiskBenchmark.c
This programs expects 4 arguments:
Arg1(Workload): Write Sequential (WS), Read Sequential (RS), Write Random(WR), Read Random(RR).  This expects a string of characters; any input of either WS (or) RS (or) WR (or) RR.
Arg 2(Threads): This expects an integer value between the values (1/2/4/8/12/24/48) .
Arg 3(Record Size): This expects a buffer size. Any value from the values (4KB/64KB/1MB/16MB).
Arg 4(Dataset): This is the total dataset size to work on. ‘10GB’ data for record size of 64KB, 1MB, 16MB. ‘1GB’ for a record size of 4KB.
 
To build this file, makefile should be executed using below command.

make

The above command will generate an out file in the directory - MyDiskBenchmark

Clear the Disk Cache by running below command before every observation with sudo privileges

echo 3 > /proc/sys/vm/drop_caches

To measure throughput and time taken for any workload run, navigate to this directory and run below command.
 
./<O/PFileName> <Arg1> <Arg2> <Arg3> <Arg4>
Example1: .myDiskBenchmark WS 4 1MB 10GB
Example2: .myDiskBenchmark WR 8 4KB 1GB
 
The above example1 will run over 10GB of data to give the throughput and the time taken by 4 threads to Write Sequential data with a record size of 1MB.
Output: The .c file creates 4 files of size 2.5GB, files names being D4_File1.bin, D4_File2.bin, D4_File3.bin, D4_File4.bin 
Similarly, example2 will run over 1GB of dataset to give the throughput, IOPS by 8 threads to Write Random data with a record size of 4KB.
 Output: The .c file creates 8 files of size 1.25GB, files names being D8_File1.bin, D8_File2.bin, D8_File3.bin,..., D8_File7.bin, D8_File8.bin 
 *Disk_Script.sh
This is a script file which is used to run all the workloads for all threads for all record sizes.
 
To run the bash file, the below command can be entered.
./Disk_Script.sh
 
*iozone_setup.sh
	This script file is automated to install IOZone benchmark on system
 
*iozone.sh
This script file is automated to run all the workloads for all threads for all record sizes for IOZone.
   
** Documentation:
This directory contains source files, Readme text and hw3-report.pdf which describes the overall assignment.
