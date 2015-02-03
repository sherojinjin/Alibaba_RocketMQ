USE cockpit;

-- Insert name server list.
INSERT INTO name_server(ip, port) VALUES ('54.173.39.198', 9876);
INSERT INTO name_server(ip, port) VALUES ('54.173.209.191', 9876);


-- Insert IP mapping.
INSERT INTO ip_mapping(inner_ip, public_ip) VALUES ('10.5.36.11', '54.94.203.40');
INSERT INTO ip_mapping(inner_ip, public_ip) VALUES ('10.1.36.13', '54.174.104.250');
INSERT INTO ip_mapping(inner_ip, public_ip) VALUES ('10.2.36.10', '54.169.194.51');
INSERT INTO ip_mapping(inner_ip, public_ip) VALUES ('10.1.36.10', '54.173.39.198');
INSERT INTO ip_mapping(inner_ip, public_ip) VALUES ('10.2.36.11', '54.169.174.172');
INSERT INTO ip_mapping(inner_ip, public_ip) VALUES ('10.3.36.10', '54.67.6.98');
INSERT INTO ip_mapping(inner_ip, public_ip) VALUES ('10.5.36.10', '54.94.212.186');
INSERT INTO ip_mapping(inner_ip, public_ip) VALUES ('10.3.36.11', '54.67.77.111');
INSERT INTO ip_mapping(inner_ip, public_ip) VALUES ('10.1.36.12', '54.174.184.203');
INSERT INTO ip_mapping(inner_ip, public_ip) VALUES ('10.1.36.11', '54.173.209.191');

INSERT INTO cockpit_user(username, password, role) VALUES ('root', '320734fbb627d6884a1284acbdaa5db9', 'ROLE_ADMIN');
INSERT INTO cockpit_user(username, password, role) VALUES ('xutao', '23dc3038d25ef09f3ad7c0552b40ef9f', 'ROLE_USER');

INSERT INTO topic(topic, broker_address) VALUES ('T_QuickStart', '54.94.212.186:10911');
INSERT INTO topic(topic, broker_address) VALUES ('T_QuickStart', '54.94.203.40:10911');
INSERT INTO topic(topic, broker_address) VALUES ('TopicTest_Robert', '172.30.50.54:10911');
INSERT INTO topic(topic, broker_address) VALUES ('T_PARSER', '172.30.50.54:10911');

INSERT INTO consumer_group(group_name, broker_address, broker_id) VALUES ('CG_QuickStart', '54.94.212.186:10911', 0);
INSERT INTO consumer_group(group_name, broker_address, broker_id) VALUES ('C_GKT_MQ_GROUP', '172.30.50.54:10911', 0);