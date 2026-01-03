package com.uth.confms.submission.exception;

public class SubmissionException extends RuntimeException {
    public SubmissionException(String message) {
        super(message);
    }
    
    public SubmissionException(String message, Throwable cause) {
        super(message, cause);
    }
}




