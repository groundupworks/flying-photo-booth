package com.groundupworks.wings.gcp;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;

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

import static com.groundupworks.wings.gcp.GoogleCloudPrintPrinterSelectionActivity.EXTRA_ACCOUNT;
import static com.groundupworks.wings.gcp.GoogleCloudPrintPrinterSelectionActivity.EXTRA_PRINTER;
import static com.groundupworks.wings.gcp.GoogleCloudPrintPrinterSelectionActivity.EXTRA_TICKET;
import static com.groundupworks.wings.gcp.GoogleCloudPrintPrinterSelectionActivity.EXTRA_TOKEN;

/**
 * Created by david.marques on 2014-10-30.
 */
public class GoogleCouldPrintEndpoint extends WingsEndpoint {

    private static final WingsDestination DESTINATION_GCP = new WingsDestination(0x01, 0x04);

    private static final String MIME_TYPE = "image/jpeg";

    private final GoogleCloudPrint mGoogleCloudPrint;

    private String mPrinterIdentifier;

    private String mAccountName;

    private String mTicket;

    private String mToken;

    public GoogleCouldPrintEndpoint() {
        mGoogleCloudPrint = new GoogleCloudPrint();
    }

    @Override
    public void startLinkRequest(final Activity activity, final Fragment fragment) {
        activity.startActivityForResult(
            new Intent(activity, GoogleCloudPrintPrinterSelectionActivity.class), DESTINATION_GCP.getEndpointId());
    }

    @Override
    public void unlink() {
        mAccountName = mPrinterIdentifier = mTicket = mToken = null;
    }

    @Override
    public boolean isLinked() {
        return !TextUtils.isEmpty(mAccountName)
            && !TextUtils.isEmpty(mPrinterIdentifier)
            && !TextUtils.isEmpty(mTicket)
            && !TextUtils.isEmpty(mToken);
    }

    @Override
    public void onResumeImpl() {

    }

    @Override
    public void onActivityResultImpl(final Activity activity, final Fragment fragment,
        final int requestCode, final int resultCode, final Intent data) {
        if (requestCode == DESTINATION_GCP.getEndpointId()) {
            if (resultCode == Activity.RESULT_OK) {
                mPrinterIdentifier = data.getStringExtra(EXTRA_PRINTER);
                mAccountName = data.getStringExtra(EXTRA_ACCOUNT);
                mTicket = data.getStringExtra(EXTRA_TICKET);
                mToken = data.getStringExtra(EXTRA_TOKEN);
            }
        }
    }

    @Override
    public String getLinkedAccountName() {
        return mAccountName;
    }


    @Override
    public int getEndpointId() {
        return DESTINATION_GCP.getEndpointId();
    }

    @Override
    public String getDestinationDescription(final int destinationId) {
        return mContext.getString(R.string.gcp_destination_description);
    }

    @Override
    public Set<IWingsNotification> processShareRequests() {
        final Observable<ShareRequest> requestObservable = Observable
            .from(mDatabase.checkoutShareRequests(DESTINATION_GCP))
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
                    return mGoogleCloudPrint.submitPrintJob(mToken, mPrinterIdentifier,
                        file.getName(), mTicket, new TypedFile(MIME_TYPE, file));
                }
            });

        Observable<IWingsNotification> notificationObservable = Observable
            .zip(requestObservable, responseObservable, new Func2<ShareRequest, Response, Object>() {
            @Override
            public ShareRequest call(final ShareRequest shareRequest, final Response response) {
                if (response.getStatus() == HttpURLConnection.HTTP_OK) {
                    mDatabase.markSuccessful(shareRequest.getId());
                } else {
                    mDatabase.markFailed(shareRequest.getId());
                }
                return shareRequest;
            }
        })
        .count()
        .map(new Func1<Integer, IWingsNotification>() {
            @Override
            public IWingsNotification call(final Integer count) {
                return new IWingsNotification() {

                    @Override
                    public int getId() {
                        return 0;
                    }

                    @Override
                    public String getTitle() {
                        return mContext.getString(R.string.gcp_notification_title);
                    }

                    @Override
                    public String getMessage() {
                        return mContext.getString(R.string.gcp_notification_message, count);
                    }

                    @Override
                    public String getTicker() {
                        return mContext.getString(R.string.gcp_notification_ticket);
                    }

                    @Override
                    public Intent getIntent() {
                        return new Intent();
                    }
                };
            }
        });

        final Set<IWingsNotification> result = new HashSet<IWingsNotification>();
        try {
            result.add(notificationObservable.toBlocking().last());
        } catch (Throwable t) {
            Log.e(getClass().getSimpleName(), "Failed to enqueue print jobs!", t);
        }
        return result;
    }
}
