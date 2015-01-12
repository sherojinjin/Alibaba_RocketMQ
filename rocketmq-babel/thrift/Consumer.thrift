namespace py com.ndpmedia.rocketmq.babel
namespace php com.ndpmedia.rocketmq.babel
namespace cpp com.ndpmedia.rocketmq.babel
namespace java com.ndpmedia.rocketmq.babel

struct MessageExt {
   1: string topic,
   2: i32 flag = 0,
   3: map<string, string> properties,
   4: binary data,
   5: i32 queueId,
   6: i32 storeSize,
   7: i64 queueOffset,
   8: i32 sysFlag,
   9: i64 bornTimestamp,
   10: string bornHost,
   11: i64 storeTimestamp,
   12: string storeHost,
   13: string msgId,
   14: i64 commitLogOffset,
   15: i64 bodyCRC,
   16: i32 reconsumeTimes,
}

enum MessageModel {
   BROADCASTING = 1,
   CLUSTERING   = 2
}

service Consumer {

   oneway void setConsumerGroup(1:string consumerGroup),

   oneway void setMessageModel(1:MessageModel messageModel),

   oneway void registerTopic(1:string topic, 2:string tag),

   oneway void start(),

   void pull(),

   oneway void stop()
}