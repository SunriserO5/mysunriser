CREATE TABLE online_tool (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    slug VARCHAR(100) NOT NULL UNIQUE,
    title VARCHAR(120) NOT NULL,
    summary VARCHAR(500) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'Draft',
    entry_type VARCHAR(20) NOT NULL DEFAULT 'INTERNAL',
    route_path VARCHAR(255),
    external_url VARCHAR(500),
    access_level VARCHAR(20) NOT NULL DEFAULT 'PUBLIC',
    sort_order INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_online_tool_list (status, access_level, sort_order, created_at)
);
