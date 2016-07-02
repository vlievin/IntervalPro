package comvlievin.github.fitme;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.google.gson.Gson;

/**
 * Home screen activity: enable the user to start an interval activity,
 * to access previous sessions,
 * to start and stop logging activities' data,
 * to get an overview of the monthly activities and
 * to delete all the data.
 *
 */
public class ActivityHomeScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);
        init_UI();
        addListenerOnButton();
    }

    /**
     * initialize UI: logging checkbox value and monthly summary
     */
    public void init_UI()
    {
        SharedPreferences mPrefs = getSharedPreferences("fitMe", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = mPrefs.getString("fitMeData", "");
        boolean log_state = mPrefs.getBoolean("log", true);
        CheckBox check_log = (CheckBox) findViewById(R.id.check_log);
        if (check_log != null) {
            check_log.setChecked(log_state);
        }
        if (json != "") {
            DataAppSaving mAppData = gson.fromJson(json, DataAppSaving.class);
            TextView nb_sessions = (TextView) findViewById(R.id.text_number_sessions);
            if (nb_sessions != null) {
                nb_sessions.setText(""+mAppData.getNumberOfSessionsThisMonth());
            }
            TextView distance = (TextView) findViewById(R.id.text_total_dist);
            if (distance != null) {
                distance.setText(""+String.format("%.1f", (0.001*mAppData.getTotalDistance()) )+"km");
            }
        }
        else
        {
            //default values
            TextView nb_sessions = (TextView) findViewById(R.id.text_number_sessions);
            if (nb_sessions != null) {
                nb_sessions.setText("0");
            }
            TextView distance = (TextView) findViewById(R.id.text_total_dist);
            if (distance != null) {
                distance.setText("0 km");
            }
        }
    }

    /**
     * this method is called when the app resume itself. The UI is update in order to change the monthly summary data if it has been changed while the activity was paused.
     */
    @Override
    public void onResume() {
        super.onResume();
        init_UI();
    }

    /**
     * add listeners callbacks for the buttons
     */
    public void addListenerOnButton() {
        final CheckBox check_log = (CheckBox) findViewById(R.id.check_log);
        if (check_log != null)
        {
            check_log.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    SharedPreferences mPrefs = getSharedPreferences("fitMe", MODE_PRIVATE);
                    SharedPreferences.Editor editor = mPrefs.edit();
                    editor.putBoolean("log", check_log.isChecked());
                    editor.commit();
                }
            });
        }
        final Button btn_interval = (Button) findViewById(R.id.btn_interval);
        if (btn_interval != null) {
            btn_interval.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    Intent intent = new Intent(ActivityHomeScreen.this, ActivityIntervalSetup.class);
                    startActivity(intent);
                }
            });
        }
        final Button btn_delete = (Button) findViewById(R.id.btn_delete);
        if (btn_delete != null) {
            btn_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    SharedPreferences mPrefs = getSharedPreferences("fitMe", MODE_PRIVATE);
                    SharedPreferences.Editor prefsEditor = mPrefs.edit();
                    prefsEditor.clear();
                    prefsEditor.commit();
                }
            });
        }
        final Button btn_overall = (Button) findViewById(R.id.btn_overall);
        if (btn_overall != null) {
            btn_overall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    Intent intent = new Intent(ActivityHomeScreen.this, ActivityDisplayAllSessions.class);
                    startActivity(intent);
                }
            });
        }
    }
}
