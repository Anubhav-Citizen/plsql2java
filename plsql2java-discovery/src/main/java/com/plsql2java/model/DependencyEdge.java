package com.plsql2java.model;

public class DependencyEdge {

    private String fromObject;
    private String toObject;
    private ReferenceType referenceType;

    public DependencyEdge() {}

    public DependencyEdge(String fromObject, String toObject, ReferenceType referenceType) {
        this.fromObject = fromObject;
        this.toObject = toObject;
        this.referenceType = referenceType;
    }

    public String getFromObject() { return fromObject; }
    public void setFromObject(String fromObject) { this.fromObject = fromObject; }

    public String getToObject() { return toObject; }
    public void setToObject(String toObject) { this.toObject = toObject; }

    public ReferenceType getReferenceType() { return referenceType; }
    public void setReferenceType(ReferenceType referenceType) { this.referenceType = referenceType; }
}
