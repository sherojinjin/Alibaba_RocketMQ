USE cockpit;

-- Insert name server list.
INSERT INTO name_server(ip, port) VALUES (INET_ATON('54.173.39.198'), 9876);
INSERT INTO name_server(ip, port) VALUES (INET_ATON('54.173.209.191'), 9876);


-- Insert IP mapping.
INSERT INTO ip_mapping(inner_ip, public_ip) VALUES (INET_ATON('10.5.36.11'), INET_ATON('54.94.203.40'));
INSERT INTO ip_mapping(inner_ip, public_ip) VALUES (INET_ATON('10.1.36.13'), INET_ATON('54.174.104.250'));
INSERT INTO ip_mapping(inner_ip, public_ip) VALUES (INET_ATON('10.2.36.10'), INET_ATON('54.169.194.51'));
INSERT INTO ip_mapping(inner_ip, public_ip) VALUES (INET_ATON('10.1.36.10'), INET_ATON('54.173.39.198'));
INSERT INTO ip_mapping(inner_ip, public_ip) VALUES (INET_ATON('10.2.36.11'), INET_ATON('54.169.174.172'));
INSERT INTO ip_mapping(inner_ip, public_ip) VALUES (INET_ATON('10.3.36.10'), INET_ATON('54.67.6.98'));
INSERT INTO ip_mapping(inner_ip, public_ip) VALUES (INET_ATON('10.5.36.10'), INET_ATON('54.94.212.186'));
INSERT INTO ip_mapping(inner_ip, public_ip) VALUES (INET_ATON('10.3.36.11'), INET_ATON('54.67.77.111'));
INSERT INTO ip_mapping(inner_ip, public_ip) VALUES (INET_ATON('10.1.36.12'), INET_ATON('54.174.184.203'));
INSERT INTO ip_mapping(inner_ip, public_ip) VALUES (INET_ATON('10.1.36.11'), INET_ATON('54.173.209.191'));

INSERT INTO cockpit_user(username, password) VALUES ('root', '320734fbb627d6884a1284acbdaa5db9');
INSERT INTO cockpit_user(username, password) VALUES ('xutao', '23dc3038d25ef09f3ad7c0552b40ef9f');