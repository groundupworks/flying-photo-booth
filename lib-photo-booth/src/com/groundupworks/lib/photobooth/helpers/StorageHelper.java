/*
 * Copyright (C) 2012 Benedict Lau
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.groundupworks.lib.photobooth.helpers;

import android.os.Environment;
import android.util.Base64;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * A helper class containing methods to read and write to external storage.
 *
 * @author Benedict Lau
 */
public class StorageHelper {

    /**
     * Encoding used to generate a valid filename.
     */
    private static final String DEFAULT_ENCODING = "UTF-8";

    /**
     * Hash algorithm used to generate a valid filename.
     */
    private static final String DEFAULT_HASH_ALGORITHM = "SHA-1";

    //
    // Public methods.
    //

    /**
     * Checks if external storage is available.
     *
     * @return true if available; false otherwise.
     */
    public static boolean isExternalStorageAvailable() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * Generates a unique and valid filename based on the input text. The same input text always gives
     * consistent output.
     *
     * @param text the input text. Must not be null.
     * @return a valid filename; or null if failed.
     */
    public static String generateValidFilename(String text) {
        String filename = null;
        byte[] digest = null;

        try {
            // Get bytes from input text.
            final byte[] bytes = text.getBytes(DEFAULT_ENCODING);

            // Generate unique hash from bytes.
            MessageDigest digester = MessageDigest.getInstance(DEFAULT_HASH_ALGORITHM);
            digester.update(bytes, 0, bytes.length);
            digest = digester.digest();
        } catch (NoSuchAlgorithmException e) {
            // Do nothing.
        } catch (UnsupportedEncodingException e) {
            // Do nothing.
        }

        if (digest != null && digest.length > 0) {
            // Encode bytes to valid characters for a filename.
            filename = Base64.encodeToString(digest, Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING);
        }

        return filename;
    }

    /**
     * Gets the full path to the writable directory in external storage. The directory will be created if it does not
     * exist.
     *
     * @param folder the directory relative to external storage root.
     * @return the full path to the directory; or null if unsuccessful.
     */
    public synchronized static String getDirectory(String folder) {
        String directoryPath = null;

        // Check if we currently have read and write access to the external storage.
        if (isExternalStorageAvailable()) {
            // Set the path to the default image directory.
            directoryPath = Environment.getExternalStorageDirectory() + folder;
            File directory = new File(directoryPath);

            // Check if directory has already been created.
            if (!directory.exists()) {
                // Create directory if it does not exist.
                if (!directory.mkdir()) {
                    // If directory creation fails, set path to null for return to indicate error.
                    directoryPath = null;
                }
            }
        }

        return directoryPath;
    }
}
