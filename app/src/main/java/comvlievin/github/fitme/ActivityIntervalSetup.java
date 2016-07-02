package comvlievin.github.fitme;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import android.support.design.widget.TabLayout;

/**
 * activity that setups an interval activity
 * use a pager to go though the 3 distinct fragments
 */
public class ActivityIntervalSetup extends AppCompatActivity {

    MyPageAdapter pageAdapter;
    private List<Fragment> mFragmentsPager;
    private int mDefaultRounds = 10;
    private int mDefaultRunDuration = 30;
    private int mDefaultResDuration = 30;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interval_setup);
        // get the preferences of the last session
        SharedPreferences mPrefs = getSharedPreferences("fitMe", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = mPrefs.getString("fitMeData", "");
        if (json != "") {
            DataAppSaving mAppData = gson.fromJson(json, DataAppSaving.class);
            DataIntervalSession mSessionData = mAppData.getLastSession();
            mDefaultRounds = mSessionData.getNumberOfRounds();
            mDefaultResDuration = mSessionData.getRoundLength_rest();
            mDefaultRunDuration = mSessionData.getRoundLength_run();
        }
        else
        {
            Log.d("########setup:" , "no json");
        }
        List<Fragment> fragments = setupFragments();
        pageAdapter = new MyPageAdapter(getSupportFragmentManager(), fragments);
        ViewPager pager = (ViewPager)findViewById(R.id.viewpager);
        pager.setAdapter(pageAdapter);
        getSupportActionBar().setTitle("Setup Interval Activity");
        addListenerOnButton();

        // Give the TabLayout the ViewPager
        TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(pager);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's current game state
        FragmentNumberOfRoundsSelector fRounds = (FragmentNumberOfRoundsSelector) mFragmentsPager.get(0);
        int n_rounds = fRounds.getValue();
        FragmentTimeSelector fRun = (FragmentTimeSelector) mFragmentsPager.get(1);
        int t_run = fRun.getValue();
        FragmentTimeSelector fTest = (FragmentTimeSelector) mFragmentsPager.get(2);
        int t_rest = fTest.getValue();
        savedInstanceState.putInt("round", n_rounds);
        savedInstanceState.putInt("run", t_run);
        savedInstanceState.putInt("rest", t_rest);
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        // Always call the superclass so it can restore the view hierarchy
        super.onRestoreInstanceState(savedInstanceState);

        // Restore state members from saved instance
        mDefaultRunDuration = savedInstanceState.getInt("run");
        mDefaultResDuration = savedInstanceState.getInt("rest");
        mDefaultRounds = savedInstanceState.getInt("rounds");
        setupFragments();
    }

    /**
     * setup all fragments
     * @return return the Pager
     */
    private List<Fragment> setupFragments(){
        mFragmentsPager = new ArrayList<Fragment>();
        mFragmentsPager.add(FragmentNumberOfRoundsSelector.newInstance(mDefaultRounds));
        mFragmentsPager.add(FragmentTimeSelector.newInstance("Running duration for each round" , mDefaultRunDuration));
        mFragmentsPager.add(FragmentTimeSelector.newInstance("Resting duration for each round", mDefaultResDuration));
        return mFragmentsPager;
    }

    class MyPageAdapter extends FragmentPagerAdapter {
        private List<Fragment> fragments;
        private String tabTitles[] = new String[] { "Rounds", "Running", "Resting" };

        public MyPageAdapter(FragmentManager fm, List<Fragment> fragments) {
            super(fm);
            this.fragments = fragments;
        }
        @Override
        public CharSequence getPageTitle(int position) {
            // Generate title based on item position
            return tabTitles[position];
        }

        @Override
        public Fragment getItem(int position) {
            return this.fragments.get(position);
        }

        @Override
        public int getCount() {
            return this.fragments.size();
        }
    }

    public void addListenerOnButton() {
        FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ActivityIntervalSetup.this, ActivityInterval.class);
                    Bundle mBundle = new Bundle();
                    FragmentNumberOfRoundsSelector fRounds = (FragmentNumberOfRoundsSelector) mFragmentsPager.get(0);
                    int n_rounds = fRounds.getValue();
                    FragmentTimeSelector fRun = (FragmentTimeSelector) mFragmentsPager.get(1);
                    int t_run = fRun.getValue();
                    FragmentTimeSelector fTest = (FragmentTimeSelector) mFragmentsPager.get(2);
                    int t_rest = fTest.getValue();
                    mBundle.putInt("n_rounds", n_rounds);
                    mBundle.putInt("t_run", t_run);
                    mBundle.putInt("t_rest", t_rest);
                    intent.putExtras(mBundle);
                    startActivity(intent);
                    finish();
                }
            });
        }
    }
}
