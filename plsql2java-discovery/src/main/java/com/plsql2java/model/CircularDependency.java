package com.plsql2java.model;

import java.util.List;

public class CircularDependency {

    private List<String> cycle;
    private String description;

    public CircularDependency() {}

    public CircularDependency(List<String> cycle) {
        this.cycle = cycle;
        this.description = "Circular dependency: " + String.join(" -> ", cycle) + " -> " + cycle.get(0);
    }

    public List<String> getCycle() { return cycle; }
    public void setCycle(List<String> cycle) { this.cycle = cycle; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
