package com.plsql2java.codegen;

import com.plsql2java.codegen.model.ArtifactType;
import com.plsql2java.codegen.model.GeneratedProject;
import com.plsql2java.codegen.model.JavaSourceFile;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GeneratedProjectTest {

    @Test
    void getFilesByType_filtersCorrectly() {
        GeneratedProject project = new GeneratedProject("m1", "test-project");
        project.addFile(new JavaSourceFile("a/Service.java", "content", "OBJ_A", ArtifactType.SERVICE));
        project.addFile(new JavaSourceFile("a/Controller.java", "content", "OBJ_A", ArtifactType.CONTROLLER));
        project.addFile(new JavaSourceFile("b/Service.java", "content", "OBJ_B", ArtifactType.SERVICE));

        assertThat(project.getFilesByType(ArtifactType.SERVICE)).hasSize(2);
        assertThat(project.getFilesByType(ArtifactType.CONTROLLER)).hasSize(1);
        assertThat(project.getFilesByType(ArtifactType.TEST)).isEmpty();
    }

    @Test
    void generatedAt_isSetOnConstruction() {
        GeneratedProject project = new GeneratedProject("m1", "test-project");
        assertThat(project.getGeneratedAt()).isNotNull();
    }

    @Test
    void skippedObjects_tracked() {
        GeneratedProject project = new GeneratedProject("m1", "test-project");
        project.addSkippedObject("FAILED_PROC");
        assertThat(project.getSkippedObjects()).containsExactly("FAILED_PROC");
    }
}
