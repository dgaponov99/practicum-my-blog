package com.github.dgaponov99.practicum.myblog.service;

import com.github.dgaponov99.practicum.myblog.exception.ImageNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
public class PostImageService {

    @Value("${post.image.directory:./images}")
    private String postImageDirectoryPath;

    public UUID saveImage(InputStream inputStream) {
        var imageUuid = generateImageUuid();
        try (var imageBos = new BufferedOutputStream(new FileOutputStream(getImagePath(imageUuid)))) {
            inputStream.transferTo(imageBos);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return imageUuid;
    }

    public void deleteImage(UUID imageUuid) {
        try {
            Files.deleteIfExists(Path.of(getImagePath(imageUuid)));
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public InputStream getImage(UUID imageUuid) throws ImageNotFoundException {
        var imagePath = Path.of(getImagePath(imageUuid));
        if (Files.notExists(imagePath)) {
            throw new ImageNotFoundException(imageUuid);
        }
        try {
            return Files.newInputStream(Path.of(getImagePath(imageUuid)));
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private String getImagePath(UUID imageUuid) {
        return postImageDirectoryPath + File.pathSeparator + imageUuid;
    }

    private UUID generateImageUuid() {
        return UUID.randomUUID();
    }

}
