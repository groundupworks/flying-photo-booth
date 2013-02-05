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
package com.groundupworks.flyingphotobooth.facebook;

public class FacebookLoginActivity {
    // Facebook.
    // Session.openActiveSession(this, true, new StatusCallback() {
    //
    // @Override
    // public void call(Session session, SessionState state, Exception exception) {
    // Log.d("BEN", "StatusCallback1 session=" + session + " state=" + state);
    //
    // runOnUiThread(new Runnable() {
    //
    // @Override
    // public void run() {
    // List<String> permissions = new LinkedList<String>();
    // permissions.add("publish_actions");
    // Session.NewPermissionsRequest permissionsRequest = new Session.NewPermissionsRequest(
    // MyPreferenceActivity.this, permissions);
    // permissionsRequest.setCallback(new StatusCallback() {
    //
    // @Override
    // public void call(Session session, SessionState state, Exception exception) {
    // Log.d("BEN", "StatusCallback2 session=" + session + " state=" + state);
    // }
    // });
    // Session.getActiveSession().requestNewPublishPermissions(permissionsRequest);
    // }
    // });
    // }
    // });
}
