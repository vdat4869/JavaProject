package com.uth.confms.common.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.IOException;
import java.io.InputStream;
import java.text.Normalizer;

/**
 * Utility class for loading PDF fonts with Unicode support.
 * Uses embedded TrueType fonts to support Vietnamese and other non-Latin
 * characters.
 * Falls back to ASCII-safe text if fonts are not available.
 */
@Slf4j
public class PdfFontUtil {

    private static final String REGULAR_FONT_PATH = "/fonts/NotoSans-Regular.ttf";
    private static final String BOLD_FONT_PATH = "/fonts/NotoSans-Bold.ttf";

    /**
     * Load the regular font for the given document.
     * Falls back to Helvetica if custom font is not available.
     */
    public static PDFont loadRegularFont(PDDocument document) {
        try {
            InputStream fontStream = PdfFontUtil.class.getResourceAsStream(REGULAR_FONT_PATH);
            if (fontStream != null) {
                PDFont font = PDType0Font.load(document, fontStream);
                log.debug("Loaded custom regular font successfully");
                return font;
            } else {
                log.warn("Custom regular font not found at {}, using fallback", REGULAR_FONT_PATH);
            }
        } catch (IOException e) {
            log.warn("Failed to load custom regular font, falling back to Helvetica: {}", e.getMessage());
        }
        return new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    }

    /**
     * Load the bold font for the given document.
     * Falls back to Helvetica-Bold if custom font is not available.
     */
    public static PDFont loadBoldFont(PDDocument document) {
        try {
            InputStream fontStream = PdfFontUtil.class.getResourceAsStream(BOLD_FONT_PATH);
            if (fontStream != null) {
                PDFont font = PDType0Font.load(document, fontStream);
                log.debug("Loaded custom bold font successfully");
                return font;
            } else {
                log.warn("Custom bold font not found at {}, using fallback", BOLD_FONT_PATH);
            }
        } catch (IOException e) {
            log.warn("Failed to load custom bold font, falling back to Helvetica-Bold: {}", e.getMessage());
        }
        return new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
    }

    /**
     * Check if custom Unicode fonts are available.
     */
    public static boolean hasCustomFonts() {
        return PdfFontUtil.class.getResourceAsStream(REGULAR_FONT_PATH) != null
                && PdfFontUtil.class.getResourceAsStream(BOLD_FONT_PATH) != null;
    }

    /**
     * Sanitize text for PDF display when using fallback fonts.
     * Converts Vietnamese and other diacritical characters to ASCII equivalents.
     * This is used when custom fonts are not available.
     */
    public static String sanitizeForBasicLatin(String text) {
        if (text == null)
            return "";

        // Normalize to NFD (decomposed form) and remove diacritical marks
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);
        // Remove combining diacritical marks (Unicode block 0300-036F)
        String ascii = normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

        // Handle special Vietnamese characters that don't decompose well
        ascii = ascii.replace("đ", "d").replace("Đ", "D");

        // Remove any remaining non-ASCII characters
        ascii = ascii.replaceAll("[^\\x00-\\x7F]", "");

        return ascii;
    }

    /**
     * Prepare text for PDF rendering.
     * Uses original text if custom fonts are loaded, otherwise sanitizes to ASCII.
     */
    public static String prepareText(String text, boolean hasUnicodeFont) {
        if (text == null)
            return "";
        if (hasUnicodeFont) {
            return text;
        }
        return sanitizeForBasicLatin(text);
    }
}
