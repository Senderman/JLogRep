package com.senderman.jlogrep.config;

import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.InputStream;
import java.util.List;
import java.util.stream.StreamSupport;

public class YamlConfigMapper implements ConfigMapper {

    @Override
    public <T> T load(InputStream in, Class<T> type) {
        return getYaml(type).load(in);
    }

    @Override
    public <T> List<T> loadAll(InputStream in, Class<T> type) {
        return StreamSupport.stream(getYaml(type).loadAll(in).spliterator(), true)
                .filter(type::isInstance)
                .map(type::cast)
                .toList();
    }

    private Yaml getYaml(Class<?> type) {
        return new Yaml(new Constructor(type, new LoaderOptions()));
    }
}
