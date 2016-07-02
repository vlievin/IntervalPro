package comvlievin.github.fitme;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

/**
 * display an overview of the selected activity
 */
public class ActivityIntervalOverall extends AppCompatActivity implements OnMapReadyCallback {

    private DataAppSaving mAppData;
    private DataIntervalSession mSessionData;
    private GoogleMap mMap;
    private int mColorMax;
    private int mColorMin;
    private ShareDialog shareDialog;
    private CallbackManager callbackManager;
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        shareDialog = new ShareDialog(this);
        setContentView(R.layout.activity_interval_overall);
        mColorMax = getResources().getColor(R.color.colorAccent);
        mColorMin = getResources().getColor(R.color.colorPrimary);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
        addListenerOnButton();
    }

    public void addListenerOnButton() {
        // go back to the home screen
        Button btn_quit = (Button) findViewById(R.id.btn_quit);
        if (btn_quit != null) {
            btn_quit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    Intent intent = new Intent(ActivityIntervalOverall.this, ActivityHomeScreen.class);
                    startActivity(intent);
                }
            });
        }
        // share an activty
        Button btn_share = (Button) findViewById(R.id.btn_share);
        if (btn_share != null) {
            btn_share.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    // use the Facebook SDK to share a screenshot of the activity
                    if (ShareDialog.canShow(ShareLinkContent.class)) {
                        Bitmap image = takeScreenshot();
                        SharePhoto photo = new SharePhoto.Builder()
                                .setBitmap(image)
                                .build();
                        SharePhotoContent content = new SharePhotoContent.Builder()
                                .addPhoto(photo)
                                .build();
                        shareDialog.show(content, ShareDialog.Mode.AUTOMATIC);
                    }
                }
            });
        }
        // delete an activity
        Button btn_delete = (Button) findViewById(R.id.btn_delete);
        if (btn_delete != null) {
            btn_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    SharedPreferences mPrefs = getSharedPreferences("fitMe", MODE_PRIVATE);
                    Gson gson = new Gson();
                    String jsonStr = mPrefs.getString("fitMeData", "");
                    if (jsonStr != "") {
                        mAppData = gson.fromJson(jsonStr, DataAppSaving.class);
                        Bundle b = getIntent().getExtras();
                        int position = b.getInt("position");
                        mSessionData = mAppData.getSession(position);

                        List<DataIntervalSession> sessions = new ArrayList<DataIntervalSession>();

                        for (int i = 0; i < mAppData.getSessions().size(); i++) {
                            if (i != position) {
                                sessions.add(mAppData.getSessions().get(i));
                            }
                        }
                        mAppData.setSessions(sessions);
                        SharedPreferences.Editor prefsEditor = mPrefs.edit();
                        Gson gson2 = new Gson();
                        String json = gson2.toJson(mAppData);
                        prefsEditor.putString("fitMeData", json);
                        prefsEditor.commit();
                    }
                    finish();
                }
            });
        }
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                setupSessionData(googleMap);
            }
        });


    }

    /**
     * draw the user's path on the map and add the session's data to the UI
     * @param googleMap googleMaps fragment
     */
    public void setupSessionData(GoogleMap googleMap) {
        SharedPreferences mPrefs = getSharedPreferences("fitMe", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = mPrefs.getString("fitMeData", "");
        if (json != "") {
            mAppData = gson.fromJson(json, DataAppSaving.class);
            Bundle b = getIntent().getExtras();
            int position = b.getInt("position");
            mSessionData = mAppData.getSession(position);
            String s = new Integer(mSessionData.getmPositions().size() ).toString();

            //running duration
            TextView text_run_duration = (TextView) findViewById((R.id.text_round_duration_running));
            text_run_duration.setText(mSessionData.getRoundLength_run() + "s");
            // resting duration
            TextView text_rest_duration = (TextView) findViewById((R.id.text_round_duration_resting));
            text_rest_duration.setText(mSessionData.getRoundLength_rest()+"s");
            // rounds achieved
            TextView text_rounds_achieved = (TextView) findViewById((R.id.text_completedRounds_value));
            text_rounds_achieved.setText(mSessionData.getNumberOfAchievedRounds() + " / " + mSessionData.getNumberOfRounds());
            // distance
            TextView text_total_distance = (TextView) findViewById((R.id.text_total_distance));
            text_total_distance.setText(  String.format("%.2f",0.001f * mSessionData.getDistance() )  + " km");
            // time
            TextView text_total_time = (TextView) findViewById((R.id.text_total_time));
            text_total_time.setText( ((int)mSessionData.getTotalTime())/60 + "min" +(int)mSessionData.getTotalTime()%60+"s"  );
            // average run
            TextView text_avg_run = (TextView) findViewById((R.id.text_average_speed_run));
            text_avg_run.setText( String.format("%.1f", mSessionData.getAverageSpeed_running())+ "km/h" );
            // average rest
            TextView text_avg_rest = (TextView) findViewById((R.id.text_average_speed_rest));
            text_avg_rest.setText( String.format("%.1f", mSessionData.getAverageSpeed_resting())+ "km/h" );
            List<LatLng> positions = mSessionData.getmPositions();
            if (positions.size() > 1) {
                PolylineOptions polyLine = new PolylineOptions().color(
                        mColorMax ).width((float) 15.0);
                polyLine.addAll(positions);
                mMap.addPolyline(polyLine);

                mMap.addMarker(new MarkerOptions().position(positions.get(0)).title("starting point"));
                mMap.addMarker(new MarkerOptions().position(positions.get(positions.size()-1)).title("Ending point"));

                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                for (LatLng pos : positions) {
                    builder.include(pos);
                }
                LatLngBounds bounds = builder.build();
                int padding = 150; // offset from edges of the map in pixels
                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                googleMap.animateCamera(cu);
            }
        }
        else
            Log.d("----->session overall", "no data");
    }

    @Override
    public void onStart() {
        super.onStart();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "ActivityIntervalOverall Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://comvlievin.github.fitme/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "ActivityIntervalOverall Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://comvlievin.github.fitme/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    // take a screenshot of the current activity
    public Bitmap takeScreenshot() {
        View rootView = findViewById(android.R.id.content).getRootView();
        rootView.setDrawingCacheEnabled(true);
        return rootView.getDrawingCache();
    }
}
