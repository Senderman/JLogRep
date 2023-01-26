package com.senderman.jlogrep.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.senderman.jlogrep.exception.InvalidConfigFormatException;
import com.senderman.jlogrep.model.rules.LogDateFormat;
import com.senderman.jlogrep.model.rules.YamlRule;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface ConfigMapper {
    <T> T map(InputStream in, Class<T> type) throws IOException;

    <T> T map(InputStream in, TypeReference<T> type) throws IOException;

    default List<YamlRule> mapRules(InputStream in) throws InvalidConfigFormatException {
        try {
            return map(in, new TypeReference<>() {
            });
        } catch (IOException e) {
            throw new InvalidConfigFormatException("rules.yml");
        }
    }

    default LogDateFormat mapLogDateFormat(InputStream in) throws InvalidConfigFormatException {
        try {
            return map(in, LogDateFormat.class);
        } catch (IOException e) {
            throw new InvalidConfigFormatException("dateformat.yml");
        }
    }

}
