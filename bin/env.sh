#!/bin/sh
CLASSPATH=${CLASSPATH}:.:../lib/commons-cli-1.2.jar:../lib/commons-io-2.4.jar:../lib/derby-10.10.2.0.jar:../lib/fastjson-1.1.41.jar:../lib/javassist-3.7.ga.jar:../lib/logback-classic-1.0.13.jar:../lib/logback-core-1.0.13.jar:../lib/mysql-connector-java-5.1.31.jar:../lib/netty-all-4.0.21.Final.jar:../lib/rocketmq-broker-3.1.9.jar:../lib/rocketmq-client-3.1.9.jar:../lib/rocketmq-common-3.1.9.jar:../lib/rocketmq-example-3.1.9.jar:../lib/rocketmq-filtersrv-3.1.9.jar:../lib/rocketmq-namesrv-3.1.9.jar:../lib/rocketmq-remoting-3.1.9.jar:../lib/rocketmq-srvutil-3.1.9.jar:../lib/rocketmq-store-3.1.9.jar:../lib/rocketmq-tools-3.1.9.jar:../lib/slf4j-api-1.7.5.jar:../lib/netty-tcnative-1.1.30.Fork1-linux-x86_64.jar
ROCKETMQ_ENABLE_SSL=true
export CLASSPATH ROCKETMQ_ENABLE_SSL
