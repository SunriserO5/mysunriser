/**
 * Tool-specific backend implementations belong in subpackages named after the tool slug.
 *
 * <p>The shared tool catalog lives in the existing controller/service/Dao/entity/dto layers.
 * A concrete tool can add its own controller, service, and DTOs under a package such as
 * {@code com.mysunriser.backend.tools.jsonformat} while exposing endpoints below
 * {@code /api/tools/json-format/...}.</p>
 */
package com.mysunriser.backend.tools;
