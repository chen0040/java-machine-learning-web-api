#!/usr/bin/env bash

cd ..
cd ..

dirArray=( "SK-Utils" "SK-Statistics" "SK-DOM" "OP-Core" "OP-Search" "ML-Core" "ML-Tune" "ML-DataPreparation" "ML-Clustering" "ML-Trees" "ML-Bayes" "ML-TextRetrieval" "ML-TextMining")
for dirName in "${dirArray[@]}"
do
	echo $dirName
	cd $dirName

	jarPath="target/$dirName-0.0.1-SNAPSHOT-jar-with-dependencies.jar"

	if [ -d $jarPath ]; then
	    chmod 777 $jarPath
	fi

    mvn package




    mvn install:install-file -Dfile=$jarPath -DgroupId=com.github.chen0040 -DartifactId=$dirName -Dpackaging=jar -Dversion=0.0.1-SNAPSHOT


    cd ..
done

cd ML-TopicDifferentialServ
mvn package

cp  target/ML-TopicDifferentialServ-0.0.1-SNAPSHOT-jar-with-dependencies.jar devops/mltopicdiff.jar
chmod 777 devops/mltopicdiff.jar

