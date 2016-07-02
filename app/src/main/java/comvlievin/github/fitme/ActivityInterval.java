package comvlievin.github.fitme;


import android.Manifest;
import android.animation.ArgbEvaluator;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import com.google.gson.Gson;

import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Button;

import android.content.BroadcastReceiver;

import java.util.List;


/**
 *
 */
public class ActivityInterval extends FragmentActivity implements OnMapReadyCallback,GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener{
    // Usain bolt world-reccord speed (not use in this iteration)
    private final float mUsainBoltSpeed = 44.64f;
    // Color related the maximal speed
    private int mColorMax;
    // Color related to the minimal speed
    private int mColorMin;
    // Google map fragment
    private GoogleMap mMap;
    // simple text view used to display the current speed
    private TextView mSpeed;
    // constant for the location access
    private static final int PERMISSION_ACCESS_FINE_LOCATION = 1;
    // google API
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    // constant string for the logger
    private final String TAG = "mapsActivity";
    // session data
    private DataIntervalSession mSessionData;
    // timer fragment
    private FragmentTimer mTimer;
    // app data
    private DataAppSaving mAppData;
    // play state
    private boolean mPlay = false;
    // number of round (session)
    private int mNumberOfRound;
    // running duration for each round
    private int mRunDuration;
    // resting duration for each round
    private int mRestDuration;
    // data loggin stat
    boolean mLog_state;
    // broadcaster receiver used to catch the timer ending signal
    private BroadcastReceiver mTimerEndReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Button btn_finish = (Button) findViewById(R.id.btn_finish);
            btn_finish.setText("End activity");
            StopRecord();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interval);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        LocalBroadcastManager.getInstance(this).registerReceiver(
                mTimerEndReceiver, new IntentFilter("timesup"));

        Intent intent = getIntent();
        mNumberOfRound = intent.getIntExtra("n_rounds", 10);
        mRunDuration = intent.getIntExtra("t_run", 30);
        mRestDuration = intent.getIntExtra("t_rest", 30);
        mColorMax = getResources().getColor(R.color.colorAccent);
        mColorMin = getResources().getColor(R.color.colorPrimary);
        mSpeed = (TextView) findViewById((R.id.text_speed));
        mTimer = (FragmentTimer)
                getSupportFragmentManager().findFragmentById(R.id.fragmentTimer);
        mTimer.setup(mNumberOfRound, mRunDuration, mRestDuration);
        // savings
        // getting shared preferences (app data)
        SharedPreferences mPrefs = getSharedPreferences("fitMe", MODE_PRIVATE);
        mLog_state = mPrefs.getBoolean("log", true);
        Gson gson = new Gson();
        String json = mPrefs.getString("fitMeData", "");
        Log.d("read json:", json);
        if (json != "")
            mAppData = gson.fromJson(json, DataAppSaving.class);
        else
            mAppData = new DataAppSaving();
        // add listeners on buttons
        addListenerOnButton();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        // request permissions at runtime for the location
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_ACCESS_FINE_LOCATION);
        }
        // initialise google API
        mGoogleApiClient = new GoogleApiClient.Builder(this, this, this).addApi(LocationServices.API).addApi(AppIndex.API).build();
        // initialise location request object
        mLocationRequest = new LocationRequest()
                .setInterval(3 * 1000) // every 3 seconds
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    }

    /**
     * add listeners on buttons
     */
    public void addListenerOnButton() {
        final Button btn_finish = (Button) findViewById(R.id.btn_finish);
        btn_finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (mPlay)
                    StopRecord();
                if (mLog_state) {
                    Intent intent = new Intent(ActivityInterval.this, ActivityIntervalOverall.class);
                    Bundle b = new Bundle();
                    b.putInt("position", mAppData.getSessions().size() - 1);
                    intent.putExtras(b);
                    startActivity(intent);
                    finish();
                }
                else
                {
                    Intent intent = new Intent(ActivityInterval.this, ActivityHomeScreen.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
        final Button btn_start = (Button) findViewById(R.id.btn_start);
        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                startRecord();
                btn_start.setVisibility(View.GONE);
                btn_finish.setVisibility(View.VISIBLE);
            }
        });

    }

    /**
     * stop recording data and commit session to the sharedpreferences if logging is enabled
     */
    public void StopRecord()
    {
        mPlay = false;
        mTimer.finishTimer();
        // save
        if (mLog_state) {
            mAppData.addSession(mSessionData);
            SharedPreferences mPrefs = getSharedPreferences("fitMe", MODE_PRIVATE);
            SharedPreferences.Editor prefsEditor = mPrefs.edit();
            Gson gson = new Gson();
            String json = gson.toJson(mAppData);
            prefsEditor.putString("fitMeData", json);
            prefsEditor.commit();
        }
    }

    /**
     * start logging data and start timer
     */
    public void startRecord()
    {
        mSessionData = new DataIntervalSession(mNumberOfRound,mRunDuration,mRestDuration);
        mSessionData.setNumberOfRounds(mNumberOfRound);
        mSessionData.setRoundLength_rest(mRestDuration);
        mSessionData.setRoundLength_run(mRunDuration);
        mPlay = true;
        mTimer.startTimer();
    }


    @Override
    protected void onSaveInstanceState (Bundle bundle)
    {
    }

    protected void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        } else
            Log.d(TAG, "start: services not connected");
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "ActivityInterval Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://comvlievin.github.fitme/http/host/path")
        );
        AppIndex.AppIndexApi.start(mGoogleApiClient, viewAction);
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "ActivityInterval Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://comvlievin.github.fitme/http/host/path")
        );
        AppIndex.AppIndexApi.end(mGoogleApiClient, viewAction);
        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        } else
            Log.d(TAG, "stop: services not connected");
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
        }
        else
            Log.d(TAG,"resume: services not connected");
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "Connected to Google Play Services!");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            updateUI(lastLocation);
            startLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "Can't connect to Google Play Services!");
    }

    @Override
    public void onLocationChanged(Location location) {

        updateUI(location);
    }

    /**
     * update google map fragment
     * @param lastLocation last known location
     */
    private void updateUI(Location lastLocation)
    {
        float speed = lastLocation.getSpeed() * 3600 / 1000; // speed in km
        mSpeed.setText(Float.toString(speed) + " km/h");
        double lat = lastLocation.getLatitude(), lon = lastLocation.getLongitude();
        LatLng pos = new LatLng(lat,lon);
        if (mPlay) {
            mSessionData.addPoint(pos, System.currentTimeMillis());
            mSessionData.addSpeed(speed);
            if (mSessionData.getmPositions().size() == 1) // place a marker on the starting point
            {
                LatLng begin = mSessionData.getmPositions().get(0);
                mMap.addMarker(new MarkerOptions().position(begin).title("starting point"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(begin));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(16.0f));
            }
            if (mSessionData.getmPositions().size() > 1) { // draw the user's path
                mMap.moveCamera(CameraUpdateFactory.newLatLng(pos));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(16.0f));
                PolylineOptions polyLine = new PolylineOptions().color( mColorMax ).width((float) 15.0);
                List<LatLng> positions = mSessionData.getmPositions();
                polyLine.addAll(positions);
                mMap.addPolyline(polyLine);
            }
        }
    }
}





