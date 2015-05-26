package gr.anomologita.anomologita.network;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import gr.anomologita.anomologita.Anomologita;
import gr.anomologita.anomologita.extras.Keys;
import gr.anomologita.anomologita.extras.Utils;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static gr.anomologita.anomologita.Anomologita.userID;

public class GCMRegister extends Activity {

    private Context context;
    private JSONParser jsonParser = new JSONParser();
    private GoogleCloudMessaging gcm;
    private String regId;

    public static final String REG_ID = "regId";
    private static final String APP_VERSION = "appVersion";

    static final String TAG = "Register Activity";

    public GCMRegister() {
        this.context = Anomologita.getsInstance();
    }

    public String registerGCM() {

        gcm = GoogleCloudMessaging.getInstance(context);
        regId = getRegistrationId(context);

        if (TextUtils.isEmpty(regId)) {
            new RegisterInBackground().execute(null, null, null);
            Log.d("RegisterActivity", "registerGCM - successfully registered with GCM server - regId: " + regId);
        } else {
            Toast.makeText(context, "RegId already available. RegId: " + regId, Toast.LENGTH_LONG).show();
        }
        Anomologita.StartMain();
        return regId;
    }

    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String registrationId = prefs.getString(REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        int registeredVersion = prefs.getInt(APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            Log.d("RegisterActivity", "I never expected this! Going down, going down!" + e);
            throw new RuntimeException(e);
        }
    }

    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(REG_ID, regId);
        editor.putInt(APP_VERSION, appVersion);
        editor.apply();
    }

    private class RegisterInBackground extends AsyncTask<Void, String, String> {

        @Override
        protected String doInBackground(Void... params) {
            String msg;
            try {
                if (gcm == null) {
                    gcm = GoogleCloudMessaging.getInstance(context);
                }
                regId = gcm.register(Utils.GOOGLE_PROJECT_ID);
                Log.d("RegisterActivity", "registerInBackground - regId: " + regId);
                msg = "Device registered, registration ID=" + regId;
                storeRegistrationId(context, regId);
            } catch (IOException ex) {
                msg = "Error :" + ex.getMessage();
                Log.d("RegisterActivity", "Error: " + msg);
            }
            int success;
            try {
                List<BasicNameValuePair> param = new ArrayList<>();
                param.add(new BasicNameValuePair("reg_id", regId));

                Log.d("request!", "starting");
                JSONObject json = jsonParser.makeHttpRequest(Keys.EndpointGroups.URL_SET_USER, "POST", param);
                Log.d("Login attempt", json.toString());
                success = json.getInt(Keys.EndpointGroups.TAG_SUCCESS);
                Log.d("success", String.valueOf(success));

                if (success == 1) {
                    userID = String.valueOf(json.getString("userID"));
                    Log.d("Login Successful!", json.toString());
                    return json.getString(Keys.EndpointGroups.TAG_MESSAGE);
                } else {
                    Log.d("Login Failure!", json.getString(Keys.EndpointGroups.TAG_MESSAGE));
                    return json.getString(Keys.EndpointGroups.TAG_MESSAGE);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.d("RegisterActivity", "AsyncTask completed: " + msg);
            return msg;
        }

        @Override
        protected void onPostExecute(String msg) {
            Toast.makeText(context, "Registered with GCM Server." + msg, Toast.LENGTH_LONG).show();
            Anomologita.setUserID(userID);
        }
    }
}
