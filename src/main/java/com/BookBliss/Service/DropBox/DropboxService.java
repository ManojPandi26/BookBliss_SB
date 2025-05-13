package com.BookBliss.Service.DropBox;

import com.dropbox.core.DbxException;
import com.dropbox.core.InvalidAccessTokenException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.WriteMode;
import com.dropbox.core.v2.sharing.SharedLinkMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
public class DropboxService {
    
    private final DbxClientV2 dropboxClient;
    private static final String DROPBOX_PATH_PREFIX = "/BookBliss/profiles/";

    public String uploadFile(MultipartFile file, String prefix) throws IOException {
        validateDropboxConnection();
        
        String fileName = generateUniqueFileName(file.getOriginalFilename(), prefix);
        String dropboxPath = DROPBOX_PATH_PREFIX + fileName;

        try (InputStream inputStream = file.getInputStream()) {
            FileMetadata metadata = dropboxClient.files().uploadBuilder(dropboxPath)
                .withMode(WriteMode.OVERWRITE)
                .uploadAndFinish(inputStream);

            // Create a shared link
            try {
                SharedLinkMetadata sharedLinkMetadata = dropboxClient.sharing()
                    .createSharedLinkWithSettings(dropboxPath);
                return convertToDirectLink(sharedLinkMetadata.getUrl());
            } catch (DbxException e) {
                // If sharing fails, try to delete the uploaded file
                try {
                    dropboxClient.files().deleteV2(dropboxPath);
                } catch (DbxException deleteError) {
                    log.error("Failed to delete file after sharing error: {}", deleteError.getMessage());
                }
                throw new RuntimeException("Failed to create shared link: " + e.getMessage(), e);
            }
        } catch (DbxException e) {
            handleDropboxException(e);
            return null; // This line won't be reached due to exception handling
        }
    }
    
    
    private void validateDropboxConnection() {
        try {
            // Test the connection by trying to get account info
            dropboxClient.users().getCurrentAccount();
        } catch (InvalidAccessTokenException e) {
            throw new RuntimeException("Invalid Dropbox access token. Please check your configuration.", e);
        } catch (DbxException e) {
            throw new RuntimeException("Failed to connect to Dropbox. Please check your internet connection and configuration.", e);
        }
    }

    public void deleteFile(String dropboxPath) throws DbxException {
        try {
            dropboxClient.files().deleteV2(dropboxPath);
        } catch (DbxException e) {
            log.error("Error deleting file from Dropbox: {}", e.getMessage());
            throw e;
        }
    }

    private void handleDropboxException(DbxException e) {
        if (e instanceof InvalidAccessTokenException) {
            throw new RuntimeException("Invalid Dropbox access token. Please check your configuration.", e);
        } else if (e.getMessage().contains("path/not_found")) {
            throw new RuntimeException("Dropbox folder not found. Please check if the folder exists.", e);
        } else if (e.getMessage().contains("insufficient_space")) {
            throw new RuntimeException("Insufficient space in Dropbox account.", e);
        } else {
            throw new RuntimeException("Dropbox operation failed: " + e.getMessage(), e);
        }
    }
    
    private String generateUniqueFileName(String originalFileName, String prefix) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        return prefix + "_" + timeStamp + extension;
    }

    private String convertToDirectLink(String sharedLink) {
        // Convert shared link to direct link
        // Replace "www.dropbox.com" with "dl.dropboxusercontent.com"
        return sharedLink.replace("www.dropbox.com", "dl.dropboxusercontent.com")
            .replace("?dl=0", "");
    }

    public String extractPathFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }

        // Extract the path from the Dropbox URL
        String[] parts = url.split("/BookBliss/profiles/");
        if (parts.length > 1) {
            return DROPBOX_PATH_PREFIX + parts[1];
        }
        return null;
    }
}
