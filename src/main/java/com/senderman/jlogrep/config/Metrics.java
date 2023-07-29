package com.senderman.jlogrep.config;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.config.MeterFilterReply;
import jakarta.inject.Singleton;

import java.util.regex.Pattern;

import static io.micrometer.core.instrument.config.MeterFilterReply.*;

@Singleton
public class Metrics implements MeterFilter {

    private final static Pattern denyMetrics = Pattern.compile(".*(?:js|css|html|ttf|png|yml)");

    @Override
    public MeterFilterReply accept(Meter.Id id) {
        if (!id.getName().contains("http.server.requests"))
            return NEUTRAL;

        var tag = id.getTag("uri");
        if (tag == null)
            return NEUTRAL;

        return denyMetrics.matcher(tag).matches() ? DENY : ACCEPT;
    }

}
