#!/usr/bin/env bash

bin=`dirname "${BASH_SOURCE-$0}"`
pwd=`pwd`
current_dir=$pwd/$bin
bin_dir=$current_dir/../bin
root_dir=$current_dir/../..

cd $root_dir

mvn clean package

cd $pwd

projs=( "ml-webapi-jersey" "ml-webapi-spring" )

for proj in "${projs[@]}"
do
    cp $root_dir/ml-webapps/$proj/target/$proj.jar $bin_dir/$proj.jar
done



