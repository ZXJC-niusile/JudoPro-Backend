package cn.edu.bistu.cs.ir.utils;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class PhotoDownloader {

    private static final Logger log = LoggerFactory.getLogger(PhotoDownloader.class);
    private final OkHttpClient httpClient;

    // Pattern to sanitize filenames, allowing alphanumeric, spaces, hyphens, underscores
    // and replacing others with an underscore.
    private static final Pattern SANITIZE_PATTERN = Pattern.compile("[^a-zA-Z0-9\\s\\-_\\.]");
    private static final int MAX_FILENAME_LENGTH = 100; // Max length for a component of the filename

    public PhotoDownloader() {
        this.httpClient = new OkHttpClient.Builder()
                .followRedirects(true)
                .build();
    }

    /**
     * Downloads a photo from the given URL and saves it to a specified path.
     * The filename is constructed based on judoka name and photo title.
     *
     * @param photoUrl         The URL of the photo to download.
     * @param judokaName       The name of the judoka, used for creating a subdirectory.
     * @param photoTitle       The title of the photo, used for generating the filename.
     * @param baseDownloadPath The base directory where photos will be saved.
     * @return Path to the downloaded file, or null if download failed.
     */
    public Path downloadPhoto(String photoUrl, String judokaName, String photoTitle, String baseDownloadPath) {
        if (photoUrl == null || photoUrl.isEmpty()) {
            log.warn("Photo URL is empty, skipping download for title: {}", photoTitle);
            return null;
        }

        try {
            // Sanitize judokaName for directory creation
            String sanitizedJudokaName = sanitizePathComponent(judokaName, "unknown_judoka");
            Path judokaDir = Paths.get(baseDownloadPath, sanitizedJudokaName);
            Files.createDirectories(judokaDir); // Ensure directory exists

            // Construct filename from photoTitle (initial simple version)
            // More sophisticated parsing will be done in a dedicated method (Step 4)
            String filename = constructFilename(photoTitle, photoUrl);

            Path destinationFile = judokaDir.resolve(filename);

            // Prevent excessively long paths/filenames (though OS limits are usually higher)
            if (destinationFile.toString().length() > 255) {
                log.warn("Generated filepath is too long, truncating filename for photo: {}", photoUrl);
                filename = "truncated_" + filename.substring(Math.max(0, filename.length() - MAX_FILENAME_LENGTH + 10)); // ensure "truncated_" fits
                destinationFile = judokaDir.resolve(filename);

            }


            Request request = new Request.Builder().url(photoUrl).build();
            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful() || response.body() == null) {
                    log.error("Failed to download photo from {}: HTTP {}", photoUrl, response.code());
                    return null;
                }
                ResponseBody body = response.body();
                try (InputStream inputStream = body.byteStream()) {
                    Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
                    log.info("Successfully downloaded photo {} to {}", photoUrl, destinationFile);
                    return destinationFile;
                }
            }
        } catch (IOException e) {
            log.error("IOException while downloading photo {} or creating directories: {}", photoUrl, e.getMessage(), e);
            return null;
        } catch (Exception e) {
            log.error("Unexpected error while downloading photo {}: {}", photoUrl, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Constructs a filename from the photo title and URL.
     * This is a basic implementation. Step 4 will involve more detailed parsing.
     *
     * @param photoTitle The title of the photo.
     * @param photoUrl   The URL of the photo (used for extension and fallback).
     * @return A sanitized filename.
     */
    private String constructFilename(String photoTitle, String photoUrl) {
        String baseName;
        if (photoTitle != null && !photoTitle.trim().isEmpty()) {
            baseName = parsePhotoTitleForFilename(photoTitle);
        } else {
            // Fallback if title is empty: use filename from URL
            String urlFilenameComponent = Paths.get(photoUrl).getFileName().toString();
            // Remove extension from URL filename for baseName
            int dotIndex = urlFilenameComponent.lastIndexOf('.');
            if (dotIndex > 0) {
                urlFilenameComponent = urlFilenameComponent.substring(0, dotIndex);
            }
            baseName = sanitizePathComponent(urlFilenameComponent, "untitled_photo_from_url");
        }

        // Truncate baseName if too long before adding extension
        // Ensure baseName itself is not overly long after parsing
        if (baseName.length() > MAX_FILENAME_LENGTH - 10) { // reserve space for extension, dot, and potential counter
            baseName = baseName.substring(0, MAX_FILENAME_LENGTH - 10);
        }
        // Remove trailing underscores that might have resulted from truncation or parsing
        baseName = baseName.replaceAll("_+$", "");


        String extension = ".jpg"; // Default extension
        String urlFileName = Paths.get(photoUrl).getFileName().toString();
        int lastDot = urlFileName.lastIndexOf('.');
        if (lastDot > 0 && lastDot < urlFileName.length() - 1) {
            String detectedExtension = urlFileName.substring(lastDot).toLowerCase();
            // Allow common image extensions
            if (detectedExtension.matches("\\.(jpg|jpeg|png|gif|webp)")) {
                extension = detectedExtension;
            }
        }
        return baseName + extension;
    }

    /**
     * Parses the photo title to extract relevant details for a structured filename.
     * Attempts to identify event name, year, location, category, athletes, etc.
     *
     * @param photoTitle The original title of the photo.
     * @return A structured filename string (without extension).
     */
    private String parsePhotoTitleForFilename(String photoTitle) {
        if (photoTitle == null || photoTitle.trim().isEmpty()) {
            return "untitled_photo";
        }

        StringBuilder filenameBuilder = new StringBuilder();
        String title = photoTitle.trim();

        // Simple approach: split by common delimiters and try to identify parts.
        // This will be highly dependent on the consistency of photo titles.
        // Example title: "Paris Grand Slam 2023 - DAY 1 - -73kg - DOE John (USA) vs YAMAMOTO Masashi (JPN) - Action"
        // Example title: "World Championships Seniors Baku 2018 - ONO Shohei (JPN) - Gold Medal"
        // Example title: "Antalya Grand Slam 2024, Final, -60kg, NAGAYAMA Ryuju (JPN), Bronze"


        // Attempt to extract year first (e.g., 2023, 2024)
        Pattern yearPattern = Pattern.compile("\\b(20\\d{2})\\b");
        Matcher yearMatcher = yearPattern.matcher(title);
        String year = "";
        if (yearMatcher.find()) {
            year = yearMatcher.group(1);
            //title = title.replace(year, "").trim(); // Remove year to simplify further parsing
        }

        // Attempt to extract weight category (e.g., -73kg, +100kg, U73)
        Pattern kgPattern = Pattern.compile("\\b([Uu]?[-\\+]?\\d{2,3}kg)\\b");
        Matcher kgMatcher = kgPattern.matcher(title);
        String category = "";
        if (kgMatcher.find()) {
            category = kgMatcher.group(1).replaceAll("[^a-zA-Z0-9\\+]", "").toLowerCase(); // Sanitize: u73kg, p100kg
           // title = title.replace(kgMatcher.group(1), "").trim();
        }

        // Split title by common delimiters like " - ", ", "
        // Prioritize longer delimiters first
        String[] parts = title.split("\\s*-\\s*|,\\s*|\\s*\\|\\s*");
        List<String> significantParts = new ArrayList<>();
        for (String part : parts) {
            String sanitizedPart = sanitizePathComponent(part.trim(), null);
            if (sanitizedPart != null && !sanitizedPart.isEmpty() &&
                !sanitizedPart.equalsIgnoreCase("action") &&
                !sanitizedPart.equalsIgnoreCase("highlights") &&
                !sanitizedPart.equalsIgnoreCase("gallery") &&
                !sanitizedPart.equalsIgnoreCase(year) && // Avoid re-adding if already found
                !sanitizedPart.toLowerCase().contains(category) && // Avoid re-adding if already found
                !sanitizedPart.matches("(?i)day\\s*\\d+") // Remove "DAY X"
            ) {
                significantParts.add(sanitizedPart);
            }
        }

        // Construct filename: Year_Category_Part1_Part2...
        if (!year.isEmpty()) {
            filenameBuilder.append(year).append("_");
        }
        if (!category.isEmpty()) {
            filenameBuilder.append(category).append("_");
        }

        for (int i = 0; i < significantParts.size(); i++) {
            filenameBuilder.append(significantParts.get(i));
            if (i < significantParts.size() - 1) {
                filenameBuilder.append("_");
            }
        }

        if (filenameBuilder.length() == 0 || filenameBuilder.toString().matches("_+")) {
            // Fallback to a more generic sanitized title if parsing yields nothing
            return sanitizePathComponent(photoTitle, "parsed_untitled_photo");
        }

        // Remove trailing underscores that might have resulted from construction
        String finalFilename = filenameBuilder.toString().replaceAll("_+$", "").replaceAll("__+", "_");

        return finalFilename.isEmpty() ? sanitizePathComponent(photoTitle, "fallback_title") : finalFilename;
    }


    /**
     * Sanitizes a string to be used as a file or directory path component.
     * Replaces illegal characters with underscores and limits length.
     *
     * @param name         The string to sanitize.
     * @param defaultValue The default value to use if the sanitized name is empty.
     * @return A sanitized string suitable for use in a file path.
     */
    public static String sanitizePathComponent(String name, String defaultValue) {
        if (name == null || name.trim().isEmpty()) {
            return defaultValue;
        }
        // Normalize whitespace (replace multiple spaces with one, trim)
        String sanitized = name.trim().replaceAll("\\s+", " ");
        // Replace problematic characters
        sanitized = SANITIZE_PATTERN.matcher(sanitized).replaceAll("_");

        // Ensure filename is not just dots or empty after sanitization
        if (sanitized.isEmpty() || sanitized.matches("^\\.*$")) {
            return defaultValue;
        }
        // Limit length
        if (sanitized.length() > MAX_FILENAME_LENGTH) {
            sanitized = sanitized.substring(0, MAX_FILENAME_LENGTH);
        }
        // Trim trailing underscores that might result from truncation or replacements
        sanitized = sanitized.replaceAll("_+$", "");
        if (sanitized.isEmpty()) {
             return defaultValue; // if it became empty after trailing underscore removal
        }

        return sanitized;
    }
}
