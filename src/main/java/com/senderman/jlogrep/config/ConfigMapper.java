package com.senderman.jlogrep.config;

import com.senderman.jlogrep.exception.InvalidConfigFormatException;
import com.senderman.jlogrep.model.rule.ConfigDateFormat;
import com.senderman.jlogrep.model.rule.ConfigRule;

import java.io.InputStream;
import java.util.List;

public interface ConfigMapper {

    <T> T load(InputStream in, Class<T> type);

    <T> List<T> loadAll(InputStream in, Class<T> type);

    default List<ConfigRule> mapRules(InputStream in) throws InvalidConfigFormatException {
        try {
            return loadAll(in, ConfigRule.class);
        } catch (Exception e) {
            throw new InvalidConfigFormatException("rules.yml", e);
        }
    }

    default ConfigDateFormat mapLogDateFormat(InputStream in) throws InvalidConfigFormatException {
        try {
            return load(in, ConfigDateFormat.class);
        } catch (Exception e) {
            throw new InvalidConfigFormatException("dateformat.yml", e);
        }
    }

}
