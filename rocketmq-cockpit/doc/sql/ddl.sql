CREATE TABLE broker(
  id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
  cluster_name VARCHAR (255) NOT NULL,
  broker_name VARCHAR (255) NOT NULL ,
  broker_id SMALLINT NOT NULL ,
  default_topic_queue_num SMALLINT NOT NULL
);

CREATE TABLE broker_aggregation(
  borker_id INT NOT NULL REFERENCES broker(id),
  topic_name VARCHAR(255) NOT NULL ,
  aggregation_num INT NOT NULL ,
  created_time DATETIME NOT NULL
);

