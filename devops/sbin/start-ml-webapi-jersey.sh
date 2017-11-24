#!/usr/bin/env bash

bin=`dirname "${BASH_SOURCE-$0}"`
pwd=`pwd`
current_dir=$pwd/$bin
bin_dir=$current_dir/../bin
var_dir=$current_dir/../var
log_dir=$current_dir/../logs
root_dir=$current_dir/../..

nohup java -jar $bin_dir/ml-webapi-jersey.jar >/dev/null 2>$log_dir/ml-webapi-jersey.err &
echo $! > $var_dir/ml-webapi-jersey.pid
