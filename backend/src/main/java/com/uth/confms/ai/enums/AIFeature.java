package com.uth.confms.ai.enums;

/**
 * Enum định nghĩa các tính năng AI có sẵn trong hệ thống.
 * Mỗi tính năng có thể được bật/tắt riêng theo từng conference.
 */
public enum AIFeature {
    /** Kiểm tra chính tả và ngữ pháp cho title/abstract */
    SPELL_CHECK("spell_check", "Spell & Grammar Check", "AUTHOR"),

    /** Gợi ý cải thiện abstract */
    ABSTRACT_POLISH("abstract_polish", "Abstract Polishing", "AUTHOR"),

    /** Đề xuất keywords từ nội dung bài */
    KEYWORD_SUGGEST("keyword_suggest", "Keyword Suggestion", "AUTHOR"),

    /** Tạo tóm tắt trung lập cho PC bidding */
    NEUTRAL_SUMMARY("neutral_summary", "Neutral Summary", "PC"),

    /** Trích xuất các điểm chính từ abstract */
    KEY_POINTS("key_points", "Key Point Extraction", "PC"),

    /** Gợi ý độ tương đồng reviewer-paper */
    SIMILARITY_HINT("similarity_hint", "Reviewer-Paper Similarity", "CHAIR"),

    /** Soạn thảo email thông báo */
    EMAIL_DRAFT("email_draft", "Email Drafting", "CHAIR");

    private final String code;
    private final String displayName;
    private final String primaryRole;

    AIFeature(String code, String displayName, String primaryRole) {
        this.code = code;
        this.displayName = displayName;
        this.primaryRole = primaryRole;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getPrimaryRole() {
        return primaryRole;
    }

    public static AIFeature fromCode(String code) {
        for (AIFeature feature : values()) {
            if (feature.code.equals(code)) {
                return feature;
            }
        }
        throw new IllegalArgumentException("Unknown AI feature code: " + code);
    }
}
