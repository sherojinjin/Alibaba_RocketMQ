namespace py com.ndpmedia.rocketmq.babel
namespace php com.ndpmedia.rocketmq.babel
namespace cpp com.ndpmedia.rocketmq.babel
namespace java com.ndpmedia.rocketmq.babel

include "Model.thrift"

service Consumer {

   list<Model.MessageExt> pull(),

   oneway void stop()
}