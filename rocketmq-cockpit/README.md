RocketMQ Cockpit
============================================

##Purpose

It's a RocketMQ Cockpit.

##Prerequisite
1. JDK 1.7+ installed, `JAVA_HOME` properly set, `PATH` updated.
2. Latest version of Maven installed, `M2_HOME` set and `PATH` updated.
3. Git


##Deployment Guide

Assuming at home folder.

1. Clone the source.

    `git clone https://github.com/lizhanhui/RocketMQ-Cockpit.git`

2. Package war.

    `cd RocketMQ-Cockpit`

    `mvn package`

3. Download jetty.

    `wget http://download.eclipse.org/jetty/stable-9/dist/jetty-distribution-9.2.5.v20141112.zip`

4. Unzip jetty `unzip jetty-distribution-9.2.5.v20141112.zip -d ~/jetty`

5. Copy war and configuration files to jetty `webapps` folder.

    `cp ~/RocketMQ-Cockpit/target/rocketmq-cockpit-3.2.2.war ~/jetty/webapps`

    `cp ~/RocketMQ-Cockpit/doc/rocketmq-cockpit-3.2.2.xml ~/jetty/webapps`

    `cp ~/RocketMQ-Cockpit/doc/start.sh ~/jetty`

    `cd ~/jetty`

    `chmod +x start.sh`

    `sh start.sh`
