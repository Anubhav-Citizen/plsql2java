package com.plsql2java.translation.model;

import java.util.List;

public class JavaMethodIR {

    private final String methodName;
    private final String returnType;
    private final List<String> parameters;
    private final String body;
    private final List<String> annotations;
    private final String javadoc;
    private final List<ConstructTranslationResult> constructResults;

    public JavaMethodIR(String methodName, String returnType, List<String> parameters,
                        String body, List<String> annotations, String javadoc,
                        List<ConstructTranslationResult> constructResults) {
        this.methodName = methodName;
        this.returnType = returnType;
        this.parameters = parameters;
        this.body = body;
        this.annotations = annotations;
        this.javadoc = javadoc;
        this.constructResults = constructResults;
    }

    public String getMethodName() { return methodName; }
    public String getReturnType() { return returnType; }
    public List<String> getParameters() { return parameters; }
    public String getBody() { return body; }
    public List<String> getAnnotations() { return annotations; }
    public String getJavadoc() { return javadoc; }
    public List<ConstructTranslationResult> getConstructResults() { return constructResults; }
}
