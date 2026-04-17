package com.github.dgaponov99.practicum.myblog.exception;

import java.util.UUID;

public class ImageNotFoundException extends Exception {

    public ImageNotFoundException(UUID imageUuid) {
        super("Image with id %s does not exist".formatted(imageUuid));
    }

}
