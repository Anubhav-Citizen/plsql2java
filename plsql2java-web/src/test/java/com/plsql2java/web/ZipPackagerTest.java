package com.plsql2java.web;

import com.plsql2java.web.packaging.ZipPackager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipInputStream;

import static org.assertj.core.api.Assertions.assertThat;

class ZipPackagerTest {

    private final ZipPackager packager = new ZipPackager();

    @TempDir Path tempDir;

    @Test
    void packageProject_createsZipWithAllFiles() throws IOException {
        Path projectDir = tempDir.resolve("project");
        Files.createDirectories(projectDir.resolve("src/main/java"));
        Files.writeString(projectDir.resolve("src/main/java/Main.java"), "public class Main {}");
        Files.writeString(projectDir.resolve("pom.xml"), "<project/>");

        Path zipFile = packager.packageProject(projectDir);

        assertThat(zipFile).exists();
        assertThat(Files.size(zipFile)).isGreaterThan(0);

        // Verify ZIP contains expected entries
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipFile))) {
            int count = 0;
            while (zis.getNextEntry() != null) count++;
            assertThat(count).isEqualTo(2);
        }
    }

    @Test
    void packageProject_excludesHiddenFiles() throws IOException {
        Path projectDir = tempDir.resolve("project2");
        Files.createDirectories(projectDir);
        Files.writeString(projectDir.resolve("Main.java"), "public class Main {}");
        Files.writeString(projectDir.resolve(".DS_Store"), "hidden");

        Path zipFile = packager.packageProject(projectDir);

        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipFile))) {
            int count = 0;
            while (zis.getNextEntry() != null) count++;
            assertThat(count).isEqualTo(1); // only Main.java
        }
    }

    @Test
    void streamAndDelete_writesToOutputAndDeletesZip() throws IOException {
        Path projectDir = tempDir.resolve("project3");
        Files.createDirectories(projectDir);
        Files.writeString(projectDir.resolve("file.java"), "class A {}");

        Path zipFile = packager.packageProject(projectDir);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        packager.streamAndDelete(zipFile, out);

        assertThat(out.size()).isGreaterThan(0);
        assertThat(zipFile).doesNotExist();
    }
}
