#!/bin/bash
# stop进程
server="ProducerServer"

if [ x"$1" != x ]; then
    server=$1
fi
echo "=================="$server" Stoping =============="
a=`ps aux | grep $server | grep -v grep`
if [ ! -z "$a" ];then
    echo $a | awk '{print $2}' | xargs kill -9
fi
echo "=================="$server" Stoped OK =============="
