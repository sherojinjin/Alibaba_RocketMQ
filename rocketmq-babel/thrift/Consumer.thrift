namespace py com.ndpmedia.rocketmq.babel
namespace php com.ndpmedia.rocketmq.babel
namespace cpp com.ndpmedia.rocketmq.babel
namespace java com.ndpmedia.rocketmq.babel

struct MessageExt {
   1: required string topic,
   2: optional i32 flag = 0,
   3: optional map<string, string> properties,
   4: required binary data,
   5: optional i32 queueId,
   6: optional i32 storeSize,
   7: optional i64 queueOffset,
   8: optional i32 sysFlag,
   9: optional i64 bornTimestamp,
   10: optional string bornHost,
   11: optional i64 storeTimestamp,
   12: optional string storeHost,
   13: required string msgId,
   14: optional i64 commitLogOffset,
   15: optional i64 bodyCRC,
   16: optional i32 reconsumeTimes,
}

service Consumer {

   list<MessageExt> pull(),

   oneway void stop()
}