package com.bangboo.adminops.client;

import com.fasterxml.jackson.databind.ObjectMapper;

final class JsonMapperHolder {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private JsonMapperHolder() {
    }

    static <T> T convert(Object value, Class<T> type) {
        return OBJECT_MAPPER.convertValue(value, type);
    }
}
