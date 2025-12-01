package com.github.dgaponov99.practicum.myblog.service;

import com.github.dgaponov99.practicum.myblog.exception.ImageNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class PostImageService {

    @Value("${post.image.directory:images}")
    private String postImageDirectoryPath;

    public UUID saveImage(InputStream inputStream) {
        try {
            Path uploadDir = Paths.get(postImageDirectoryPath);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            var imageUuid = generateImageUuid();
            var imagePath = getImagePath(imageUuid);
            try (var imageFileBos = new BufferedOutputStream(new FileOutputStream(imagePath.toFile()))) {
                inputStream.transferTo(imageFileBos);
            }
            return imageUuid;
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public void deleteImage(UUID imageUuid) {
        try {
            Files.deleteIfExists(getImagePath(imageUuid));
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public InputStream getImage(UUID imageUuid) throws ImageNotFoundException {
        if (Files.notExists(getImagePath(imageUuid))) {
            throw new ImageNotFoundException(imageUuid);
        }
        try {
            return Files.newInputStream(getImagePath(imageUuid));
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private Path getImagePath(UUID imageUuid) {
        return Path.of(postImageDirectoryPath).resolve(imageUuid.toString()).normalize();
    }

    private UUID generateImageUuid() {
        return UUID.randomUUID();
    }

}
