package com.plsql2java.web;

import com.plsql2java.web.model.UploadedFile;
import com.plsql2java.web.service.FileUploadService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileUploadServiceTest {

    @TempDir Path tempDir;

    private FileUploadService service() throws IOException {
        return new FileUploadService(tempDir.toString());
    }

    @Test
    void store_validSqlFile_returnsUploadedFile() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file", "schema.sql", "text/plain", "CREATE TABLE t (id NUMBER);".getBytes());

        UploadedFile result = service().store(file);

        assertThat(result.getUploadId()).isNotBlank();
        assertThat(result.getSanitizedFilename()).isEqualTo("schema.sql");
        assertThat(result.getSizeBytes()).isGreaterThan(0);
        assertThat(result.getTempPath()).exists();
    }

    @Test
    void store_nonSqlFile_throwsIllegalArgument() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file", "script.sh", "text/plain", "rm -rf /".getBytes());

        assertThatThrownBy(() -> service().store(file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(".sql");
    }

    @Test
    void store_emptyFile_throwsIllegalArgument() throws IOException {
        MockMultipartFile file = new MockMultipartFile("file", "empty.sql", "text/plain", new byte[0]);

        assertThatThrownBy(() -> service().store(file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("empty");
    }

    @Test
    void store_pathTraversalFilename_sanitized() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file", "../../../etc/passwd.sql", "text/plain", "SELECT 1 FROM DUAL;".getBytes());

        UploadedFile result = service().store(file);
        // Sanitized filename should not contain path separators
        assertThat(result.getSanitizedFilename()).doesNotContain("/").doesNotContain("\\").doesNotContain("..");
    }

    @Test
    void delete_removesUploadDirectory() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file", "schema.sql", "text/plain", "SELECT 1 FROM DUAL;".getBytes());
        UploadedFile uploaded = service().store(file);

        FileUploadService svc = service();
        svc.store(file); // store again to get a fresh uploadId
        svc.delete(uploaded.getUploadId());

        assertThat(uploaded.getTempPath().getParent()).doesNotExist();
    }
}
