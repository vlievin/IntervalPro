package comvlievin.github.fitme;

import android.location.Location;
import android.text.format.Time;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

/**
 * store a session for a given session and provide interesting measures about the session
 */
public class DataIntervalSession {
    // list of positions
    private List<LatLng> mPositions = new ArrayList<>();
    // absolute time for each measures
    private List<Long> mTimes = new ArrayList<>();
    // speed at each measure
    private List<Float> mSpeeds = new ArrayList<>();
    // number of rounds
    private int mNumberOfRounds;
    // running duration for each round
    private int mRoundLength_run;
    // resting duration for each round
    private int mRoundLength_rest;
    // date of the session
    private Time mDate;

    /**
     * constructor
     * @param mNumberOfRounds number of rounds
     * @param mRoundLength_run  running duration
     * @param mRoundLength_rest resting duration
     */
    public DataIntervalSession(int mNumberOfRounds, int mRoundLength_run, int mRoundLength_rest) {
        this.mNumberOfRounds = mNumberOfRounds;
        this.mRoundLength_run = mRoundLength_run;
        this.mRoundLength_rest = mRoundLength_rest;
        Time today = new Time(Time.getCurrentTimezone());
        today.setToNow();
        this.mDate = today;
    }

    /**
     * know if a date is included in a running period (or resting)
     * @param absoluteTime absolute time of the given measurement
     * @return  true if the point has been measured during a running session
     */
    public boolean isRunningPeriod(long absoluteTime)
    {
        long sessionsLength = 1000 * (mRoundLength_rest + mRoundLength_run);
        long t0 = mTimes.get(0);
        long t_inSession = (absoluteTime - t0)%t0;
        if (t_inSession > (1000 * mRoundLength_run) )
        {
            return false;
        }
        else
            return true;
    }

    /**
     * @return the average speed during all the running periods
     */
    public float getAverageSpeed_running()
    {
        // only keep segments that are completely in a running period (first iteration)
        float s = 0f;
        int n = 0;
        int sessionsLength = mRoundLength_rest + mRoundLength_run;

        for ( int i = 0; i < mTimes.size(); i++)
        {
            if (isRunningPeriod(mTimes.get(i))) {
                s += mSpeeds.get(i);
                n++;
            }
        }
        if (n!=0)
            return s/(float)n;
        else
            return 0;

    }
    public float getAverageSpeed_resting()
    {
        // only keep segments that are completely in a resting period (first iteration)
        float s = 0f;
        int n = 0;
        for ( int i = 0; i < mTimes.size(); i++)
        {
            if (!isRunningPeriod(mTimes.get(i))) {
                s += mSpeeds.get(i);
                n++;
            }
        }
        if (n!=0)
            return s/(float)n;
        else
            return 0;

    }
    public float getAvergaeSpeed()
    {
        if (mSpeeds.size() > 0) {
            float s = 0;
            for (int i = 0; i < mSpeeds.size() - 1; i++) {
                s += mSpeeds.get(i);
            }
            return s / (float)(mSpeeds.size());
        }
        else
            return 0;

    }

    /**
     * get the session duration
     * @return duration in seconds
     */
    public float getTotalTime()
    {
        if (mTimes.size()>1)
            return 0.001f * ( this.getTimes().get( this.getTimes().size() - 1 ) - this.getTimes().get(0) );
        else
            return 0f;
    }

    /**
     * return the total distance
     * @return distance in km
     */
    public float getDistance()
    {
        float distance = 0;
        List<LatLng> positions = this.getmPositions();
        for (int i = 0 ; i < positions.size() - 1 ; i++ )
        {
            Location A = new Location("A");
            A.setLatitude( positions.get(i).latitude );
            A.setLongitude(positions.get(i).longitude);
            Location B = new Location("B");
            B.setLatitude(positions.get(i + 1).latitude);
            B.setLongitude(positions.get(i + 1).longitude);

            distance += A.distanceTo(B);
        }
        return distance;
    }

    /**
     * @return the number of achieved rounds
     */
    public int getNumberOfAchievedRounds()
    {
        int total_time = (int)getTotalTime();
        int roundDuration = mRoundLength_rest + mRoundLength_run;
        int total = (total_time+mRoundLength_rest)/roundDuration;
        if (total>mNumberOfRounds)
            total = mNumberOfRounds;
        return total;
    }
    public List<Float> getSpeeds() {
        return mSpeeds;
    }
    public void setSpeeds(List<Float> mSpeeds) {
        this.mSpeeds = mSpeeds;
    }
    public void addSpeed( float speed )
    {
        this.mSpeeds.add(speed);
    }
    public void addPoint(LatLng pos, Long time)
    {
        this.mPositions.add(pos);
        this.mTimes.add(time);
    }
    public List<LatLng> getmPositions() {
        return mPositions;
    }

    public void setPositions(List<LatLng> mPositions) {
        this.mPositions = mPositions;
    }
    public List<Long> getTimes() {
        return mTimes;
    }
    public void setTimes(List<Long> mTimes) {
        this.mTimes = mTimes;
    }
    public int getNumberOfRounds() {
        return mNumberOfRounds;
    }

    public void setNumberOfRounds(int mNumberOfRounds) {
        this.mNumberOfRounds = mNumberOfRounds;
    }

    public int getRoundLength_run() {
        return mRoundLength_run;
    }

    public void setRoundLength_run(int mRoundLength_run) {
        this.mRoundLength_run = mRoundLength_run;
    }

    public int getRoundLength_rest() {
        return mRoundLength_rest;
    }

    public void setRoundLength_rest(int mRoundLength_rest) {
        this.mRoundLength_rest = mRoundLength_rest;
    }
    public Time getDate() {
        return mDate;
    }
}
