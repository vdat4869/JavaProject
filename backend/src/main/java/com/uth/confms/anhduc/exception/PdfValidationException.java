package com.uth.confms.anhduc.exception;

import com.uth.confms.anhduc.modules.validation.dto.ValidationResultDTO;
import lombok.Getter;

/**
 * Exception khi PDF không hợp lệ.
 * 
 * @author Anh Đức
 * @version 1.0.0
 */
@Getter
public class PdfValidationException extends RuntimeException {

    private final String errorCode;
    private final ValidationResultDTO validationResult;

    public PdfValidationException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.validationResult = null;
    }

    public PdfValidationException(String errorCode, String message, ValidationResultDTO validationResult) {
        super(message);
        this.errorCode = errorCode;
        this.validationResult = validationResult;
    }

    public static PdfValidationException fileSizeExceeded(long actualSize, long maxSize) {
        return new PdfValidationException("FILE_SIZE_EXCEEDED",
                String.format("Kích thước file (%d bytes) vượt quá giới hạn (%d bytes)", actualSize, maxSize));
    }

    public static PdfValidationException invalidPdf() {
        return new PdfValidationException("INVALID_PDF", "File không phải PDF hợp lệ");
    }

    public static PdfValidationException pdfEncrypted() {
        return new PdfValidationException("PDF_ENCRYPTED", "PDF không được bảo vệ bằng mật khẩu");
    }

    public static PdfValidationException pageCountExceeded(int actualPages, int maxPages) {
        return new PdfValidationException("PAGE_COUNT_EXCEEDED",
                String.format("Số trang (%d) vượt quá giới hạn (%d)", actualPages, maxPages));
    }

    public static PdfValidationException invalidPageSize(String actualSize) {
        return new PdfValidationException("INVALID_PAGE_SIZE",
                String.format("Kích thước trang không hợp lệ: %s", actualSize));
    }
}
