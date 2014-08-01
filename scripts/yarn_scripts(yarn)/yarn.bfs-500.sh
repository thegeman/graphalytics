#!/bin/bash

############################################################################################
# each dataset is copied in a way to have exactly 20 blocks, thus fully utilize 20 wrokers (10mappers n 10reducers) #
############################################################################################
./client.sh fs -mkdir -p /local/hadoop.tmp.yongguo

echo "--- Copy graph500_FCF ---"
#./client.sh dfs -copyFromLocal /var/scratch/yongguo/input/filtered/graph500_FCF /local/hadoop.tmp.yongguo
./client.sh dfs -D dfs.block.size=50535424 -copyFromLocal /var/scratch/yongguo/sc_dataset/graph500_FCF /local/hadoop.tmp.yongguo/graph500_FCF
mkdir -p /var/scratch/yongguo/output/yarn_bfs/output_graph500_FCF

for i in 1 2 3 4 5 6 7 8 9 10
do
    echo "--- Run $i Stats for graph500_FCF ---"
    ./client.sh jar /home/yongguo/hadoopJobs/hadoopJobs.jar org.hadoop.test.jobs.BFSJob undirected filtered false false 20 20 /local/hadoop.tmp.yongguo/graph500_FCF /local/hadoop.tmp.yongguo/output_$i\_graph500_FCF 287144
    echo "--- Copy output ---"
    ./client.sh dfs -copyToLocal /local/hadoop.tmp.yongguo/output_$i\_graph500_FCF/benchmark.txt /var/scratch/yongguo/output/yarn_bfs/output_graph500_FCF/benchmark_$i
    echo "--- Clear dfs ---"
    ./client.sh dfs -rmr /local/hadoop.tmp.yongguo/output_$i\_graph500_FCF
    rm -rf /var/scratch/${USER}/yarn_logs/userlogs
done

./client.sh dfs -rm /local/hadoop.tmp.yongguo/graph500_FCF
echo "--- graph500_FCF DONE ---"

