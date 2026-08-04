package com.plsql2java.discovery;

import com.plsql2java.model.OracleObject;
import com.plsql2java.model.OracleObjectType;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Normalizes raw OracleObject instances:
 * - Merges PACKAGE_BODY into its corresponding PACKAGE spec
 * - Merges TYPE_BODY into its corresponding TYPE spec
 * - Uppercases names and schemas
 */
@Component
public class OracleObjectNormalizer {

    public List<OracleObject> normalize(List<OracleObject> raw) {
        Map<String, OracleObject> packageSpecs = new LinkedHashMap<>();
        Map<String, OracleObject> typeSpecs = new LinkedHashMap<>();
        List<OracleObject> others = new ArrayList<>();

        for (OracleObject obj : raw) {
            obj.setName(obj.getName().toUpperCase());
            if (obj.getSchema() != null) obj.setSchema(obj.getSchema().toUpperCase());

            if (obj.getType() == OracleObjectType.PACKAGE) {
                packageSpecs.put(obj.getName(), obj);
            } else if (obj.getType() == OracleObjectType.PACKAGE_BODY) {
                OracleObject spec = packageSpecs.get(obj.getName());
                if (spec != null) {
                    spec.setSourceBody(obj.getSourceSpec());
                } else {
                    // Body without spec — treat as standalone
                    obj.setType(OracleObjectType.PACKAGE);
                    packageSpecs.put(obj.getName(), obj);
                }
            } else if (obj.getType() == OracleObjectType.TYPE) {
                typeSpecs.put(obj.getName(), obj);
            } else if (obj.getType() == OracleObjectType.TYPE_BODY) {
                OracleObject spec = typeSpecs.get(obj.getName());
                if (spec != null) {
                    spec.setSourceBody(obj.getSourceSpec());
                } else {
                    obj.setType(OracleObjectType.TYPE);
                    typeSpecs.put(obj.getName(), obj);
                }
            } else {
                others.add(obj);
            }
        }

        List<OracleObject> result = new ArrayList<>();
        result.addAll(packageSpecs.values());
        result.addAll(typeSpecs.values());
        result.addAll(others);
        return result;
    }
}
