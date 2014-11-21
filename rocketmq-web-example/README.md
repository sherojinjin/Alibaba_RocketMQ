RocketMQ Web Example
======================
#Usage
##To Run This Example Web App
1. Edit `src/main/java/webapp/WEB-INF/applicationContext.xml` to match your name server address, topic name etc.
2. At this module folder, execute the following command `mvn jetty:run`.
3. It will start and begin to consume messages.
4. For now, it just prints out the messages consumes. You may work on
`src/main/java/com/ndpmedia/rocketmq/web/message/listener/DefaultMessageListener.java` to do something meaningful.

#Contribute
###Please create Bug Ticket if you find something wrong.