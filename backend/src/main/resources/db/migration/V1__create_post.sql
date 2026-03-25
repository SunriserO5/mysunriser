CREATE TABLE post (
                      id BIGINT PRIMARY KEY AUTO_INCREMENT,
                      slug VARCHAR(128) NOT NULL UNIQUE,
                      title VARCHAR(200) NOT NULL,
                      summary VARCHAR(200),
                      content MEDIUMTEXT NOT NULL,
                      status VARCHAR(20) NOT NULL,
                      published_at DATETIME NULL,
                      created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                      updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);