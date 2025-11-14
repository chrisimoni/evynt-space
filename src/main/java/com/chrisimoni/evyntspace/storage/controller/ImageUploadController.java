package com.chrisimoni.evyntspace.storage.controller;

import com.chrisimoni.evyntspace.common.dto.ApiResponse;
import com.chrisimoni.evyntspace.common.enums.FileType;
import com.chrisimoni.evyntspace.storage.dto.FileDetails;
import com.chrisimoni.evyntspace.storage.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/images")
@RequiredArgsConstructor
public class ImageUploadController {
    private final FileStorageService fileStorageService;

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<FileDetails> uploadFile(@RequestParam("file")MultipartFile file) {
        FileDetails fileDetails = fileStorageService.uploadFile(file, FileType.IMAGE);
        return ApiResponse.success("Image uploaded successfully", fileDetails);
    }
}
