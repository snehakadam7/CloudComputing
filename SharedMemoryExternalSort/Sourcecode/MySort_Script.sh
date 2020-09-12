#!/bin/bash

cd tmp/64

javac *.java

./gensort -a 10000000 input1GB.txt
echo $'1GB input file generated\n' >> mysort1GB.log
echo "Run MySort Benchmark for 1GB :" >> mysort1GB.log
java -Xmx8g MySort 46 46 input1GB.txt output1GB.txt >> mysort1GB.log
echo $'\n' >> mysort1GB.log
echo "Run Valsort verification for sorted output file :" >> mysort1GB.log
./valsort output1GB.txt 2>> mysort1GB.log

./gensort -a 40000000 input4GB.txt
echo $'4GB input file generated\n' >> mysort4GB.log
echo "Run MySort Benchmark for 4GB :" >> mysort4GB.log
java -Xmx8g MySort 46 46 input4GB.txt output4GB.txt >> mysort4GB.log
echo $'\n' >> mysort4GB.log
echo "Run Valsort verification for sorted output file :" >> mysort4GB.log
./valsort output4GB.txt 2>> mysort4GB.log

./gensort -a 160000000 input16GB.txt
echo $'16GB input file generated\n' >> mysort16GB.log
echo "Run MySort Benchmark for 16GB :" >> mysort16GB.log
java -Xmx8g MySort 5 40 input16GB.txt output16GB.txt 2 >> mysort16GB.log
echo $'\n' >> mysort16GB.log
echo "Run Valsort verification for sorted output file :" >> mysort16GB.log
./valsort output16GB.txt 2>> mysort16GB.log

./gensort -a 640000000 input64GB.txt
echo $'64GB input file generated\n' >> mysort64GB.log
echo "Run MySort Benchmark for 64GB :" >> mysort64GB.log
java -Xmx8g MySort 46 46 input64GB.txt output64GB.txt 1 >> mysort64GB.log
echo $'\n' >> mysort64GB.log
echo "Run Valsort verification for sorted output file :" >> mysort64GB.log
./valsort output64GB.txt 2>> mysort64GB.log

