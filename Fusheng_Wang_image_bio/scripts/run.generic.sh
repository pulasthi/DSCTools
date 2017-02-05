#!/bin/bash

#cores per socket
cps=6
#sockets per node
spn=4

#number of memory maps per node -- the best is to keep at 1
mmaps=1
#memory map directory -- the best is to use tmpfs
mmapdir=/dev/shm/$USER


#---------------------------------------------------------------#
# The following content may not need to be changed. 
# A few things that may be of interest are
# 1. Java options
# Comments are given for these below
#---------------------------------------------------------------#

cp=$HOME/.m2/repository/com/google/guava/guava/15.0/guava-15.0.jar:$HOME/sali/software/jdk1.8.0/jre/../lib/tools.jar:$HOME/.m2/repository/commons-cli/commons-cli/1.2/commons-cli-1.2.jar:$HOME/.m2/repository/edu/indiana/soic/spidal/common/1.0/common-1.0.jar:$HOME/.m2/repository/habanero-java-lib/habanero-java-lib/0.1.4-SNAPSHOT/habanero-java-lib-0.1.4-SNAPSHOT.jar:$HOME/.m2/repository/net/java/dev/jna/jna/4.1.0/jna-4.1.0.jar:$HOME/.m2/repository/net/java/dev/jna/jna-platform/4.1.0/jna-platform-4.1.0.jar:$HOME/.m2/repository/net/openhft/affinity/3.0/affinity-3.0.jar:$HOME/.m2/repository/net/openhft/compiler/2.2.0/compiler-2.2.0.jar:$HOME/.m2/repository/net/openhft/lang/6.7.2/lang-6.7.2.jar:$HOME/.m2/repository/ompi/ompijavabinding/1.10.1/ompijavabinding-1.10.1.jar:$HOME/.m2/repository/org/kohsuke/jetbrains/annotations/9.0/annotations-9.0.jar:$HOME/.m2/repository/org/ow2/asm/asm/5.0.3/asm-5.0.3.jar:$HOME/.m2/repository/org/slf4j/slf4j-api/1.7.12/slf4j-api-1.7.12.jar:$HOME/.m2/repository/org/xerial/snappy/snappy-java/1.1.1.6/snappy-java-1.1.1.6.jar:$HOME/.m2/repository/org/pulasthi/dsctools/fusheng_wang_bio/1.0-SNAPSHOT/fusheng_wang_bio-1.0-SNAPSHOT-jar-with-dependencies.jar

wd=`pwd`
x='x'

cpn=$(($cps*$spn))

tpp=$1
ppn=$2
nodes=$7

commpat=$tpp$x$mmaps$x$nodes

memmultype=g
xmx=1
if [ "$5" ];then
    xmx=$5
    memmultype=$6
fi

bindToUnit=core
if [ $tpp -gt 1 ];then
    bindToUnit=none
fi

if [ $ppn -gt $cpn ];then
    bindToUnit=hwthread
fi

pat=$tpp$x$ppn$x$nodes
mkdir -p $pat

#Java options
opts="-XX:+UseG1GC -Xms256m -Xmx"$xmx"$memmultype"
infile=/N/u/pswickra/data/FushengWang/TCGA-83-5908-01Z-00-DX1.381c8f82-61a0-4e9d-982d-1ad0af7bead9/TCGA-83-5908-01Z-00-DX1.381c8f82-61a0-4e9d-982d-1ad0af7bead9.100k.random.data
outfile=/N/u/pswickra/data/FushengWang/TCGA-83-5908-01Z-00-DX1.381c8f82-61a0-4e9d-982d-1ad0af7bead9/TCGA-83-5908-01Z-00-DX1.381c8f82-61a0-4e9d-982d-1ad0af7bead9.100k.random.bin
numPoints=102655
dem=96
stats=false

echo "Running $pat on `date`" >> status.txt
$BUILD/bin/mpirun --hostfile $8 -np $(($nodes*$ppn)) java $opts -cp $cp org.pulasthi.dsctools.DistanceCalculation $infile $numPoints $dem $outfile $stats 2>&1 | tee $pat/$pat.$xmx.$memmultype.$4.$3.comm.$commpat.out.txt
echo "Finished $pat on `date`" >> status.txt

