package com.bangboo.marketplace.client;

import com.fasterxml.jackson.databind.ObjectMapper;

public final class JsonMapperHolder {
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .findAndRegisterModules();

    private JsonMapperHolder() {
    }

    public static <T> T convert(Object value, Class<T> type) {
        return MAPPER.convertValue(value, type);
    }
}
