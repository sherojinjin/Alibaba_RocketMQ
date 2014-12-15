/**
 * 
 */
package com.alibaba.rocketmq.common.constant;

/**
 * @author holly
 * name server configuration keys
 */
public enum NSConfigKey {
  //数据中心分发比例 10.1:0.5,10.2:0.3,10.5:0.2
  DC_DISPATCH_RATIO ("DC_SELECTOR","DC_DISPATCH_RATIO"),
  //数据中心分发策略： BY_RATIO, BY_LOCATION
  DC_DISPATCH_STRTEGY ("DC_SELECTOR","DC_DISPATCH_STRTEGY"),
  //数据中心分发速度策略比例，同机房    80
  DC_DISPATCH_STRTEGY_LOCATION_RATIO ("DC_SELECTOR","DC_DISPATCH_STRTEGY_LOCATION_RATIO");
  
  private String namespace;
  private String key;
  
  private NSConfigKey(String namespace,String key){
    this.namespace = namespace;
    this.key = key;
  }

  public String getNamespace() {
    return namespace;
  }

  public String getKey() {
    return key;
  }
  
}
