/*
 * Copyright (C) 2014 David Marques
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
package com.groundupworks.wings.gcp;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.widget.Toast;

import com.github.dpsm.android.print.GoogleCloudPrint;
import com.groundupworks.wings.IWingsNotification;
import com.groundupworks.wings.WingsDestination;
import com.groundupworks.wings.WingsEndpoint;
import com.groundupworks.wings.core.ShareRequest;

import java.io.File;
import java.net.HttpURLConnection;
import java.util.HashSet;
import java.util.Set;

import retrofit.client.Response;
import retrofit.mime.TypedFile;
import rx.Observable;
import rx.functions.Func1;
import rx.functions.Func2;

import static com.groundupworks.wings.gcp.GoogleCloudPrintSettingsActivity.EXTRA_ACCOUNT;
import static com.groundupworks.wings.gcp.GoogleCloudPrintSettingsActivity.EXTRA_PRINTER;
import static com.groundupworks.wings.gcp.GoogleCloudPrintSettingsActivity.EXTRA_TICKET;
import static com.groundupworks.wings.gcp.GoogleCloudPrintSettingsActivity.EXTRA_TOKEN;

/**
 * The Wings endpoint for Google Cloud Print.
 *
 * @author David Marques
 */
public class GoogleCloudPrintEndpoint extends WingsEndpoint {

    /**
     * Google Cloud Print endpoint id.
     */
    private static final int ENDPOINT_ID = 2;

    private static final int REQUEST_CODE = ENDPOINT_ID;

    private static final String MIME_TYPE = "image/jpeg";

    private final GoogleCloudPrint mGoogleCloudPrint = new GoogleCloudPrint();

    @Override
    public int getEndpointId() {
        return ENDPOINT_ID;
    }

    @Override
    public void startLinkRequest(final Activity activity, final Fragment fragment) {
        if (fragment != null) {
            fragment.startActivityForResult(new Intent(activity, GoogleCloudPrintSettingsActivity.class), REQUEST_CODE);
        } else {
            activity.startActivityForResult(new Intent(activity, GoogleCloudPrintSettingsActivity.class), REQUEST_CODE);
        }
    }

    @Override
    public void unlink() {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(mContext).edit();
        editor.putBoolean(mContext.getString(R.string.gcp__link_key), false);
        editor.remove(mContext.getString(R.string.gcp__account_name_key));
        editor.remove(mContext.getString(R.string.gcp__printer_identifier_key));
        editor.remove(mContext.getString(R.string.gcp__ticket));
        editor.remove(mContext.getString(R.string.gcp__token));
        editor.apply();

        // Remove existing share requests in a background thread.
        mHandler.post(new Runnable() {

            @Override
            public void run() {
                mDatabase.deleteShareRequests(new WingsDestination(DestinationId.PRINT_QUEUE, ENDPOINT_ID));
            }
        });
    }

    @Override
    public boolean isLinked() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        return preferences.getBoolean(mContext.getString(R.string.gcp__link_key), false);
    }

    @Override
    public void onResumeImpl() {
        // Do nothing.
    }

    @Override
    public void onActivityResultImpl(final Activity activity, final Fragment fragment,
                                     final int requestCode, final int resultCode, final Intent data) {
        if (requestCode == REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                String accountName = data.getStringExtra(EXTRA_ACCOUNT);
                String printerIdentifier = data.getStringExtra(EXTRA_PRINTER);
                String ticket = data.getStringExtra(EXTRA_TICKET);
                String token = data.getStringExtra(EXTRA_TOKEN);

                if (!TextUtils.isEmpty(accountName) && !TextUtils.isEmpty(printerIdentifier) && !TextUtils.isEmpty(ticket) && !TextUtils.isEmpty(token)) {
                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(mContext).edit();
                    editor.putBoolean(mContext.getString(R.string.gcp__link_key), true);
                    editor.putString(mContext.getString(R.string.gcp__account_name_key), accountName);
                    editor.putString(mContext.getString(R.string.gcp__printer_identifier_key), printerIdentifier);
                    editor.putString(mContext.getString(R.string.gcp__ticket), ticket);
                    editor.putString(mContext.getString(R.string.gcp__token), token);
                    editor.apply();
                } else {
                    Toast.makeText(mContext, mContext.getString(R.string.gcp__error_link), Toast.LENGTH_SHORT).show();
                    unlink();
                }
            } else {
                Toast.makeText(mContext, mContext.getString(R.string.gcp__error_link), Toast.LENGTH_SHORT).show();
                unlink();
            }
        }
    }

    @Override
    public String getLinkedAccountName() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        return preferences.getString(mContext.getString(R.string.gcp__account_name_key), null);
    }

    @Override
    public String getDestinationDescription(final int destinationId) {
        String destinationDescription = null;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        String accountName = preferences.getString(mContext.getString(R.string.gcp__account_name_key), null);
        String printerIdentifier = preferences.getString(mContext.getString(R.string.gcp__printer_identifier_key), null);
        if (!TextUtils.isEmpty(accountName) && !TextUtils.isEmpty(printerIdentifier)) {
            destinationDescription = mContext.getString(R.string.gcp__destination_description, accountName, printerIdentifier);
        }
        return destinationDescription;
    }

    @Override
    public Set<IWingsNotification> processShareRequests() {
        final Set<IWingsNotification> notifications = new HashSet<IWingsNotification>();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);

        final boolean isLinked = preferences.getBoolean(mContext.getString(R.string.gcp__link_key), false);
        final String accountName = preferences.getString(mContext.getString(R.string.gcp__account_name_key), null);
        final String printerIdentifier = preferences.getString(mContext.getString(R.string.gcp__printer_identifier_key), null);
        final String ticket = preferences.getString(mContext.getString(R.string.gcp__ticket), null);
        final String token = preferences.getString(mContext.getString(R.string.gcp__token), null);

        if (isLinked && !TextUtils.isEmpty(accountName) && !TextUtils.isEmpty(printerIdentifier) &&
                !TextUtils.isEmpty(ticket) && !TextUtils.isEmpty(token)) {
            final WingsDestination destination = new WingsDestination(DestinationId.PRINT_QUEUE, ENDPOINT_ID);
            final Observable<ShareRequest> requestObservable = Observable
                    .from(mDatabase.checkoutShareRequests(destination))
                    .cache();
            final Observable<Response> responseObservable = requestObservable
                    .map(new Func1<ShareRequest, File>() {
                        @Override
                        public File call(final ShareRequest shareRequest) {
                            return new File(shareRequest.getFilePath());
                        }
                    })
                    .filter(new Func1<File, Boolean>() {
                        @Override
                        public Boolean call(final File file) {
                            return file.exists();
                        }
                    })
                    .flatMap(new Func1<File, Observable<Response>>() {
                        @Override
                        public Observable<Response> call(final File file) {
                            return mGoogleCloudPrint.submitPrintJob(token, printerIdentifier,
                                    file.getName(), ticket, new TypedFile(MIME_TYPE, file));
                        }
                    });
            final Observable<IWingsNotification> notificationObservable = Observable
                    .zip(requestObservable, responseObservable, new Func2<ShareRequest, Response, Boolean>() {
                        @Override
                        public Boolean call(final ShareRequest shareRequest, final Response response) {
                            boolean isSuccessful = false;
                            if (response.getStatus() == HttpURLConnection.HTTP_OK) {
                                mDatabase.markSuccessful(shareRequest.getId());
                                isSuccessful = true;
                            } else {
                                mDatabase.markFailed(shareRequest.getId());
                            }
                            return isSuccessful;
                        }
                    })
                    .filter(new Func1<Boolean, Boolean>() {
                        @Override
                        public Boolean call(Boolean aBoolean) {
                            return aBoolean;
                        }
                    })
                    .count()
                    .filter(new Func1<Integer, Boolean>() {
                        @Override
                        public Boolean call(Integer count) {
                            return count > 0;
                        }
                    })
                    .map(new Func1<Integer, IWingsNotification>() {
                        @Override
                        public IWingsNotification call(final Integer count) {
                            return new IWingsNotification() {

                                @Override
                                public int getId() {
                                    return destination.getHash();
                                }

                                @Override
                                public String getTitle() {
                                    return mContext.getString(R.string.gcp__notification_shared_title);
                                }

                                @Override
                                public String getMessage() {
                                    String msg;
                                    if (count == 1) {
                                        msg = mContext.getString(R.string.gcp__notification_shared_msg_single, printerIdentifier);
                                    } else {
                                        msg = mContext.getString(R.string.gcp__notification_shared_msg_multi, count, printerIdentifier);
                                    }
                                    return msg;
                                }

                                @Override
                                public String getTicker() {
                                    return mContext.getString(R.string.gcp__notification_shared_ticker);
                                }

                                @Override
                                public Intent getIntent() {
                                    return new Intent();
                                }
                            };
                        }
                    });

            try {
                notifications.add(notificationObservable.toBlocking().last());
            } catch (Throwable t) {
                sLogger.log(GoogleCloudPrintEndpoint.class, "processShareRequests", "throwable=" + t);
            }
        }
        return notifications;
    }

    /**
     * The list of destination ids.
     */
    public interface DestinationId {

        /**
         * The GCP print queue.
         */
        public static final int PRINT_QUEUE = 0;
    }
}
