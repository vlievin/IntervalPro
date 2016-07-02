package comvlievin.github.fitme;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import android.os.CountDownTimer;

import android.util.Log;


/**
 * fragment activity that display a timer designed for interval training
 * (indicates when the user must run or rest, displays the number of remaining rounds and displays the remaining time for each period with a custom circular loading bar)
 */
public class FragmentTimer extends Fragment {
    // countDown timer
    private CountDownTimer mCountDownTimer_Run;
    private CountDownTimer mCountDownTimer_Rest;
    // custom View
    private MyView chronoView;
    // layout where is drawn the timer
    private LinearLayout drawZone;
    // running duration time for each period
    private long mRunDuration;
    // resting duration time for each period
    private long mRestDuration;
    // display update rate
    private final long interval = 16;
    // time elapsed during each period
    private long timeElapsed = 0;
    // resting state (running or resting)
    private boolean rest_state = false;
    // number of rounds
    private int mNumberOfRound;
    // number of rounds until the end of the session
    private int roundsLeft;
    // playing state
    private boolean playing = false;
    // finished state
    private boolean finished = false;
    // this view
    private View mFragmentView;
    // duration
    private long mDuration = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        // Inflate the layout for this fragment
        mFragmentView = inflater.inflate(R.layout.fragment_fragment_timer, container, false);
        drawZone = (LinearLayout) mFragmentView.findViewById(R.id.drawZone);
        chronoView = new MyView( getContext() );
        drawZone.addView(chronoView);
        if (savedInstanceState != null)
        {
            if ( savedInstanceState.containsKey("roundsLeft"))
            {
                roundsLeft = savedInstanceState.getInt("roundsLeft");
            }
            if ( savedInstanceState.containsKey("timeElapsed"))
            {
                timeElapsed = savedInstanceState.getLong("timeElapsed");
            }
        }
        return mFragmentView;//inflater.inflate(R.layout.fragment_fragment_timer, container, false);
    }

    /**
     * setup fragment
     * @param mNumberOfRound
     * @param mRunDuration
     * @param mRestDuration
     */
    public void setup( int mNumberOfRound, int mRunDuration, int mRestDuration )
    {
        this.mNumberOfRound = mNumberOfRound;
        this.roundsLeft = mNumberOfRound;
        this.mRunDuration = 1000 * mRunDuration;
        this.mRestDuration = 1000 * mRestDuration;

        mCountDownTimer_Run = new MyCountDownTimer(this.mRunDuration, interval);
        mCountDownTimer_Rest = new MyCountDownTimer(this.mRestDuration, interval);
    }


    @Override
    public void onSaveInstanceState (Bundle bundle)
    {
        super.onSaveInstanceState(bundle);
        bundle.putInt("roundsLeft", roundsLeft);
        bundle.putLong("timeElapsed", timeElapsed);

    }

    /**
     * start timer
     */
    public void startTimer()
    {
        if (rest_state)
            mCountDownTimer_Rest.start();
        else
            mCountDownTimer_Run.start();
        playing = true;
        mDuration = System.currentTimeMillis();
    }

    /**
     * reset timer
     */
    public void resetTimer()
    {
        stopActivity();
    }

    /**
     * end timer
     */
    public void finishTimer()
    {
        stopActivity();
    }

    /**
     * actions that must be done at the end of the session
     */
    private void stopActivity()
    {
        mCountDownTimer_Run.cancel();
        mCountDownTimer_Rest.cancel();
        roundsLeft = mNumberOfRound;
        timeElapsed = 0;
        rest_state = false;
        playing = false;
        finished = true;
        chronoView.invalidate();
    }

    /**
     * custom countDown timer
     */
    public class MyCountDownTimer extends CountDownTimer {
        public MyCountDownTimer(long startTime, long interval) {
            super(startTime, interval);
        }

        /**
         * different actions at the end of each timer
         */
        @Override
        public void onFinish() {
            timeElapsed = 0;
            if (!rest_state)
            {
                rest_state = true;
                if (roundsLeft > 0)
                {
                    mCountDownTimer_Rest.start();
                }
                else
                {
                    mCountDownTimer_Rest.cancel();
                    roundsLeft = mNumberOfRound;
                    finished = true;
                    playing = false;
                    timeElapsed = 0;
                    rest_state = false;
                    chronoView.invalidate();
                }
            }
            else
            {
                rest_state = false;
                roundsLeft--;
                if (roundsLeft > 0)
                {
                    mCountDownTimer_Run.start();
                }
                else
                {
                    mDuration = mDuration - System.currentTimeMillis();
                    finished = true;
                    playing = false;
                    chronoView.invalidate();
                    Intent intent = new Intent("timesup");
                    LocalBroadcastManager.getInstance( getContext() ).sendBroadcast(intent);
                }
            }
        }
        @Override
        public void onTick(long millisUntilFinished) {
            timeElapsed = millisUntilFinished;
            chronoView.invalidate();
        }
    }

    /*** draw the cutom timer ***/
    /**
     * custom view with built in drawer
     */
    public class MyView extends View {
        private Paint paint;
        private Path path;
        private int radius = this.getWidth() / 4;
        private int center_x = this.getWidth() / 2;
        private int center_y = this.getHeight() / 2;
        private int res = 200;

        public MyView(Context context) {
            super(context);
            // TODO Auto-generated constructor stub
        }

        @Override
        protected void onDraw(Canvas canvas) {
            // TODO Auto-generated method stub
            super.onDraw(canvas);

            radius = this.getWidth() / 4;
            center_x = this.getWidth() / 2;
            center_y = 2 *this.getHeight() / 5;
            paint = new Paint();

            // what to display when the session is finished
            if (finished)
            {
                paint.setTextSize(100);
                paint.setTextAlign(Paint.Align.CENTER);
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(getResources().getColor(R.color.colorPrimary));
                String timestr = "Amazing!";
                int xPos = center_x;
                int yPos = center_y;
                Rect r = new Rect();
                paint.getTextBounds(timestr, 0, timestr.length(), r);
                int textH = (Math.abs(r.height())) / 2;
                yPos += textH;
                canvas.drawText(timestr, xPos, yPos, paint);
            }
            // what to display during the session
            else {
                if (playing) {
                    path = new Path();
                    // rounds
                    paint.setStrokeWidth(20);
                    paint.setAntiAlias(true);
                    paint.setStrokeCap(Paint.Cap.ROUND);
                    paint.setStrokeJoin(Paint.Join.ROUND);
                    paint.setStyle(Paint.Style.FILL);
                    paint.setColor(getResources().getColor(R.color.colorAchieved));
                    for (int i = 0; i < mNumberOfRound; i++) {
                        int radRound = 30;
                        int marginRound = 3 * radRound;
                        float x_round = marginRound + (this.getWidth() - 2 * marginRound) * (float) i / (float) (mNumberOfRound - 1);

                        if (i > mNumberOfRound - roundsLeft)
                            paint.setColor(getResources().getColor(R.color.colorNeutral));
                        else if (i == mNumberOfRound - roundsLeft) {
                            if (rest_state) {
                                paint.setColor(getResources().getColor(R.color.colorPrimary));
                            } else {
                                if (rest_state) {
                                    paint.setColor(getResources().getColor(R.color.colorPrimary));
                                } else {
                                    paint.setColor(getResources().getColor(R.color.colorAccent));
                                }
                            }
                            radRound += 5 * Math.cos(4* Math.PI  * timeElapsed / (1000) );
                        }
                        canvas.drawCircle((int) x_round, this.getHeight() - marginRound, radRound, paint);

                    }
                    paint.setStyle(Paint.Style.STROKE);
                    long length;
                    if (rest_state) {
                        length = mRestDuration;
                        paint.setColor(getResources().getColor(R.color.colorPrimary));
                    } else {
                        length = mRunDuration;
                        paint.setColor(getResources().getColor(R.color.colorAccent));
                    }
                    float alpha = ((float) length - (float) timeElapsed) / (float) length;
                    float n = ((float) res * alpha);
                    path.moveTo(center_x, center_y - radius);
                    for (int i = 0; i < n; i++) {
                        double t = (double) i * 2 * Math.PI / (double) res;
                        double x = center_x + ( (double)radius * Math.sin(t));
                        double y = center_y - ( (double)radius * Math.cos(t));
                        path.lineTo((int)x, (int)y);
                    }
                    canvas.drawPath(path, paint);
                    // text
                    paint.setTextSize(300);
                    paint.setTextAlign(Paint.Align.CENTER);
                    paint.setStyle(Paint.Style.FILL);
                    String timestr = Long.toString(timeElapsed / 1000 + 1);
                    int xPos = center_x;
                    int yPos = center_y - 50;
                    Rect r = new Rect();
                    paint.getTextBounds(timestr, 0, timestr.length(), r);
                    int textH = (Math.abs(r.height())) / 2;
                    yPos += textH;
                    canvas.drawText(timestr, xPos, yPos, paint);
                    paint.setTextSize(100);
                    if (rest_state) {
                        canvas.drawText("rest", xPos, yPos + textH, paint);
                    } else {
                        canvas.drawText("run", xPos, yPos + textH, paint);
                    }
                }
                // what to display before the session
                else
                {
                    paint.setTextSize(120);
                    paint.setTextAlign(Paint.Align.CENTER);
                    paint.setStyle(Paint.Style.FILL);
                    paint.setColor(getResources().getColor(R.color.colorPrimary));
                    String timestr = "Ready?";
                    int xPos = center_x;
                    int yPos = center_y;
                    Rect r = new Rect();
                    paint.getTextBounds(timestr, 0, timestr.length(), r);
                    int textH = (Math.abs(r.height())) / 2;
                    yPos += textH;
                    canvas.drawText(timestr, xPos, yPos, paint);
                }
            }

        }
    }
}



