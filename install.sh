#!/bin/sh
git pull

if [ ! -f conf/private.key ]; then
  echo "Generating New SSL Key Pair";
  openssl genrsa -out conf/private.key 2048
  openssl rsa -in conf/private.key -pubout -out conf/cert.pub
  echo "Done"
fi

rm -rf target
rm -f devenv
if [ -z "$JAVA_HOME" ]; then
  JAVA_HOME=/opt/taobao/java
fi
export PATH=/opt/taobao/mvn/bin:$JAVA_HOME/bin:$PATH
mvn -Dmaven.test.skip=true clean package install assembly:assembly -U

ln -s target/alibaba-rocketmq-3.2.2/alibaba-rocketmq devenv
cp ${JAVA_HOME}/jre/lib/ext/sunjce_provider.jar devenv/lib/
