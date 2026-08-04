package com.plsql2java.web.packaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
public class ZipPackager {

    private static final Logger log = LoggerFactory.getLogger(ZipPackager.class);

    /**
     * Packages all files under projectDir into a temp ZIP file.
     * Uses streaming ZipOutputStream — no full in-memory buffering.
     */
    public Path packageProject(Path projectDir) throws IOException {
        Path zipFile = Files.createTempFile("plsql2java-generated-", ".zip");
        try (ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(Files.newOutputStream(zipFile)))) {
            try (var stream = Files.walk(projectDir)) {
                stream.filter(Files::isRegularFile)
                      .filter(p -> !isExcluded(p))
                      .forEach(file -> addToZip(zos, projectDir, file));
            }
        }
        log.debug("Packaged project to ZIP: {} ({} bytes)", zipFile, Files.size(zipFile));
        return zipFile;
    }

    /**
     * Writes ZIP content to the output stream, then deletes the temp ZIP file.
     */
    public void streamAndDelete(Path zipFile, OutputStream out) throws IOException {
        try {
            Files.copy(zipFile, out);
        } finally {
            Files.deleteIfExists(zipFile);
        }
    }

    private void addToZip(ZipOutputStream zos, Path base, Path file) {
        try {
            String entryName = base.relativize(file).toString().replace('\\', '/');
            zos.putNextEntry(new ZipEntry(entryName));
            Files.copy(file, zos);
            zos.closeEntry();
        } catch (IOException e) {
            log.warn("Failed to add file to ZIP: {}", file, e);
        }
    }

    private boolean isExcluded(Path path) {
        String name = path.getFileName().toString();
        return name.startsWith(".") || name.equals(".DS_Store");
    }
}
