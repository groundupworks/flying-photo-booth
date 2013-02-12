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

/**
 * A helper class for using functionalities from the Facebook SDK.
 * 
 * @author Benedict Lau
 */
public class FacebookHelper {
    // Facebook.
    // Session session = Session.getActiveSession();

    // List<String> permissions = new LinkedList<String>();
    // permissions.add("publish_actions");
    // Session.NewPermissionsRequest permissionsRequest = new Session.NewPermissionsRequest(getActivity(),
    // permissions);
    // permissionsRequest.setCallback(new StatusCallback() {
    //
    // @Override
    // public void call(Session session, SessionState state, Exception exception) {
    // Log.d("BEN", "StatusCallback2 session=" + session + " state=" + state);
    // }
    // });
    // Session.getActiveSession().requestNewPublishPermissions(permissionsRequest);

    // if (session != null && session.getPermissions().contains("publish_actions")) {
    // try {
    // Request request = Request.newUploadPhotoRequest(Session.getActiveSession(), new File("file://"
    // + (String) msg.obj), new Request.Callback() {
    //
    // @Override
    // public void onCompleted(Response response) {
    // Log.d("BEN", "Published");
    // // showPublishResult(getString(R.string.photo_post), response.getGraphObject(),
    // // response.getError());
    // }
    // });
    // request.executeAsync();
    // } catch (FileNotFoundException e) {
    // Log.d("BEN", "FileNotFoundException");
    // }
    //
    // } else {
    // // pendingAction = PendingAction.POST_PHOTO;
    // if (session == null) {
    // Log.d("BEN", "No active session");
    // } else {
    // Log.d("BEN", "No publish permission");
    // }
    // }
}
