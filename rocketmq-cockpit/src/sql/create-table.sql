USE cockpit;

CREATE TABLE IF NOT EXISTS name_server (
  id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
  ip INT NOT NULL,
  port SMALLINT NOT NULL DEFAULT 9876
);

CREATE TABLE IF NOT EXISTS ip_mapping(
  id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
  inner_ip INT NOT NULL,
  public_ip INT NOT NULL
);

CREATE TABLE IF NOT EXISTS topic(
  id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(255) NOT NULL,
  cluster VARCHAR(100) NOT NULL DEFAULT 'DefaultCluster',
  permission TINYINT NOT NULL DEFAULT 6
-- continue
);


CREATE TABLE IF NOT EXISTS consumer_group(
  id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(255) NOT NULL,
  enable BOOL,
  broadcasting BOOL
-- continue
);

-- ------------------------------------------------------------
-- Constraints and indexes.
-- ------------------------------------------------------------








