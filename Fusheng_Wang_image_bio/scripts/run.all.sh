#!/bin/bash
# nodes.txt file keeps the ip's of the nodes. 1 per each line
nodes=28
name="$nodes"n
nodefile=nodes.txt

./run.generic.sh 1 24 $name samplerun 1 g $nodes $nodefile 
