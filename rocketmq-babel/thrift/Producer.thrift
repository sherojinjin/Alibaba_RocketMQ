namespace py com.ndpmedia.rocketmq.babel
namespace php com.ndpmedia.rocketmq.babel
namespace cpp com.ndpmedia.rocketmq.babel
namespace java com.ndpmedia.rocketmq.babel

struct Message {
   1: string topic,
   2: i32 flag = 0,
   3: map<string, string> properties,
   4: binary data,
}

service Producer {

  oneway void setProducerGroup(1:string consumerGroup),

   void send(1: Message message),

   void batchSend(1: list<Message> messageList)

}