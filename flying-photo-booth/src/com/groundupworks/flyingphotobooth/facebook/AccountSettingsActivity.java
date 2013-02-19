package com.groundupworks.flyingphotobooth.facebook;

import android.app.Activity;
import android.content.Intent;

public class AccountSettingsActivity extends Activity {

    //
    // Default album associated with the app.
    //

    /**
     * The name of the default album to share to.
     */
    private static final String DEFAULT_ALBUM_NAME = "Flying PhotoBooth Photos";

    /**
     * The graph path of the default album to share to.
     */
    private static final String DEFAULT_ALBUM_GRAPH_PATH = "me/photos";

    private static final String UPLOAD_PHOTO_PRIVACY_SELF = "Only Me";

    private static final String UPLOAD_PHOTO_PRIVACY_FRIENDS = "Friends";

    private static final String UPLOAD_PHOTO_PRIVACY_EVERYONE = "Public";

    private static final String UPLOAD_PHOTO_PRIVACY_SELF_VALUE = "{'value':'SELF'}";

    private static final String UPLOAD_PHOTO_PRIVACY_FRIENDS_VALUE = "{'value':'ALL_FRIENDS'}";

    private static final String UPLOAD_PHOTO_PRIVACY_EVERYONE_VALUE = "{'value':'EVERYONE'}";

    @Override
    protected void onResume() {
        super.onResume();

        Intent returnIntent = new Intent();
        AccountSettings accountSettings = AccountSettings.newInstance("Benedict Lau", "{'value':'SELF'}",
                "Flying PhotoBooth Photos", "me/photos");
        returnIntent.putExtras(accountSettings.toBundle());
        setResult(RESULT_OK, returnIntent);
        finish();
    }

    // new GraphUserCallback() {
    // @Override
    // public void onCompleted(GraphUser user, Response response) {
    // // Check for error.
    // if (response != null && response.getError() == null && user != null) {
    // String accountName = user.getFirstName() + " " + user.getLastName();
    // }
    // }
    // };

    // new Callback() {
    // @Override
    // public void onCompleted(Response response) {
    // GraphObject graphObject = response.getGraphObject();
    // JSONObject jsonObject = graphObject.getInnerJSONObject();
    // try {
    // JSONArray jsonArray = jsonObject.getJSONArray("data");
    // for (int i = 0; i < jsonArray.length(); i++) {
    // try {
    // JSONObject album = jsonArray.getJSONObject(i);
    // String albumGraphPath = album.getString("id") + "/photos";
    // String albumName = album.getString("name");
    // String albumPrivacy = album.getString("privacy");
    // boolean albumCanUpload = album.getBoolean("can_upload");
    // Log.d("BEN", Thread.currentThread().getId() + " albumGraphPath=" + albumGraphPath
    // + " albumName=" + albumName + " albumPrivacy=" + albumPrivacy + " albumCanUpload="
    // + albumCanUpload);
    // } catch (JSONException e) {
    // // Do nothing.
    // }
    // }
    // } catch (JSONException e1) {
    // // Do nothing.
    // }
    // }
    // };
}
