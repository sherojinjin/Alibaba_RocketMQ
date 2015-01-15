namespace py com.ndpmedia.rocketmq.babel
namespace php com.ndpmedia.rocketmq.babel
namespace cpp com.ndpmedia.rocketmq.babel
namespace java com.ndpmedia.rocketmq.babel

include "Model.thrift"

service Producer {
   void send(1: Model.Message message),

   void batchSend(1: list<Model.Message> messageList)

   oneway void stop()
}