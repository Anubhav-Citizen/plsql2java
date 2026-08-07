package com.plsql2java.translation.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AstNode {

    private final ConstructType constructType;
    private final String text;
    private final int lineNumber;
    private final List<AstNode> children;
    private final Map<String, String> attributes;
    private final Map<String, Object> objectAttributes;

    public AstNode(ConstructType constructType, String text, int lineNumber) {
        this.constructType = constructType;
        this.text = text;
        this.lineNumber = lineNumber;
        this.children = new ArrayList<>();
        this.attributes = new HashMap<>();
        this.objectAttributes = new HashMap<>();
    }

    public void addChild(AstNode child) {
        children.add(child);
    }

    public void setAttribute(String key, String value) {
        attributes.put(key, value);
    }

    public void setAttribute(String key, Object value) {
        objectAttributes.put(key, value);
    }

    public String getAttribute(String key) {
        return attributes.get(key);
    }

    public Object getObjectAttribute(String key) {
        return objectAttributes.get(key);
    }

    public ConstructType getConstructType() { return constructType; }
    public String getText() { return text; }
    public int getLineNumber() { return lineNumber; }
    public List<AstNode> getChildren() { return children; }
    public Map<String, String> getAttributes() { return attributes; }
}
