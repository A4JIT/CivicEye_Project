package com.civiceye.ai.complaint;

import com.civiceye.ai.exception.BadRequestException;
import com.civiceye.ai.exception.ResourceNotFoundException;
import com.civiceye.ai.storage.FileStorageService;
import com.civiceye.ai.storage.StorageProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileStorageServiceTest {

    @TempDir
    Path tempDir;

    private FileStorageService fileStorageService;

    @BeforeEach
    void setUp() {
        StorageProperties properties = new StorageProperties();
        properties.setLocation(tempDir.toString());
        properties.setMaxFileSize(2 * 1024 * 1024); // 2 MB
        fileStorageService = new FileStorageService(properties);
        fileStorageService.init();
    }

    @Test
    @DisplayName("Should successfully store valid JPEG image")
    void storeFile_ValidJpeg_Success() {
        byte[] jpegBytes = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0x00, 0x10, 0x4A, 0x46};
        MockMultipartFile file = new MockMultipartFile("image", "evidence.jpg", "image/jpeg", jpegBytes);

        String savedFilename = fileStorageService.storeFile(file);

        assertNotNull(savedFilename);
        assertTrue(savedFilename.endsWith(".jpg"));
        Resource resource = fileStorageService.loadAsResource(savedFilename);
        assertTrue(resource.exists());
    }

    @Test
    @DisplayName("Should successfully store valid PNG image")
    void storeFile_ValidPng_Success() {
        byte[] pngBytes = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00, 0x00};
        MockMultipartFile file = new MockMultipartFile("image", "photo.png", "image/png", pngBytes);

        String savedFilename = fileStorageService.storeFile(file);

        assertNotNull(savedFilename);
        assertTrue(savedFilename.endsWith(".png"));
    }

    @Test
    @DisplayName("Should reject empty file")
    void storeFile_EmptyFile_ThrowsException() {
        MockMultipartFile emptyFile = new MockMultipartFile("image", "empty.jpg", "image/jpeg", new byte[0]);

        assertThrows(BadRequestException.class, () -> fileStorageService.storeFile(emptyFile));
    }

    @Test
    @DisplayName("Should reject oversized file")
    void storeFile_OversizedFile_ThrowsException() {
        byte[] oversizedBytes = new byte[3 * 1024 * 1024]; // 3 MB > 2 MB
        oversizedBytes[0] = (byte) 0xFF;
        oversizedBytes[1] = (byte) 0xD8;
        oversizedBytes[2] = (byte) 0xFF;

        MockMultipartFile file = new MockMultipartFile("image", "huge.jpg", "image/jpeg", oversizedBytes);

        assertThrows(BadRequestException.class, () -> fileStorageService.storeFile(file));
    }

    @Test
    @DisplayName("Should reject unsupported file extensions (e.g., .exe, .sh)")
    void storeFile_DisallowedExtension_ThrowsException() {
        MockMultipartFile file = new MockMultipartFile("image", "script.sh", "text/x-shellscript", new byte[]{1, 2, 3, 4});

        assertThrows(BadRequestException.class, () -> fileStorageService.storeFile(file));
    }

    @Test
    @DisplayName("Should reject file when file extension is valid but magic bytes do not match")
    void storeFile_SpoofedExtension_ThrowsException() {
        byte[] fakeBytes = "Hello this is a plain text file pretending to be jpg".getBytes();
        MockMultipartFile file = new MockMultipartFile("image", "fake.jpg", "image/jpeg", fakeBytes);

        assertThrows(BadRequestException.class, () -> fileStorageService.storeFile(file));
    }

    @Test
    @DisplayName("Should reject path traversal in original filename")
    void storeFile_PathTraversal_ThrowsException() {
        byte[] jpegBytes = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0x00, 0x10};
        MockMultipartFile file = new MockMultipartFile("image", "../../../evil.jpg", "image/jpeg", jpegBytes);

        assertThrows(BadRequestException.class, () -> fileStorageService.storeFile(file));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException for non-existent file")
    void loadAsResource_NotFound_ThrowsException() {
        assertThrows(ResourceNotFoundException.class, () -> fileStorageService.loadAsResource("non_existent_file.jpg"));
    }

    @Test
    @DisplayName("Should return correct content type based on extension")
    void getContentType_ResolvesAccurately() {
        assertEquals("image/jpeg", fileStorageService.getContentType("photo.jpg"));
        assertEquals("image/png", fileStorageService.getContentType("photo.png"));
        assertEquals("image/webp", fileStorageService.getContentType("photo.webp"));
    }
}
