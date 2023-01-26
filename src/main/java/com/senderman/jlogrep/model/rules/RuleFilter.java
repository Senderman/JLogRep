package com.senderman.jlogrep.model.rules;

import com.senderman.jlogrep.model.response.LogString;
import com.senderman.jlogrep.scanner.filter.UniqueFilter;

import java.util.function.Predicate;
import java.util.function.Supplier;

public enum RuleFilter implements Supplier<Predicate<LogString>> {
    UNIQUE {
        @Override
        public Predicate<LogString> get() {
            return new UniqueFilter();
        }
    }
}
