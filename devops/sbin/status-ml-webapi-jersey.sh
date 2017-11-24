#!/usr/bin/env bash

bin=`dirname "${BASH_SOURCE-$0}"`
pwd=`pwd`
current_dir=$pwd/$bin
bin_dir=$current_dir/../bin
var_dir=$current_dir/../var
root_dir=$current_dir/../..

[ -f $var_dir/ml-webapi-jersey.pid ] & echo `cat $var_dir/ml-webapi-jersey.pid` | echo "Process not running"
