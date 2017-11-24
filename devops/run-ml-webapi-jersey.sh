#!/usr/bin/env bash

bin=`dirname "${BASH_SOURCE-$0}"`
pwd=`pwd`
current_dir=$pwd/$bin
devops_dir=$current_dir
bin_dir=$devops_dir/bin
var_dir=$devops_dir/var
log_dir=$devops_dir/logs

if [ -f $bin_dir/ml-webapi-jersey.jar ]; then
    if [ -f $var_dir/ml-webapi-jersey.pid ]; then
        echo "Kill the previous version of the ml server currently running"
        kill -9 `cat $var_dir/ml-webapi-jersey.pid`
        rm -f $var_dir/ml-webapi-jersey.pid
    fi
    echo "Starting the server at localhost:8900"
    nohup java -jar $bin_dir/ml-webapi-jersey.jar >/dev/null 2>$log_dir/ml-webapi-jersey.err &
    echo $! > $var_dir/ml-webapi-jersey.pid
    echo "Website is now running at localhost:8900"
    echo "To check whether the server is running, run this command \"curl localhost:8900\""
fi


