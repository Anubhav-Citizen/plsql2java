package ${packageName}.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "${tableName}")
public class ${className} {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "${tableName}_SEQ")
    @SequenceGenerator(name = "${tableName}_SEQ", sequenceName = "SEQ_${tableName}", allocationSize = 1)
    private Long id;

<#list columns as col>
    @Column(name = "${col.name}"<#if !col.nullable>, nullable = false</#if><#if col.length gt 0>, length = ${col.length}</#if>)
    private ${col.javaType} ${col.fieldName};

</#list>
    public ${className}() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

<#list columns as col>
    public ${col.javaType} get${col.capitalName}() { return ${col.fieldName}; }
    public void set${col.capitalName}(${col.javaType} ${col.fieldName}) { this.${col.fieldName} = ${col.fieldName}; }

</#list>
    /**
     * Translated from Oracle TRIGGER TRG_${tableName}.
     * Sequence ID is handled by @GeneratedValue.
     */
    @PrePersist
    protected void onPrePersist() {
        // BEFORE INSERT trigger logic
    }

    @PreUpdate
    protected void onPreUpdate() {
        // BEFORE UPDATE trigger logic
    }
}
