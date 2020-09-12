#!/bin/bash

javac *.java

./gensort -a 10000000 input1GB.txt
echo $'1GB input file generated\n' >> mysort1GB-1-small.log
echo "Run MySort Benchmark for 1GB :" >> mysort1GB-1-small.log
java -Xmx6g MySort 16 4 input1GB.txt output1GB.txt >> mysort1GB-1-small.log
echo $'\n' >> mysort1GB-1-small.log
echo "Run Valsort verification for sorted output file :" >> mysort1GB-1-small.log
./valsort output1GB.txt 2>> mysort1GB-1-small.log

./gensort -a 40000000 input4GB.txt
echo $'4GB input file generated\n' >> mysort4GB-1-small.log
echo "Run MySort Benchmark for 4GB :" >> mysort4GB-1-small.log
java -Xmx6g MySort 16 4 input4GB.txt output4GB.txt >> mysort4GB-1-small.log
echo $'\n' >> mysort4GB-1-small.log
echo "Run Valsort verification for sorted output file :" >> mysort4GB-1-small.log
./valsort output4GB.txt 2>> mysort4GB-1-small.log

./gensort -a 160000000 input16GB.txt
echo $'16GB input file generated\n' >> mysort16GB-1-small.log
echo "Run MySort Benchmark for 16GB :" >> mysort16GB-1-small.log
java -Xmx6g MySort 8 4 input16GB.txt output16GB.txt 1 >> mysort16GB-1-small.log
echo $'\n' >> mysort16GB-1-small.log
echo "Run Valsort verification for sorted output file :" >> mysort16GB-1-small.log
./valsort output16GB.txt 2>> mysort16GB-1-small.log



