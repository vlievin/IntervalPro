package comvlievin.github.fitme;


import android.text.format.Time;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

/**
 * models the data of the whole app, provide some interesting results
 */
public class DataAppSaving {
    // list of all sessions
    private List<DataIntervalSession> mSessions;
    /**
     * constructor
     */
    public DataAppSaving()
    {
        mSessions = new ArrayList<DataIntervalSession>();
    }
    public List<DataIntervalSession> getSessions() {
        return mSessions;
    }
    public DataIntervalSession getSession(int i)
    {
        return mSessions.get(i);
    }
    public void setSessions(List<DataIntervalSession> mSessions) {
        this.mSessions = mSessions;
    }
    public void addSession(DataIntervalSession session) {
        this.mSessions.add(session);
    }
    public DataIntervalSession getLastSession()
    {
        if (mSessions != null && mSessions.size()>0)
            return mSessions.get(mSessions.size()-1);
        else
            return new DataIntervalSession(10,30,30);
    }
    /**
     *
     * @return the number of session this month
     */
    public int getNumberOfSessionsThisMonth()
    {
        int s = 0;
        Time now = new Time();
        now.setToNow();
        now.setToNow();
        int month = now.MONTH;
        int day = now.MONTH_DAY;
        int year = now.YEAR;
        now.set(day, month - 1 , year);
        for(int i = 0; i < mSessions.size();i++)
            if ( Time.compare(now , mSessions.get(i).getDate()) < 0 )
                s += 1;
        return s;
    }

    /**
     * return the total traveled distance
     * @return
     */
    public float getTotalDistance()
    {
        Float s = 0f;
        Time now = new Time();
        now.setToNow();
        int month = now.MONTH;
        int day = now.MONTH_DAY;
        int year = now.YEAR;
        now.set(day, month - 1 , year);
        for(int i = 0; i < mSessions.size();i++)
            if ( Time.compare(now , mSessions.get(i).getDate()) < 0 )
                s += mSessions.get(i).getDistance();
        return s;
    }
}
