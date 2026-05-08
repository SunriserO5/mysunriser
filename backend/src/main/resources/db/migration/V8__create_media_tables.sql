CREATE TABLE media_asset (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    asset_type VARCHAR(20) NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    storage_key VARCHAR(255) NOT NULL UNIQUE,
    mime_type VARCHAR(100) NOT NULL,
    size_bytes BIGINT NOT NULL,
    access_level VARCHAR(20) NOT NULL DEFAULT 'PUBLIC',
    uploaded_by VARCHAR(64) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE post_media_reference (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    post_id BIGINT NOT NULL,
    media_asset_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_post_media_reference_post_asset (post_id, media_asset_id),
    CONSTRAINT fk_post_media_reference_post
        FOREIGN KEY (post_id) REFERENCES post(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_post_media_reference_media_asset
        FOREIGN KEY (media_asset_id) REFERENCES media_asset(id)
        ON DELETE RESTRICT
);

CREATE INDEX idx_media_asset_type ON media_asset(asset_type);
CREATE INDEX idx_media_asset_access_level ON media_asset(access_level);
CREATE INDEX idx_post_media_reference_asset ON post_media_reference(media_asset_id);
