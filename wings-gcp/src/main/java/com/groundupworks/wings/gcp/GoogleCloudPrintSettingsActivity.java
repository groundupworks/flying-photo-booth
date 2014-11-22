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
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.dpsmarques.android.account.activity.AccountSelectionActivityHelper;
import com.dpsmarques.android.auth.GoogleOauthTokenObservable;
import com.dpsmarques.android.auth.activity.OperatorGoogleAuthenticationActivityController;
import com.github.dpsm.android.print.GoogleCloudPrint;
import com.github.dpsm.android.print.jackson.JacksonPrinterSearchResultOperator;
import com.github.dpsm.android.print.model.Printer;
import com.github.dpsm.android.print.model.PrinterSearchResult;

import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * {@link android.app.Activity} to select a Google Cloud printer.
 *
 * @author David Marques
 */
public class GoogleCloudPrintSettingsActivity extends Activity implements
        AccountSelectionActivityHelper.AccountSelectionListener,
        OperatorGoogleAuthenticationActivityController.GoogleAuthenticationListener {

    private static final String GOOGLE_PRINT_SCOPE = "oauth2:https://www.googleapis.com/auth/cloudprint";

    private static final String[] ACCOUNT_TYPE = new String[]{"com.google"};

    private static final String TICKET = "{\n" +
            "  \"version\": \"1.0\",\n" +
            "  \"print\": {\n" +
            "    \"vendor_ticket_item\": [],\n" +
            "    \"color\": {\n" +
            "      \"type\": \"STANDARD_COLOR\"\n" +
            "    },\n" +
            "    \"media_size\": {\n" +
            "      \"width_microns\": 1,\n" +
            "      \"height_microns\": 1,\n" +
            "      \"is_continuous_feed\": false,\n" +
            "      \"vendor_id\" : \"EnvMonarch\"\n" +
            "    }\n" +
            "  }\n" +
            "}";

    static final String EXTRA_ACCOUNT = "account";
    static final String EXTRA_PRINTER = "printer";
    static final String EXTRA_TICKET = "ticket";
    static final String EXTRA_TOKEN = "token";

    private static final int REQUEST_CODE_BASE = 1000;

    private final Action1<PrinterSearchResult> mUpdatePrinterListAction = new Action1<PrinterSearchResult>() {
        @Override
        public void call(final PrinterSearchResult response) {
            final List<Printer> printers = response.getPrinters();
            if (printers != null && printers.size() > 0) {
                mPrinterSpinner.setAdapter(new ArrayAdapter<Printer>(
                        GoogleCloudPrintSettingsActivity.this, R.layout.gcp_settings_spinner_item,
                        R.id.activity_main_spinner_item_text, printers
                ));
            } else {
                Toast.makeText(getApplicationContext(), R.string.gcp__settings__error_no_printer, Toast.LENGTH_SHORT).show();
            }
        }
    };

    private final Action1<Throwable> mShowPrinterNotFoundAction = new Action1<Throwable>() {
        @Override
        public void call(final Throwable throwable) {
            Toast.makeText(getApplicationContext(), R.string.gcp__settings__error_no_printer, Toast.LENGTH_SHORT).show();
        }
    };

    private GoogleCloudPrint mGoogleCloudPrint;
    private AccountSelectionActivityHelper mAccountSelectionHelper;
    private OperatorGoogleAuthenticationActivityController mAuthenticationHelper;

    private Observable<String> mOauthObservable;
    private String mAccountSelected;
    private String mAuthenticationToken;

    private Button mSelectPrinterButton;
    private Spinner mPrinterSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gcp_activity_settings);

        mGoogleCloudPrint = new GoogleCloudPrint();
        mAccountSelectionHelper = new AccountSelectionActivityHelper(this, REQUEST_CODE_BASE);
        mAuthenticationHelper = new OperatorGoogleAuthenticationActivityController(this, REQUEST_CODE_BASE + 100);

        mSelectPrinterButton = (Button) findViewById(R.id.gcp_activity_settings_button_link);
        mPrinterSpinner = (Spinner) findViewById(R.id.gcp_activity_settings_spinner_printers);

        mSelectPrinterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                final Printer selectedPrinter = (Printer) mPrinterSpinner.getSelectedItem();
                if (selectedPrinter != null) {
                    final String id = selectedPrinter.getId();
                    final String account = mAccountSelected;
                    final String token = mAuthenticationToken;
                    if (!TextUtils.isEmpty(id) && !TextUtils.isEmpty(account) && !TextUtils.isEmpty(token)) {
                        final Intent intent = new Intent();
                        intent.putExtra(EXTRA_PRINTER, id);
                        intent.putExtra(EXTRA_ACCOUNT, account);
                        intent.putExtra(EXTRA_TICKET, TICKET);
                        intent.putExtra(EXTRA_TOKEN, token);

                        setResult(Activity.RESULT_OK, intent);
                        finish();
                    }
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mOauthObservable == null) {
            mAccountSelectionHelper.selectUserAccount(ACCOUNT_TYPE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mAccountSelectionHelper.handleActivityResult(requestCode, resultCode, data)) {
            return; // Handled by helper.
        }
        if (mAuthenticationHelper.handleActivityResult(requestCode, resultCode, data)) {
            return; // Handled by helper.
        }
    }

    @Override
    public void onAccountSelected(final String accountName) {
        mAccountSelected = accountName;
        mOauthObservable = GoogleOauthTokenObservable
                .create(this, accountName, GOOGLE_PRINT_SCOPE)
                .authenticateUsing(this, REQUEST_CODE_BASE)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
        findPrinters();
    }

    @Override
    public void onAccountSelectionCanceled() {
        final Intent intent = new Intent();
        setResult(Activity.RESULT_CANCELED, intent);
        finish();
    }

    @Override
    public void onAuthenticationError(final Throwable throwable) {
        Toast.makeText(getApplicationContext(), R.string.gcp__settings__error_authenticate, Toast.LENGTH_SHORT).show();
        mOauthObservable = null;
    }

    @Override
    public void onAuthenticationSucceeded(final String token) {
        mAuthenticationToken = token;
    }

    @Override
    public void onRetryAuthentication() {
        Toast.makeText(getApplicationContext(), R.string.gcp__settings__error_authenticate, Toast.LENGTH_SHORT).show();
    }

    private void findPrinters() {
        mOauthObservable.subscribe(new Action1<String>() {
            @Override
            public void call(final String token) {
                mGoogleCloudPrint.getPrinters(token)
                        .lift(new JacksonPrinterSearchResultOperator())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(mUpdatePrinterListAction, mShowPrinterNotFoundAction);
            }
        });
    }
}
