package com.chrisimoni.evyntspace.storage.service;

import com.chrisimoni.evyntspace.common.enums.FileType;
import com.chrisimoni.evyntspace.common.exception.ExternalServiceException;
import com.chrisimoni.evyntspace.storage.dto.FileDetails;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import static com.chrisimoni.evyntspace.storage.util.FileUtil.validateFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryFileStorageServiceImpl implements FileStorageService {
    private final Cloudinary cloudinary;

    @Value("${cloudinary.folder-prefix}")
    private String baseDir;

    @Override
    public FileDetails uploadFile(MultipartFile file, FileType fileType) {
        validateFile(file, fileType);
        try {
            var result = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", baseDir,
                            "resource_type", "auto" // auto-detect type (image, video, raw)
                    ));
            String publicId = (String) result.get("public_id");
            String url = (String) result.get("secure_url");

            return new FileDetails(publicId, file.getOriginalFilename(), file.getContentType(), url);
        } catch(Exception e) {
            log.error("Failed to upload file to cloudinary {}", e.getMessage(), e);
            throw new ExternalServiceException("Failed to upload file to cloudinary", e);
        }

    }
}
