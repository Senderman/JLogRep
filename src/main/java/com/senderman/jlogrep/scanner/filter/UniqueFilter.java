package com.senderman.jlogrep.scanner.filter;

import com.senderman.jlogrep.model.response.LogString;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

public class UniqueFilter implements Predicate<LogString> {

    Set<String> lines = new HashSet<>();

    @Override
    public boolean test(LogString logString) {
        return lines.add(logString.getLine());
    }
}
