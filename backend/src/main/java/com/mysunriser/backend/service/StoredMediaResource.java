package com.mysunriser.backend.service;

import org.springframework.core.io.Resource;

public record StoredMediaResource(Resource resource, long contentLength) {
}
