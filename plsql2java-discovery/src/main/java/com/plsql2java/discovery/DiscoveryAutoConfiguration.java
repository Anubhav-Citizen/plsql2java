package com.plsql2java.discovery;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {"com.plsql2java.discovery", "com.plsql2java.dependency"})
public class DiscoveryAutoConfiguration {
}
