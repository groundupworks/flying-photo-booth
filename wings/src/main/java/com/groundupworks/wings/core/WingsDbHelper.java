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
package com.groundupworks.wings.core;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.groundupworks.wings.IWingsLogger;
import com.groundupworks.wings.WingsDestination;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * The Wings database helper that stores {@link ShareRequest} records and manages the state of those records.
 *
 * @author Benedict Lau
 */
@Singleton
public class WingsDbHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "wings.db";

    private static final int DB_VERSION = 1;

    private static final long ID_ERROR = -1L;

    //
    // SQL where clauses.
    //

    /**
     * SQL where clause by id.
     */
    private static final String WHERE_CLAUSE_BY_ID = ShareRequestTable.COLUMN_ID + "=?";

    /**
     * SQL where clause by destination.
     */
    private static final String WHERE_CLAUSE_BY_DESTINATION = ShareRequestTable.COLUMN_DESTINATION + "=?";

    /**
     * SQL where clause by state.
     */
    private static final String WHERE_CLAUSE_BY_STATE = ShareRequestTable.COLUMN_STATE + "=?";

    /**
     * SQL where clause by destination and state.
     */
    private static final String WHERE_CLAUSE_BY_DESTINATION_AND_STATE = ShareRequestTable.COLUMN_DESTINATION
            + "=? AND " + ShareRequestTable.COLUMN_STATE + "=?";

    /**
     * SQL where clause that describes the purge policy. A query with this where clause will return all records
     * satisfying one or more of the following conditions:
     * <p/>
     * <pre>
     * 1. Records created before a certain time
     * 2. Records in a certain state
     * 3. Records that failed more than a certain number of times
     * </pre>
     */
    private static final String WHERE_CLAUSE_PURGE_POLICY = ShareRequestTable.COLUMN_TIME_CREATED + "<? OR "
            + ShareRequestTable.COLUMN_STATE + "=? OR " + ShareRequestTable.COLUMN_FAILS + ">?";

    /**
     * SQL sort order by creation time of creation, from earliest to the most recent.
     */
    private static final String SORT_ORDER_TIME_CREATED = ShareRequestTable.COLUMN_TIME_CREATED + " ASC";

    //
    // Purge policy params.
    //

    /**
     * Records expire after 2 days. In milliseconds.
     */
    private static long RECORD_EXPIRY_TIME = 172800000L;

    /**
     * The number of times a record may fail to process, beyond which it will be purged. Too small of a number is
     * dangerous as every new record creation will trigger a retry, and the number of fails can build up quickly when
     * the device has no connectivity.
     */
    private static int RECORD_MAX_FAILS = 500;

    /**
     * The logger for debug messages.
     */
    @Inject
    static IWingsLogger sLogger;

    /**
     * The {@link Context}.
     */
    private Context mContext;

    /**
     * Static initializer.
     */
    static {
        // Inject static dependencies.
        WingsInjector.injectStatics();
    }

    /**
     * Constructor.
     *
     * @param context the {@link Context}.
     */
    @Inject
    WingsDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(ShareRequestTable.CREATE_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Do nothing.
    }

    //
    // Public methods.
    //

    /**
     * Creates a new {@link ShareRequest}.
     *
     * @param filePath    the local path to the file to share.
     * @param destination the destination of the share.
     * @return true if successful; false otherwise.
     */
    public synchronized boolean createShareRequest(String filePath, WingsDestination destination) {
        boolean isSuccessful = false;

        SQLiteDatabase db = null;
        try {
            db = getWritableDatabase();

            // Create new record.
            ContentValues values = new ContentValues();
            values.put(ShareRequestTable.COLUMN_FILE_PATH, filePath);
            values.put(ShareRequestTable.COLUMN_DESTINATION, destination.getHash());
            values.put(ShareRequestTable.COLUMN_TIME_CREATED, System.currentTimeMillis());
            values.put(ShareRequestTable.COLUMN_STATE, ShareRequest.STATE_PENDING);
            values.put(ShareRequestTable.COLUMN_FAILS, 0);

            isSuccessful = db.insert(ShareRequestTable.NAME, null, values) != ID_ERROR;

            sLogger.log(WingsDbHelper.class, "createShareRequest", "isSuccessful=" + isSuccessful + " filePath="
                    + filePath + " destination=" + destination.getHash());
        } catch (SQLException e) {
            // Do nothing.
        } finally {
            db.close();
        }

        // Reset retry policy because a new record is created.
        RetryPolicy.reset(mContext);

        return isSuccessful;
    }

    /**
     * Checks out a list of {@link ShareRequest} that need to be processed, filtered by destination. The list is sorted
     * by time of creation, from the earliest to most recent. This method internally changes the checked out records to
     * a processing state, so a call to {@link #markSuccessful(int)} or {@link #markFailed(int)} is expected to be
     * called on each of those records.
     *
     * @param destination the destination of the {@link ShareRequest} to checkout.
     * @return the list of {@link ShareRequest}; may be empty.
     */
    public synchronized List<ShareRequest> checkoutShareRequests(WingsDestination destination) {
        List<ShareRequest> shareRequests = new ArrayList<ShareRequest>();

        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = getWritableDatabase();

            // Get all records for the requested destination in the pending state.
            cursor = db.query(ShareRequestTable.NAME, new String[]{ShareRequestTable.COLUMN_ID,
                            ShareRequestTable.COLUMN_FILE_PATH, ShareRequestTable.COLUMN_DESTINATION},
                    WHERE_CLAUSE_BY_DESTINATION_AND_STATE, new String[]{String.valueOf(destination.getHash()),
                            String.valueOf(ShareRequest.STATE_PENDING)}, null, null, SORT_ORDER_TIME_CREATED
            );

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndex(ShareRequestTable.COLUMN_ID));
                    String filePath = cursor.getString(cursor.getColumnIndex(ShareRequestTable.COLUMN_FILE_PATH));
                    int destinationHash = cursor.getInt(cursor.getColumnIndex(ShareRequestTable.COLUMN_DESTINATION));

                    // Update state back to processing.
                    ContentValues values = new ContentValues();
                    values.put(ShareRequestTable.COLUMN_STATE, ShareRequest.STATE_PROCESSING);

                    if (db.update(ShareRequestTable.NAME, values, WHERE_CLAUSE_BY_ID,
                            new String[]{String.valueOf(id)}) > 0) {
                        // Add record to list.
                        WingsDestination resultDestination = WingsDestination.from(destinationHash);
                        shareRequests.add(new ShareRequest(id, filePath, resultDestination));

                        sLogger.log(WingsDbHelper.class, "checkoutShareRequests", "id=" + id + " filePath="
                                + filePath + " destination=" + resultDestination.getHash());
                    }
                } while (cursor.moveToNext());
            }
        } catch (SQLException e) {
            // Do nothing.
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return shareRequests;
    }

    /**
     * Deletes all {@link ShareRequest} based on destination.
     *
     * @param destination the destination of the list of {@link ShareRequest} to delete.
     */
    public synchronized void deleteShareRequests(WingsDestination destination) {
        SQLiteDatabase db = null;
        try {
            db = getWritableDatabase();

            int recordsDeleted = db.delete(ShareRequestTable.NAME, WHERE_CLAUSE_BY_DESTINATION,
                    new String[]{String.valueOf(destination.getHash())});

            sLogger.log(WingsDbHelper.class, "deleteShareRequests", "destination=" + destination.getHash()
                    + " rowsDeleted=" + recordsDeleted);
        } catch (SQLException e) {
            // Do nothing.
        } finally {
            db.close();
        }
    }

    /**
     * Marks a {@link ShareRequest} as successfully processed.
     *
     * @param id the id of the {@link ShareRequest}.
     * @return true if successful; false otherwise.
     */
    public synchronized boolean markSuccessful(int id) {
        boolean isSuccessful = false;

        SQLiteDatabase db = null;
        try {
            db = getWritableDatabase();

            // Set state to processed.
            ContentValues values = new ContentValues();
            values.put(ShareRequestTable.COLUMN_STATE, ShareRequest.STATE_PROCESSED);

            isSuccessful = db.update(ShareRequestTable.NAME, values, WHERE_CLAUSE_BY_ID,
                    new String[]{String.valueOf(id)}) > 0;

            sLogger.log(WingsDbHelper.class, "markSuccessful", "isSuccessful=" + isSuccessful + " id=" + id);
        } catch (SQLException e) {
            // Do nothing.
        } finally {
            db.close();
        }
        return isSuccessful;
    }

    /**
     * Marks a {@link ShareRequest} as failed to process.
     *
     * @param id the id of the {@link ShareRequest}.
     * @return true if successful; false otherwise.
     */
    public synchronized boolean markFailed(int id) {
        boolean isSuccessful = false;

        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = getWritableDatabase();

            // Get number of times this record has already failed to process.
            cursor = db.query(ShareRequestTable.NAME, new String[]{ShareRequestTable.COLUMN_FAILS},
                    WHERE_CLAUSE_BY_ID, new String[]{String.valueOf(id)}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int fails = cursor.getInt(cursor.getColumnIndex(ShareRequestTable.COLUMN_FAILS));

                // Reset state back to pending and increment fails.
                ContentValues values = new ContentValues();
                values.put(ShareRequestTable.COLUMN_STATE, ShareRequest.STATE_PENDING);
                values.put(ShareRequestTable.COLUMN_FAILS, fails + 1);

                isSuccessful = db.update(ShareRequestTable.NAME, values, WHERE_CLAUSE_BY_ID,
                        new String[]{String.valueOf(id)}) > 0;

                sLogger.log(WingsDbHelper.class, "markFailed", "isSuccessful=" + isSuccessful + " id=" + id);
            }
        } catch (SQLException e) {
            // Do nothing.
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return isSuccessful;
    }

    /**
     * Purges the database based on the purge policy.
     *
     * @return the number of records remaining after the purge; or -1 if an error occurred.
     */
    public synchronized int purge() {
        int recordsRemaining = -1;
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = getWritableDatabase();

            // Purge records.
            long earliestValidTime = System.currentTimeMillis() - RECORD_EXPIRY_TIME;
            int recordsDeleted = db.delete(ShareRequestTable.NAME, WHERE_CLAUSE_PURGE_POLICY,
                    new String[]{String.valueOf(earliestValidTime), String.valueOf(ShareRequest.STATE_PROCESSED),
                            String.valueOf(RECORD_MAX_FAILS)}
            );

            // Check number of records remaining in the table.
            cursor = db.query(ShareRequestTable.NAME, new String[]{ShareRequestTable.COLUMN_ID}, null, null, null,
                    null, null);
            if (cursor != null) {
                recordsRemaining = cursor.getCount();
            }

            sLogger.log(WingsDbHelper.class, "purge", "recordsDeleted=" + recordsDeleted + " recordsRemaining="
                    + recordsRemaining);
        } catch (SQLException e) {
            // Do nothing.
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return recordsRemaining;
    }

    /**
     * Reset all records that somehow got stuck in a processing state.
     */
    public synchronized void resetProcessingShareRequests() {
        SQLiteDatabase db = null;
        try {
            db = getWritableDatabase();

            // Update state back to pending.
            ContentValues values = new ContentValues();
            values.put(ShareRequestTable.COLUMN_STATE, ShareRequest.STATE_PENDING);

            int recordsUpdated = db.update(ShareRequestTable.NAME, values, WHERE_CLAUSE_BY_STATE,
                    new String[]{String.valueOf(ShareRequest.STATE_PROCESSING)});

            sLogger.log(WingsDbHelper.class, "resetProcessingShareRequests", "recordsUpdated=" + recordsUpdated);
        } catch (SQLException e) {
            // Do nothing.
        } finally {
            db.close();
        }
    }

    //
    // Private classes.
    //

    /**
     * Table with each record representing a share request that needs to be processed.
     */
    private static class ShareRequestTable {

        /**
         * Table name.
         */
        private static final String NAME = "shares";

        /**
         * SQL statement to create table.
         */
        private static final String CREATE_SQL = String
                .format("CREATE TABLE %s (%s INTEGER PRIMARY KEY AUTOINCREMENT, %s TEXT NOT NULL, %s INTEGER NOT NULL, %s INTEGER NOT NULL, %s INTEGER NOT NULL, %s INTEGER NOT NULL)",
                        ShareRequestTable.NAME, ShareRequestTable.COLUMN_ID, ShareRequestTable.COLUMN_FILE_PATH,
                        ShareRequestTable.COLUMN_DESTINATION, ShareRequestTable.COLUMN_TIME_CREATED,
                        ShareRequestTable.COLUMN_STATE, ShareRequestTable.COLUMN_FAILS);

        //
        // Columns names.
        //

        /**
         * The record id.
         */
        private static final String COLUMN_ID = "_id";

        /**
         * The local path to the file to share.
         */
        private static final String COLUMN_FILE_PATH = "file_path";

        /**
         * The destination of the share.
         */
        private static final String COLUMN_DESTINATION = "destination";

        /**
         * The time the record is created. Internally managed.
         */
        private static final String COLUMN_TIME_CREATED = "time_created";

        /**
         * The current state of the share. Internally managed.
         */
        private static final String COLUMN_STATE = "state";

        /**
         * The number of times sharing failed. Internally managed.
         */
        private static final String COLUMN_FAILS = "fails";
    }
}
