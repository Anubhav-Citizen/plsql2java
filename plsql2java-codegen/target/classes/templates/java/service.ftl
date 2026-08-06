package ${packageName}.service;

<#list imports as imp>
import ${imp};
</#list>
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Translated from PL/SQL: ${schemaName}.${objectName}
 */
@Service
public class ${className}Service {

    private static final Logger log = LoggerFactory.getLogger(${className}Service.class);

<#list fields as field>
    private final ${field};
</#list>

    public ${className}Service(<#list fields as field>${field}<#sep>, </#sep></#list>) {
<#list fieldNames as name>
        this.${name} = ${name};
</#list>
    }

<#list methods as method>
    /**
     * Translated from PL/SQL: ${schemaName}.${objectName}.${method.methodName}
<#if method.javadoc?has_content>
     * ${method.javadoc}
</#if>
     */
<#list method.annotations as ann>
    ${ann}
</#list>
<#if method.belowThreshold>
    // @ConfidenceScore(${method.confidenceScore}%) — manual review recommended
</#if>
    public ${method.returnType} ${method.methodName}(<#list method.parameters as p>${p}<#sep>, </#sep></#list>) {
        ${method.body}
    }

</#list>
}
