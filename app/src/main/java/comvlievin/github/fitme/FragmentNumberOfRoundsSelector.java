package comvlievin.github.fitme;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;


/**
 * fragment used to set the number of rounds for each sessions
 */
public class FragmentNumberOfRoundsSelector extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    private int mNumberOfRounds;
    private static final String ARG_PARAM1 = "default";

    // TODO: Rename and change types of parameters

    //private OnFragmentInteractionListener mListener;

    public FragmentNumberOfRoundsSelector() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static FragmentNumberOfRoundsSelector newInstance(int param1) {
        FragmentNumberOfRoundsSelector fragment = new FragmentNumberOfRoundsSelector();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mNumberOfRounds = getArguments().getInt(ARG_PARAM1);
            Log.d("-->fragment", "arg: " + mNumberOfRounds);
        }
        else
        {
            Log.d("-->fragment", "no argument");
            mNumberOfRounds = 10;
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            mNumberOfRounds = savedInstanceState.getInt("rounds");
            final TextView textNumberOfRounds = (TextView) getView().findViewById(R.id.textView_rounds);
            textNumberOfRounds.setText("" + mNumberOfRounds);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's current game state
        savedInstanceState.putInt("rounds", mNumberOfRounds);
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
        Log.d("-->fragmentrounds" , "onSaveInstance");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_number_of_rounds_selector, container, false);
        final TextView textNumberOfRounds = (TextView) view.findViewById(R.id.textView_rounds);
        textNumberOfRounds.setText("" + mNumberOfRounds);
        addListeners(view);
        return view;
    }

    public int  getValue()
    {
        return mNumberOfRounds;
    }
    public void addListeners(View view)
    {
        final TextView textNumberOfRounds = (TextView) view.findViewById(R.id.textView_rounds);
        Button btn_minus = (Button) view.findViewById(R.id.btn_rounds_minus);
        Button btn_plus = (Button) view.findViewById(R.id.btn_rounds_plus);
        btn_minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mNumberOfRounds--;
                if (mNumberOfRounds < 1)
                    mNumberOfRounds = 1;
                textNumberOfRounds.setText("" + mNumberOfRounds);
            }
        });
        btn_plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mNumberOfRounds++;
                textNumberOfRounds.setText("" + mNumberOfRounds);
            }
        });
    }
}
