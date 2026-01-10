package com.uth.confms.cameraready.service;

import com.uth.confms.cameraready.dto.ValidationResultDTO;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service interface cho kiểm tra PDF camera-ready.
 * 
 * @author Anh Đức
 * @version 1.0.0
 */
public interface PdfValidationService {

    ValidationResultDTO validate(MultipartFile file);

    int getPageCount(byte[] pdfContent) throws Exception;

    String getPageSize(byte[] pdfContent) throws Exception;
}
