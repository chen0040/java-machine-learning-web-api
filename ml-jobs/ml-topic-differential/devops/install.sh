#!/bin/bash
cp data/*.* /tmp/
[ -d /opt/mltopicdiff ] || mkdir /opt/mltopicdiff
cp -i mltopicdiff.jar /opt/mltopicdiff/
cp -i mltopicdiffserv /etc/init.d/
chmod +x /etc/init.d/mltopicdiffserv

iptables -I INPUT 5 -i eth0 -p tcp --dport 9091 -m state --state NEW,ESTABLISHED -j ACCEPT

### run the jar server     => sudo service mltopicdiffserv start ###
### stop the jar server    => sudo service mltopicdiffserv stop ###
### restart the jar server => sudo service mltopicdiffserv restart ###



