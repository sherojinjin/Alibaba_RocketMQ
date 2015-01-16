#!/bin/sh
CLASSPATH=${CLASSPATH}:.:../lib/commons-cli-1.2.jar:../lib/commons-io-2.4.jar:../lib/derby-10.10.2.0.jar:../lib/fastjson-1.1.41.jar:../lib/javassist-3.7.ga.jar:../lib/logback-classic-1.0.13.jar:../lib/logback-core-1.0.13.jar:../lib/mysql-connector-java-5.1.31.jar:../lib/netty-all-4.0.21.Final.jar:../lib/rocketmq-broker-3.2.2.jar:../lib/rocketmq-client-3.2.2.jar:../lib/rocketmq-common-3.2.2.jar:../lib/rocketmq-example-3.2.2.jar:../lib/rocketmq-filtersrv-3.2.2.jar:../lib/rocketmq-namesrv-3.2.2.jar:../lib/rocketmq-remoting-3.2.2.jar:../lib/rocketmq-srvutil-3.2.2.jar:../lib/rocketmq-store-3.2.2.jar:../lib/rocketmq-tools-3.2.2.jar:../lib/slf4j-api-1.7.5.jar:../lib/httpclient-4.3.6.jar:../lib/httpcore-4.3.3.jar
ROCKETMQ_ENABLE_SSL=true
export CLASSPATH ROCKETMQ_ENABLE_SSL
