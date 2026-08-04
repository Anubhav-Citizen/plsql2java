package com.plsql2java.codegen;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {
        "com.plsql2java.codegen",
        "com.plsql2java.scoring",
        "com.plsql2java.reporting"
})
public class CodegenAutoConfiguration {
}
