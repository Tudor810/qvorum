package com.qvorum.server.service;

import com.qvorum.server.exception.ApiException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Stores uploaded photos (meter readings, issue reports) on disk, under qvorum.upload-dir.
 * The database keeps only the relative path that store() returns, e.g. "meter-readings/3f2a….jpg".
 *
 * Rules:
 * - only JPEG and PNG images are accepted, checked from the file's first bytes, because the
 *   file name and the declared type are chosen by the client and can lie;
 * - the file gets a new random name, so a client cannot choose where it is written or overwrite
 *   another file;
 * - load() refuses paths that leave the upload folder (e.g. "../../secret.txt").
 * The maximum size is spring.servlet.multipart.max-file-size.
 */
@Service
public class FileStorageService {

    private static final Pattern FOLDER_NAME = Pattern.compile("[a-z0-9-]+");

    private final Path root;

    public FileStorageService(@Value("${qvorum.upload-dir}") String uploadDir) {
        this.root = Path.of(uploadDir).toAbsolutePath().normalize();
    }

    /**
     * Saves an image in the given folder and returns its path relative to the upload folder.
     *
     * @param folder a fixed name chosen by the server, such as "issue-reports"
     */
    public String store(MultipartFile file, String folder) {
        if (!FOLDER_NAME.matcher(folder).matches()) {
            throw new IllegalArgumentException("Invalid folder name: " + folder);
        }
        if (file == null || file.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Please attach a photo.");
        }
        String extension = detectImageExtension(file);
        String relativePath = folder + "/" + UUID.randomUUID() + "." + extension;
        Path target = root.resolve(relativePath);
        try {
            Files.createDirectories(target.getParent());
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, target);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Could not save the uploaded file", e);
        }
        return relativePath;
    }

    /** Returns the file stored at a path that store() returned earlier. */
    public Path load(String relativePath) {
        Path file = root.resolve(relativePath).normalize();
        if (!file.startsWith(root) || !Files.isRegularFile(file)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "The file does not exist.");
        }
        return file;
    }

    /** Recognizes JPEG (starts with FF D8 FF) and PNG (starts with 89 'P' 'N' 'G'). */
    private static String detectImageExtension(MultipartFile file) {
        byte[] header = new byte[4];
        int read;
        try (InputStream in = file.getInputStream()) {
            read = in.readNBytes(header, 0, header.length);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read the uploaded file", e);
        }
        if (read >= 3 && (header[0] & 0xFF) == 0xFF && (header[1] & 0xFF) == 0xD8 && (header[2] & 0xFF) == 0xFF) {
            return "jpg";
        }
        if (read == 4 && (header[0] & 0xFF) == 0x89 && header[1] == 'P' && header[2] == 'N' && header[3] == 'G') {
            return "png";
        }
        throw new ApiException(HttpStatus.BAD_REQUEST, "Only JPEG and PNG photos are accepted.");
    }
}
