package com.senderman.jlogrep.config;

import com.senderman.jlogrep.model.internal.ScanOptions;
import com.senderman.jlogrep.model.rule.GrepRule;
import com.senderman.jlogrep.model.rule.LogDateFormat;
import com.senderman.jlogrep.model.rule.RuleFilter;
import com.senderman.jlogrep.util.ConfigConverter;
import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Prototype;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;

@Factory
public class Beans {

    @Singleton
    @Context
    @Named("internalTags")
    List<String> internalTags(List<GrepRule> rules) {
        return rules
                .stream()
                .map(GrepRule::tag)
                .distinct()
                .toList();
    }

    @Singleton
    @Context
    List<GrepRule> rules(ConfigMapper configMapper) {
        return ConfigConverter.toGrepRules(configMapper.mapRules(getClass().getResourceAsStream("/rules.yml")));
    }

    @Singleton
    @Context
    LogDateFormat logDateFormat(ConfigMapper configMapper) {
        return ConfigConverter.toLogDateFormat(configMapper.mapLogDateFormat(getClass().getResourceAsStream("/dateformat.yml")));
    }

    @Singleton
    ConfigMapper configMapper() {
        return new YamlConfigMapper();
    }

    // always inject this bean as javax.inject.Provider / jakarta.inject.Provider / io.micronaut.context.BeanProvider
    // so each get() call will give you a new instance
    @Prototype
    ScanOptions defaultScanOptions(List<GrepRule> rules, LogDateFormat dateFormat) {
        return new ScanOptions(
                rules,
                dateFormat,
                null,
                10,
                LocalDate.now(ZoneId.of("UTC+0")).getYear(),
                new HashSet<>(),
                EnumSet.noneOf(RuleFilter.class)
        );
    }


}
