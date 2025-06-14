package com.chrisimoni.evyntspace.storage.service;

import com.chrisimoni.evyntspace.common.enums.FileType;
import com.chrisimoni.evyntspace.storage.dto.FileDetails;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    FileDetails uploadFile(MultipartFile multipartFile, FileType fileType);
}
