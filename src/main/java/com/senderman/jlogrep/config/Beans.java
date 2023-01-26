package com.senderman.jlogrep.config;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.senderman.jlogrep.model.rules.FileRule;
import com.senderman.jlogrep.model.rules.LogDateFormat;
import com.senderman.jlogrep.model.rules.YamlRule;
import com.senderman.jlogrep.util.RuleConverter;
import io.micronaut.context.annotation.Factory;
import jakarta.inject.Singleton;

import java.util.List;

@Factory
public class Beans {

    @Singleton
    List<YamlRule> yamlRules(ConfigMapper configMapper) {
        return configMapper.mapRules(getClass().getResourceAsStream("/rules.yml"));
    }

    @Singleton
    List<FileRule> rules(List<YamlRule> yamlRules) {
        return RuleConverter.toFileRules(yamlRules);
    }

    @Singleton
    LogDateFormat logDateFormat(ConfigMapper configMapper) {
        return configMapper.mapLogDateFormat(getClass().getResourceAsStream("/dateformat.yml"));
    }

    @Singleton
    ConfigMapper configMapper() {
        return new YamlConfigMapper(new YAMLMapper());
    }

}
