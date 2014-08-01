#!bin/bash

nodesNr=`preserve -llist | grep $USER | awk '{print NF}' | sed -n '1,1p'`
let "nodesNr -= 8"
index=9
nodename=`preserve -llist | grep $USER | awk -v col=$index '{print $col}' | sed -n '1,1p'`
echo $nodename

mkdir -p /var/scratch/yongguo/output/bfs


echo "--- Copy DotaLeague_FCF ---"
./client.sh dfs -D dfs.block.size=22882304 -copyFromLocal /var/scratch/yongguo/sc_dataset/DotaLeague_FCF /local/hadoop.tmp.yongguo/DotaLeague_FCF

for i in 1 2 3 4 5 6 7 8 9 10
do
	echo "--- Run $i Stats for DotaLeague_FCF ---"
	bash bfs.sh ../biggraph.jar eu.stratosphere.pact.example.biggraph.BreadthFirstSearch 30 hdfs://$nodename.cm.cluster:54310/local/hadoop.tmp.yongguo/DotaLeague_FCF hdfs://$nodename.cm.cluster:54310/local/hadoop.tmp.yongguo/output_$i\_DotaLeague_FCF 0 u DotaLeague_FCF_30 $i
	echo "--- Copy output ---"
	#./client.sh dfs -copyToLocal /local/hadoop.tmp.yongguo/output_$i\_DotaLeague_FCF /var/scratch/yongguo/output/bfs/
	echo "--- Clear dfs ---"
	./client.sh dfs -rmr /local/hadoop.tmp.yongguo/output_$i\_DotaLeague_FCF
done
./client.sh dfs -rm /local/hadoop.tmp.yongguo/DotaLeague_FCF
echo "--- DotaLeague_FCF DONE ---"

