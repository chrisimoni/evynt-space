package com.chrisimoni.evyntspace.storage.util;

import com.chrisimoni.evyntspace.common.enums.FileType;
import com.chrisimoni.evyntspace.common.exception.BadRequestException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class FileUtil {
    private static final List<String> ALLOWED_IMAGE_EXTENSIONS = List.of("jpg", "jpeg", "png", "webp");

    public static void validateFile(MultipartFile file, FileType fileType) {
        if(file == null || file.isEmpty()) {
            throw new BadRequestException("File cannot be empty");
        }

        if(FileType.IMAGE.equals(fileType)) {
            validateImage(file);
        }
    }

    private static void validateImage(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        assert originalFilename != null;
        String extension = getFileExtension(originalFilename);
        if(extension != null && !ALLOWED_IMAGE_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new BadRequestException(
                    "Invalid file type: only JPG, JPEG, and PNG image files are allowed.");
        }
    }

    private static String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return null;
        }
        return filename.substring(lastDotIndex + 1);
    }
}
