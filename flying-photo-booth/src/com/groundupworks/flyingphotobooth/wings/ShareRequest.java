package com.groundupworks.flyingphotobooth.wings;

/**
 * A model object representing a share request.
 */
public class ShareRequest {

    //
    // Valid values for destination.
    //

    public static final int DESTINATION_FACEBOOK = 0;

    public static final int DESTINATION_DROPBOX = 1;

    //
    // Valid values for state. Package private because they are internally used.
    //

    static final int STATE_PENDING = 0;

    static final int STATE_PROCESSING = 1;

    static final int STATE_PROCESSED = 2;

    /**
     * The record id.
     */
    private int mId;

    /**
     * The local path to the file to share.
     */
    private String mFilePath;

    /**
     * The destination of the share.
     */
    private int mDestination;

    /**
     * Package private constructor.
     * 
     * @param id
     *            the record id.
     * @param filePath
     *            the local path to the file to share.
     * @param destination
     *            the destination of the share.
     */
    ShareRequest(int id, String filePath, int destination) {
        mId = id;
        mFilePath = filePath;
        mDestination = destination;
    }

    //
    // Public methods.
    //

    /**
     * @return the record id.
     */
    public int getId() {
        return mId;
    }

    /**
     * @return the local path to the file to share.
     */
    public String getFilePath() {
        return mFilePath;
    }

    /**
     * @return the destination of the share.
     */
    public int getDestination() {
        return mDestination;
    }
}
