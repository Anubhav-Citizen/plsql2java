package com.plsql2java.translation.model;

import java.util.List;

public class JavaIR {

    private final String objectName;
    private final String packageName;
    private final String className;
    private final List<String> imports;
    private final List<String> fields;
    private final List<JavaMethodIR> methods;
    private final String rawSource;

    public JavaIR(String objectName, String packageName, String className,
                  List<String> imports, List<String> fields,
                  List<JavaMethodIR> methods, String rawSource) {
        this.objectName = objectName;
        this.packageName = packageName;
        this.className = className;
        this.imports = imports;
        this.fields = fields;
        this.methods = methods;
        this.rawSource = rawSource;
    }

    public String getObjectName() { return objectName; }
    public String getPackageName() { return packageName; }
    public String getClassName() { return className; }
    public List<String> getImports() { return imports; }
    public List<String> getFields() { return fields; }
    public List<JavaMethodIR> getMethods() { return methods; }
    public String getRawSource() { return rawSource; }
}
