package com.senderman.jlogrep.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;

public class YamlConfigMapper implements ConfigMapper {

    private final ObjectMapper objectMapper;

    public YamlConfigMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public <T> T map(InputStream in, Class<T> type) throws IOException {
        return objectMapper.readValue(in, type);
    }

    @Override
    public <T> T map(InputStream in, TypeReference<T> type) throws IOException {
        return objectMapper.readValue(in, type);
    }
}
