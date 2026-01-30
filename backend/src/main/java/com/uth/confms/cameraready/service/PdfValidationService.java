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

    /**
     * Kiểm tra tính hợp lệ của file PDF (số trang, kích thước, định dạng).
     */
    ValidationResultDTO validate(MultipartFile file);

    /**
     * Lấy số trang của file PDF.
     */
    int getPageCount(byte[] pdfContent) throws Exception;

    /**
     * Lấy kích thước trang của file PDF (ví dụ: A4, Letter).
     */
    String getPageSize(byte[] pdfContent) throws Exception;
}
