package com.uth.confms.cameraready.service.impl;

import com.uth.confms.cameraready.dto.ValidationResultDTO;
import com.uth.confms.cameraready.service.PdfValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Implementation của PdfValidationService sử dụng Apache PDFBox.
 * 
 * @author Anh Đức
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PdfValidationServiceImpl implements PdfValidationService {

    @Value("${app.camera-ready.pdf.max-file-size:20971520}")
    private long maxFileSize;

    @Value("${app.camera-ready.pdf.max-pages:500}")
    private int maxPages;

    private static final float A4_WIDTH = 595f;
    private static final float A4_HEIGHT = 842f;
    private static final float LETTER_WIDTH = 612f;
    private static final float LETTER_HEIGHT = 792f;
    private static final float SIZE_TOLERANCE = 5f;

    @Override
    public ValidationResultDTO validate(MultipartFile file) {
        log.debug("Bắt đầu kiểm tra PDF: {}", file.getOriginalFilename());

        ValidationResultDTO result = ValidationResultDTO.builder()
                .passed(true)
                .fileSizeBytes(file.getSize())
                .build();

        // VR-001: Kiểm tra kích thước file
        if (file.getSize() > maxFileSize) {
            result.addError("FILE_SIZE_EXCEEDED",
                    String.format("Kích thước file (%s) vượt quá giới hạn (%s)",
                            formatFileSize(file.getSize()), formatFileSize(maxFileSize)));
        }

        // VR-002: Kiểm tra PDF hợp lệ
        try {
            byte[] content = file.getBytes();

            try (PDDocument document = Loader.loadPDF(content)) {

                // VR-003: Kiểm tra mã hóa
                if (document.isEncrypted()) {
                    result.addError("PDF_ENCRYPTED", "PDF không được bảo vệ bằng mật khẩu");
                }

                // VR-004: Kiểm tra số trang
                int pageCount = document.getNumberOfPages();
                result.setPageCount(pageCount);

                if (pageCount > maxPages) {
                    result.addError("PAGE_COUNT_EXCEEDED",
                            String.format("Số trang (%d) vượt quá giới hạn (%d)", pageCount, maxPages));
                }

                if (pageCount == 0) {
                    result.addError("EMPTY_PDF", "PDF không có trang nào");
                }

                // VR-005: Kiểm tra kích thước trang
                if (pageCount > 0) {
                    String pageSize = detectPageSize(document.getPage(0));
                    result.setPageSize(pageSize);

                    if ("UNKNOWN".equals(pageSize)) {
                        result.addWarning("UNKNOWN_PAGE_SIZE",
                                "Kích thước trang không phải A4 hoặc Letter");
                    }
                }

                // VR-006: Kiểm tra JavaScript
                if (document.getDocumentCatalog().getNames() != null &&
                        document.getDocumentCatalog().getNames().getJavaScript() != null) {
                    result.addWarning("CONTAINS_JAVASCRIPT", "PDF chứa JavaScript");
                }

                // VR-007: Kiểm tra file đính kèm
                if (document.getDocumentCatalog().getNames() != null &&
                        document.getDocumentCatalog().getNames().getEmbeddedFiles() != null) {
                    result.addWarning("CONTAINS_EMBEDDED_FILES", "PDF có file đính kèm");
                }
            }

        } catch (IOException e) {
            log.error("Lỗi khi kiểm tra PDF: {}", e.getMessage());
            result.addError("INVALID_PDF", "File không phải PDF hợp lệ");
        }

        result.setPassed(!result.hasErrors());

        log.debug("Kết quả kiểm tra: passed={}", result.isPassed());
        return result;
    }

    @Override
    public int getPageCount(byte[] pdfContent) throws Exception {
        try (PDDocument document = Loader.loadPDF(pdfContent)) {
            return document.getNumberOfPages();
        }
    }

    @Override
    public String getPageSize(byte[] pdfContent) throws Exception {
        try (PDDocument document = Loader.loadPDF(pdfContent)) {
            if (document.getNumberOfPages() > 0) {
                return detectPageSize(document.getPage(0));
            }
            return null;
        }
    }

    private String detectPageSize(PDPage page) {
        PDRectangle mediaBox = page.getMediaBox();
        float width = mediaBox.getWidth();
        float height = mediaBox.getHeight();

        if ((isWithinTolerance(width, A4_WIDTH) && isWithinTolerance(height, A4_HEIGHT)) ||
                (isWithinTolerance(width, A4_HEIGHT) && isWithinTolerance(height, A4_WIDTH))) {
            return "A4";
        }

        if ((isWithinTolerance(width, LETTER_WIDTH) && isWithinTolerance(height, LETTER_HEIGHT)) ||
                (isWithinTolerance(width, LETTER_HEIGHT) && isWithinTolerance(height, LETTER_WIDTH))) {
            return "LETTER";
        }

        return "UNKNOWN";
    }

    private boolean isWithinTolerance(float actual, float expected) {
        return Math.abs(actual - expected) <= SIZE_TOLERANCE;
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024)
            return bytes + " B";
        if (bytes < 1024 * 1024)
            return String.format("%.2f KB", bytes / 1024.0);
        return String.format("%.2f MB", bytes / (1024.0 * 1024));
    }
}
