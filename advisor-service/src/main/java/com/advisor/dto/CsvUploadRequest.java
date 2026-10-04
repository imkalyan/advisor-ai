package com.advisor.dto;

import org.springframework.web.multipart.MultipartFile;

public class CsvUploadRequest {
    private MultipartFile file;

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }
}