package com.neuromart.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class CloudinaryService {

    private static final Logger log = LoggerFactory.getLogger(CloudinaryService.class);
    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public Map<String, String> upload(String source, String folder, Integer width, String crop) {
        if (source == null || source.isBlank()) {
            return Map.of("public_id", "default_avatar_id", "url", "https://via.placeholder.com/150");
        }

        try {
            Map<String, Object> params = new HashMap<>();
            params.put("folder", folder);
            if (width != null) {
                params.put("width", width);
            }
            if (crop != null) {
                params.put("crop", crop);
            }

            Map<?, ?> uploadResult = cloudinary.uploader().upload(source, params);
            String publicId = (String) uploadResult.get("public_id");
            String secureUrl = (String) uploadResult.get("secure_url");

            return Map.of("public_id", publicId != null ? publicId : "", "url", secureUrl != null ? secureUrl : "");
        } catch (IOException e) {
            log.error("Cloudinary upload failed: {}", e.getMessage());
            // Fallback so development without active Cloudinary credentials does not crash
            return Map.of("public_id", "local_" + System.currentTimeMillis(), "url", source.startsWith("http") ? source : "https://via.placeholder.com/300");
        }
    }

    public Map<String, String> upload(String source, String folder) {
        return upload(source, folder, null, null);
    }

    public void destroy(String publicId) {
        if (publicId == null || publicId.isBlank() || "default_avatar_id".equals(publicId) || publicId.startsWith("local_")) {
            return;
        }
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (Exception e) {
            log.warn("Cloudinary delete failed for publicId {}: {}", publicId, e.getMessage());
        }
    }
}
