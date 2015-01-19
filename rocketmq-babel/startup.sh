#!/bin/sh
#JAVA_HOME="/usr/java/jdk1.6.0_31"
APP_LOG=logs
if [ ! -d "$APP_LOG" ]; then
  mkdir $APP_LOG
fi
JAVA_OPTS="-Duser.timezone=GMT+8 -server -Xms512m -Xmx512m -Xloggc:logs/gc.log -Denable_ssl=true -Drocketmq.namesrv.domain=172.30.50.54 "
APP_MAIN=com.ndpmedia.rocketmq.babel.$1
CLASSPATH=target/classes
echo $CLASSPATH
#JAVA_HOME="/Library/Java/JavaVirtualMachines/jdk1.7.0_71.jdk/Contents/Home"
nohup $JAVA_HOME/bin/java $JAVA_OPTS -Dworkdir=./  -cp target/classes:`find $PROJECT_PATH/target/lib -name "*.jar" -printf "%p:"` $APP_MAIN > $APP_LOG/nohup.log &