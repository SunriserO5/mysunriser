CREATE TABLE project (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    slug VARCHAR(100) NOT NULL UNIQUE,
    title VARCHAR(120) NOT NULL,
    summary VARCHAR(500) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'Draft',
    repo_owner VARCHAR(100) NOT NULL,
    repo_name VARCHAR(120) NOT NULL,
    repo_url VARCHAR(500) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    readme_markdown MEDIUMTEXT,
    readme_etag VARCHAR(255),
    readme_cached_at DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_project_list (status, sort_order, created_at),
    INDEX idx_project_repo (repo_owner, repo_name)
);
