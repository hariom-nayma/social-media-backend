package com.socialmedia.app.util;


import java.util.UUID;

import com.socialmedia.app.exception.InvalidUUIDException;

public final class UuidUtil {
    private UuidUtil() {}

    public static UUID parse(String idStr) {
        try {
            return UUID.fromString(idStr);
        } catch (IllegalArgumentException e) {
            throw new InvalidUUIDException("Invalid UUID: " + idStr);
        }
    }
}

