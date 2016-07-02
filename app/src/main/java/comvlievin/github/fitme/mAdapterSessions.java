package comvlievin.github.fitme;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;


/**
 * custom adapter for the ActivityDisplayAllSessions
 */
public class mAdapterSessions extends BaseAdapter {
    private final Context context;
    private final List<DataIntervalSession> sessions;

    public mAdapterSessions(Context context, List<DataIntervalSession> sessions) {
        super();
        this.context = context;
        this.sessions = sessions;
        Log.d("adaptarer: " , "sessisons: " + sessions.size() );
    }

    public int getCount() {
        return this.sessions.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        Log.d("adapter: " , "position: " + position );
        if (rowView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            rowView = inflater.inflate(R.layout.adaptater_session_view, parent, false);
            DataIntervalSession session = this.sessions.get(position);
            TextView run = (TextView) rowView.findViewById(R.id.text_view_run);
            TextView rest = (TextView) rowView.findViewById(R.id.text_view_rest);
            TextView rounds = (TextView) rowView.findViewById(R.id.text_view_rounds);
            TextView date = (TextView) rowView.findViewById(R.id.text_view_date);
            try
            {
                run.setText( session.getRoundLength_run() + "s");
                rest.setText( session.getRoundLength_rest() + "s");
                rounds.setText( session.getNumberOfAchievedRounds() + " / " + session.getNumberOfRounds() +  " rounds");
                date.setText( session.getDate().format("%d/%m/%Y %H:%M:%S")  );
            }
            catch (IndexOutOfBoundsException e)
            {
                // do nothing
            }
        }
        return rowView;
    }
}
