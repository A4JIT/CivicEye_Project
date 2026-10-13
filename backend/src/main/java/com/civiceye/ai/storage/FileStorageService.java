package com.civiceye.ai.storage;

import com.civiceye.ai.exception.BadRequestException;
import com.civiceye.ai.exception.ResourceNotFoundException;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path rootLocation;
    private final long maxFileSize;

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "webp");

    public FileStorageService(StorageProperties properties) {
        this.rootLocation = Paths.get(properties.getLocation()).toAbsolutePath().normalize();
        this.maxFileSize = properties.getMaxFileSize();
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage directory: " + rootLocation, e);
        }
    }

    public String storeFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Uploaded file is empty");
        }

        if (file.getSize() > maxFileSize) {
            throw new BadRequestException(String.format("File size exceeds maximum allowed limit of %d MB", maxFileSize / (1024 * 1024)));
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "");
        if (originalFilename.contains("..") || originalFilename.contains("/") || originalFilename.contains("\\")) {
            throw new BadRequestException("Filename contains invalid path sequence: " + originalFilename);
        }

        String extension = getFileExtension(originalFilename);
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new BadRequestException("Unsupported image format. Allowed formats are: JPG, JPEG, PNG, WEBP");
        }

        validateMagicBytes(file, extension);

        String uniqueFileName = UUID.randomUUID() + "." + extension.toLowerCase();
        try {
            Path destinationFile = this.rootLocation.resolve(uniqueFileName).normalize().toAbsolutePath();
            if (!destinationFile.getParent().equals(this.rootLocation.toAbsolutePath())) {
                throw new BadRequestException("Cannot store file outside current storage directory");
            }

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }
            return uniqueFileName;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file: " + originalFilename, e);
        }
    }

    public Resource loadAsResource(String filename) {
        if (!StringUtils.hasText(filename) || filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            throw new BadRequestException("Invalid filename request");
        }

        try {
            Path filePath = this.rootLocation.resolve(filename).normalize();
            if (!filePath.startsWith(this.rootLocation)) {
                throw new BadRequestException("Access denied: path traversal detected");
            }

            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new ResourceNotFoundException("File not found: " + filename);
            }
        } catch (MalformedURLException e) {
            throw new ResourceNotFoundException("File not found: " + filename);
        }
    }

    public void deleteFile(String filename) {
        if (!StringUtils.hasText(filename) || filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            return;
        }
        try {
            Path filePath = this.rootLocation.resolve(filename).normalize();
            if (filePath.startsWith(this.rootLocation)) {
                Files.deleteIfExists(filePath);
            }
        } catch (IOException ignored) {
        }
    }

    public String getContentType(String filename) {
        String ext = getFileExtension(filename).toLowerCase();
        switch (ext) {
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "webp":
                return "image/webp";
            default:
                return "application/octet-stream";
        }
    }

    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1);
    }

    private void validateMagicBytes(MultipartFile file, String extension) {
        try (InputStream is = file.getInputStream()) {
            byte[] header = new byte[12];
            int bytesRead = is.read(header);
            if (bytesRead < 4) {
                throw new BadRequestException("Corrupted or empty image file");
            }

            boolean isValid = false;
            String lowerExt = extension.toLowerCase();

            if (lowerExt.equals("jpg") || lowerExt.equals("jpeg")) {
                // JPEG starts with FF D8 FF
                isValid = (header[0] == (byte) 0xFF && header[1] == (byte) 0xD8 && header[2] == (byte) 0xFF);
            } else if (lowerExt.equals("png")) {
                // PNG starts with 89 50 4E 47 0D 0A 1A 0A
                isValid = (bytesRead >= 8 &&
                        header[0] == (byte) 0x89 && header[1] == (byte) 0x50 &&
                        header[2] == (byte) 0x4E && header[3] == (byte) 0x47 &&
                        header[4] == (byte) 0x0D && header[5] == (byte) 0x0A &&
                        header[6] == (byte) 0x1A && header[7] == (byte) 0x0A);
            } else if (lowerExt.equals("webp")) {
                // WebP starts with 'RIFF' and bytes 8..11 is 'WEBP'
                isValid = (bytesRead >= 12 &&
                        header[0] == (byte) 0x52 && header[1] == (byte) 0x49 &&
                        header[2] == (byte) 0x46 && header[3] == (byte) 0x46 &&
                        header[8] == (byte) 0x57 && header[9] == (byte) 0x45 &&
                        header[10] == (byte) 0x42 && header[11] == (byte) 0x50);
            }

            if (!isValid) {
                throw new BadRequestException("File signature does not match claimed " + extension.toUpperCase() + " image format");
            }
        } catch (IOException e) {
            throw new BadRequestException("Unable to read image file content: " + e.getMessage());
        }
    }
}
