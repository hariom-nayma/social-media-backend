package com.socialmedia.app.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;
    public CloudinaryService(Cloudinary cloudinary) { this.cloudinary = cloudinary; }

    public String uploadImage(MultipartFile file) {
        try {
            Map upload = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap("folder", "social_media_app/profile_images",
                            "flags", "progressive",
                            "quality", "auto"));
            return upload.get("secure_url").toString();
        } catch (IOException e) {
            throw new RuntimeException("Image upload failed: " + e.getMessage());
        }
    }

    public void deleteImage(String publicId) {
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (IOException e) {
            throw new RuntimeException("Image delete failed: " + e.getMessage());
        }
    }
}
