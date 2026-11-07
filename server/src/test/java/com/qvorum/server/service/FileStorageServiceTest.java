package com.qvorum.server.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.qvorum.server.exception.ApiException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

/** A plain unit test: no Spring, no database, just the class and a temporary folder. */
class FileStorageServiceTest {

    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A};
    private static final byte[] JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0x00};

    @TempDir
    Path tempDir;

    FileStorageService storage;

    @BeforeEach
    void setUp() {
        storage = new FileStorageService(tempDir.toString());
    }

    @Test
    void storesPngWithRandomNameInFolder() throws Exception {
        String path = storage.store(new MockMultipartFile("photo", "../../evil.png", "image/png", PNG), "issue-reports");

        assertThat(path).startsWith("issue-reports/").endsWith(".png").doesNotContain("evil");
        assertThat(Files.readAllBytes(storage.load(path))).isEqualTo(PNG);
    }

    @Test
    void detectsJpegFromContentNotName() {
        String path = storage.store(new MockMultipartFile("photo", "photo.png", "image/png", JPEG), "meter-readings");

        assertThat(path).endsWith(".jpg");
    }

    @Test
    void rejectsFilesThatAreNotImages() {
        var fakeImage = new MockMultipartFile("photo", "photo.jpg", "image/jpeg", "<script>".getBytes());

        assertThatThrownBy(() -> storage.store(fakeImage, "issue-reports"))
                .isInstanceOf(ApiException.class)
                .hasMessage("Only JPEG and PNG photos are accepted.");
    }

    @Test
    void rejectsEmptyUpload() {
        var empty = new MockMultipartFile("photo", new byte[0]);

        assertThatThrownBy(() -> storage.store(empty, "issue-reports")).isInstanceOf(ApiException.class);
    }

    @Test
    void loadRefusesPathsOutsideTheUploadFolder() throws Exception {
        Files.writeString(tempDir.getParent().resolve("secret.txt"), "secret");

        assertThatThrownBy(() -> storage.load("../secret.txt")).isInstanceOf(ApiException.class);
    }
}
