package com.senderman.jlogrep.model.rule;

import com.senderman.jlogrep.model.response.LogString;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Deque;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;

@Serdeable
@Schema(description = "Filters for rules")
public enum RuleFilter implements Supplier<Predicate<LogString>> {

    UNIQUE {
        @Override
        public Predicate<LogString> get() {

            return new Predicate<>() {

                private final Set<Deque<String>> lines = new HashSet<>();

                @Override
                public boolean test(LogString logString) {
                    return lines.add(logString.getLines());
                }
            };
        }
    },

    DATED {
        @Override
        public Predicate<LogString> get() {
            return s -> s.getRawDate() != null;
        }
    }

}
