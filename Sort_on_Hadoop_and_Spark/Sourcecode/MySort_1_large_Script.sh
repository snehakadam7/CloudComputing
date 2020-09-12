#!/bin/bash

javac *.java

./gensort -a 10000000 input1GB.txt
echo $'1GB input file generated\n' >> mysort1GB-1-large.log
echo "Run MySort Benchmark for 1GB :" >> mysort1GB-1-large.log
java -Xmx27g MySort 20 16 input1GB.txt output1GB.txt >> mysort1GB-1-large.log
echo $'\n' >> mysort1GB-1-large.log
echo "Run Valsort verification for sorted output file :" >> mysort1GB-1-large.log
./valsort output1GB.txt 2>> mysort1GB-1-large.log

./gensort -a 40000000 input4GB.txt
echo $'4GB input file generated\n' >> mysort4GB-1-large.log
echo "Run MySort Benchmark for 4GB :" >> mysort4GB-1-large.log
java -Xmx27g MySort 16 16 input4GB.txt output4GB.txt >> mysort4GB-1-large.log
echo $'\n' >> mysort4GB-1-large.log
echo "Run Valsort verification for sorted output file :" >> mysort4GB-1-large.log
./valsort output4GB.txt 2>> mysort4GB-1-large.log

./gensort -a 160000000 input16GB.txt
echo $'16GB input file generated\n' >> mysort16GB-1-large.log
echo "Run MySort Benchmark for 16GB :" >> mysort16GB-1-large.log
java -Xmx27g MySort 32 16 input16GB.txt output16GB.txt >> mysort16GB-1-large.log
echo $'\n' >> mysort16GB-1-large.log
echo "Run Valsort verification for sorted output file :" >> mysort16GB-1-large.log
./valsort output16GB.txt 2>> mysort16GB-1-large.log

./gensort -a 240000000 input24GB.txt
echo $'24GB input file generated\n' >> mysort24GB-1-large.log
echo "Run MySort Benchmark for 24GB :" >> mysort24GB-1-large.log
java -Xmx27g MySort 16 10 input24GB.txt output24GB.txt 2 >> mysort24GB-1-large.log
echo $'\n' >> mysort24GB-1-large.log
echo "Run Valsort verification for sorted output file :" >> mysort24GB-1-large.log
./valsort output24GB.txt 2>> mysort24GB-1-large.log

