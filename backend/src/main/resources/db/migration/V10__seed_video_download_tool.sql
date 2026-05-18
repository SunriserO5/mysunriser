INSERT INTO online_tool (
    slug,
    title,
    summary,
    status,
    entry_type,
    route_path,
    external_url,
    access_level,
    sort_order
)
VALUES (
    'video-download',
    '在线视频解析下载器',
    '解析视频、图片或音频帖子链接，提取可直接打开的资源地址。',
    'Published',
    'INTERNAL',
    '/tools/video-download',
    '',
    'PUBLIC',
    10
)
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    summary = VALUES(summary),
    status = VALUES(status),
    entry_type = VALUES(entry_type),
    route_path = VALUES(route_path),
    external_url = VALUES(external_url),
    access_level = VALUES(access_level),
    sort_order = VALUES(sort_order);
