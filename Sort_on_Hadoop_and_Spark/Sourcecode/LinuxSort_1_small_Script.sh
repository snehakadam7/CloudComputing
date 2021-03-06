#!/bin/bash

echo "Generating 1GB file..."
./gensort -a 10000000 1gbfile.txt
echo "Completed generating 1GB file."
echo "Linux sort for 1GB:" >> linsort1GB-1-small.log
echo "Starting linux sort..."
begin=$(date +%s)
sort --parallel=4 -o sorted1gb.txt 1gbfile.txt >> linsort1GB-1-small.log
end=$(date +%s)
echo "Completed 1GB sort!"
sortTime=$(expr $end - $begin)
echo "time taken = $sortTime sec" >> linsort1GB-1-small.log
echo $'\n' >> linsort1GB-1-small.log
echo "Verifying with valsort..."
echo "Valsort verification for sorted output file: " >> linsort1GB-1-small.log
./valsort sorted1gb.txt 2>> linsort1GB-1-small.log
echo "Completed 1GB."
rm 1gbfile.txt sorted1gb.txt
echo "Removed 1GB files"
echo 3 > /proc/sys/vm/drop_caches



echo "Generating 4GB file..."
./gensort -a 40000000 4gbfile.txt
echo "Completed generating 4GB file."
echo "Linux sort for 4GB:" >> linsort4GB-1-small.log
echo "Starting linux sort..."
begin=$(date +%s)
sort --parallel=12 -o sorted4gb.txt 4gbfile.txt >> linsort4GB-1-small.log
end=$(date +%s)
echo "Completed 4GB sort!"
sortTime=$(expr $end - $begin)
echo "time taken = $sortTime sec" >> linsort4GB-1-small.log
echo $'\n' >> linsort4GB-1-small.log
echo "Verifying with valsort..."
echo "Valsort verification for sorted output file: " >> linsort4GB-1-small.log
./valsort sorted4gb.txt 2>> linsort4GB-1-small.log
echo "Completed 4GB."
rm 4gbfile.txt sorted4gb.txt
echo "Removed 4GB files"
echo 3 > /proc/sys/vm/drop_caches



echo "Generating 16GB file..."
./gensort -a 160000000 16gbfile.txt
echo "Completed generating 16GB file."
echo "Linux sort for 16GB:" >> linsort16GB-1-small.log
echo "Starting linux sort..."
begin=$(date +%s)
sort --parallel=4 -o sorted16gb.txt 16gbfile.txt >> linsort16GB-1-small.log
end=$(date +%s)
echo "Completed 16GB sort!"
sortTime=$(expr $end - $begin)
echo "time taken = $sortTime sec" >> linsort16GB-1-small.log
echo $'\n' >> linsort16GB-1-small.log
echo "Verifying with valsort..."
echo "Valsort verification for sorted output file: " >> linsort16GB-1-small.log
./valsort sorted16gb.txt 2>> linsort16GB-1-small.log
echo "Completed 16GB."
rm 16gbfile.txt sorted16gb.txt
echo "Removed 16GB files"
echo 3 > /proc/sys/vm/drop_caches


