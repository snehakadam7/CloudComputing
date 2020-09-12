#!/bin/bash

mkdir tmp

cd tmp

wget http://www.iozone.org/src/current/iozone3_489.tar

tar xvf iozone3_489.tar

cd iozone3_489/src/current

make linux


