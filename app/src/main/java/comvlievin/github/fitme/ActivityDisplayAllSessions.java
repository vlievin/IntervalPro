package comvlievin.github.fitme;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

/**
 * this activities displays all the stored sessions with a ListView, each element is clickable (send the user to the right session overview)
 */
public class ActivityDisplayAllSessions extends AppCompatActivity {
    // custom adapter
    private mAdapterSessions adapter;
    // ListView object
    private ListView mListview;
    // list of all stored sessions
    private List<DataIntervalSession> mSessions = new ArrayList<DataIntervalSession>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_all_sessions);
        // get shared preferences
        SharedPreferences mPrefs = getSharedPreferences("fitMe", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = mPrefs.getString("fitMeData", "");
        if (json != "") {
            DataAppSaving mAppData = gson.fromJson(json, DataAppSaving.class);
            mSessions = mAppData.getSessions();
        }
        else
        {
            Log.d("ActivityDisplayAllSess", "no data");
        }
        mListview = (ListView) findViewById(R.id.listview);
        adapter = new mAdapterSessions(this , mSessions);
        mListview.setAdapter(adapter);
        mListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                Intent intent = new Intent(ActivityDisplayAllSessions.this, ActivityIntervalOverall.class);
                Bundle b = new Bundle();
                b.putInt("position", position);
                intent.putExtras(b);
                startActivity(intent);
            }

        });
    }

    /**
     * refresh list view when the activity is resumed (data has potentially changed)
     */
    @Override
    public void onResume()
    {
        super.onResume();
        SharedPreferences mPrefs = getSharedPreferences("fitMe", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = mPrefs.getString("fitMeData", "");
        if (json != "") {
            DataAppSaving mAppData = gson.fromJson(json, DataAppSaving.class);
            mSessions = mAppData.getSessions();
        }
        adapter = new mAdapterSessions(this , mSessions);
        mListview.setAdapter(adapter);
        mListview.invalidate();

    }

}
