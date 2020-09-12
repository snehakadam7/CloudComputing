#!/bin/bash

cd tmp

cd iozone3_394/src/current

./iozone -t 1 -s 10000000 -i 0 -i 1 -i 2 -r 64 -T -I
./iozone -t 1 -s 10000000 -i 0 -i 1 -i 2 -r 1000 -T -I
./iozone -t 1 -s 10000000 -i 0 -i 1 -i 2 -r 16000 -T -I

./iozone -t 2 -s 5000000 -i 0 -i 1 -i 2 -r 64 -T -I
./iozone -t 2 -s 5000000 -i 0 -i 1 -i 2 -r 1000 -T -I
./iozone -t 2 -s 5000000 -i 0 -i 1 -i 2 -r 16000 -T -I

./iozone -t 4 -s 2500000 -i 0 -i 1 -i 2 -r 64 -T -I
./iozone -t 4 -s 2500000 -i 0 -i 1 -i 2 -r 1000 -T -I
./iozone -t 4 -s 2500000 -i 0 -i 1 -i 2 -r 16000 -T -I

./iozone -t 8 -s 1250000 -i 0 -i 1 -i 2 -r 64 -T -I
./iozone -t 8 -s 1250000 -i 0 -i 1 -i 2 -r 1000 -T -I
./iozone -t 8 -s 1250000 -i 0 -i 1 -i 2 -r 16000 -T -I

./iozone -t 12 -s 833330 -i 0 -i 1 -i 2 -r 64 -T -I
./iozone -t 12 -s 833330 -i 0 -i 1 -i 2 -r 1000 -T -I
./iozone -t 12 -s 833330 -i 0 -i 1 -i 2 -r 16000 -T -I

./iozone -t 24 -s 416670 -i 0 -i 1 -i 2 -r 64 -T -I
./iozone -t 24 -s 416670 -i 0 -i 1 -i 2 -r 1000 -T -I
./iozone -t 24 -s 416670 -i 0 -i 1 -i 2 -r 16000 -T -I

./iozone -t 48 -s 208330 -i 0 -i 1 -i 2 -r 64 -T -I
./iozone -t 48 -s 208330 -i 0 -i 1 -i 2 -r 1000 -T -I
./iozone -t 48 -s 208330 -i 0 -i 1 -i 2 -r 16000 -T -I


#### IOPS

./iozone -t 1 -s 1000000 -i 0 -i 2 -r 4 -T -I -O

./iozone -t 2 -s 500000 -i 0 -i 2 -r 4 -T -I -O

./iozone -t 4 -s 250000 -i 0 -i 2 -r 4 -T -I -O

./iozone -t 8 -s 125000 -i 0 -i 2 -r 4 -T -I -O

./iozone -t 12 -s 83333 -i 0 -i 2 -r 4 -T -I -O

./iozone -t 24 -s 41667 -i 0 -i 2 -r 4 -T -I -O

./iozone -t 48 -s 20833 -i 0 -i 2 -r 4 -T -I -O

