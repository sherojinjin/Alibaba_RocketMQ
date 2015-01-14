namespace py com.ndpmedia.rocketmq.babel
namespace php com.ndpmedia.rocketmq.babel
namespace cpp com.ndpmedia.rocketmq.babel
namespace java com.ndpmedia.rocketmq.babel

struct Message {
   1: required string topic,
   2: optional i32 flag = 0,
   3: optional map<string, string> properties,
   4: required binary data,
}

service Producer {
   void send(1: Message message),

   void batchSend(1: list<Message> messageList)

   oneway void stop()
}