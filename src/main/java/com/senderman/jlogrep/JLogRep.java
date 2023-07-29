package com.senderman.jlogrep;

import io.micronaut.runtime.Micronaut;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;

@OpenAPIDefinition(
        info = @Info(
                title = "JLogRep",
                description = "Regex-based log parser and analyzer",
                license = @License(name = "MIT")
        )
)
public class JLogRep {

    public static void main(String[] args) {
        Micronaut.build(args)
                .classes(JLogRep.class)
                .banner(!System.getProperties().containsKey("disableBanner"))
                .environmentPropertySource(false)
                .start();
    }

}
