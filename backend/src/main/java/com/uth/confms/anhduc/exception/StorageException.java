package com.uth.confms.anhduc.exception;

/**
 * Exception khi có lỗi lưu trữ file.
 * 
 * @author Anh Đức
 * @version 1.0.0
 */
public class StorageException extends RuntimeException {

    public StorageException(String message) {
        super(message);
    }

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }

    public static StorageException storeError(String filename, Throwable cause) {
        return new StorageException("Không thể lưu file: " + filename, cause);
    }

    public static StorageException loadError(String path, Throwable cause) {
        return new StorageException("Không thể đọc file: " + path, cause);
    }

    public static StorageException fileNotFound(String path) {
        return new StorageException("File không tồn tại: " + path);
    }

    public static StorageException deleteError(String path, Throwable cause) {
        return new StorageException("Không thể xóa file: " + path, cause);
    }
}
