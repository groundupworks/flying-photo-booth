/*
 * This file is part of Flying PhotoBooth.
 * 
 * Flying PhotoBooth is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Flying PhotoBooth is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Flying PhotoBooth.  If not, see <http://www.gnu.org/licenses/>.
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
