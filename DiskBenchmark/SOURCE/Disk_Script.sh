#!/bin/bash

CC=gcc
EXEC=a.out
SRC=MyDiskBenchmark.c

$CC $SRC -lpthread

declare -a workload=("WS" "RS" "WR" "RR")

for wl in "${workload[@]}"
do
for threads in 1 2 4 8 12 24 48
do
for block in 64KB 1MB 16MB
do
for data in 10GB
do
	echo "Running Workload $wl for $threads threads with block size $block"
RES=`./$EXEC $wl $threads $block $data`

echo "$wl $threads $block :" "$RES" >> THROUGHPUT_RESULTS.txt

done
done
done
done

declare -a workload1=("WR" "RR")


for wl1 in "${workload1[@]}"
do
for threads1 in 1 2 4 8 12 24 48
do
for block1 in 4KB
do
for data1 in 10GB
do
	echo "Running Workload $wl1 for $threads1 threads with block size $block1"
RES1=`./$EXEC $wl1 $threads1 $block1 $data1`

echo "$wl1 $threads1 $block1 :" "$RES1" >> IOPS_RESULTS.txt

done
done
done
done

