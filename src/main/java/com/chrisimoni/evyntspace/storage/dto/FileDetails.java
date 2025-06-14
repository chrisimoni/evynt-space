package com.chrisimoni.evyntspace.storage.dto;

import java.net.URL;

public record FileDetails(
        String publicId,
        String fileName,
        String fileType,
        String fileUrl
) {
}
