package comvlievin.github.fitme;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;


/**
 * fragment used to define a round duration
 */
public class FragmentTimeSelector extends Fragment {

    private int mSeconds = 30;
    private int mMinutes = 0;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "kind";
    private static final String ARG_PARAM2 = "default";

    // TODO: Rename and change types of parameters
    private String mKind;
    private int mDefault;

    //private OnFragmentInteractionListener mListener;

    public FragmentTimeSelector() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static FragmentTimeSelector newInstance(String param1 , int param2) {
        FragmentTimeSelector fragment = new FragmentTimeSelector();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putInt(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mKind = getArguments().getString(ARG_PARAM1);
            mDefault = getArguments().getInt(ARG_PARAM2);
            mSeconds = mDefault%60;
            mMinutes = mDefault/60;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_time_selector, container, false);
        NumberPicker mins = (NumberPicker) view.findViewById(R.id.numberPickerMin);
        NumberPicker secs = (NumberPicker) view.findViewById(R.id.numberPickerSeconds);
        mins.setMaxValue(60);
        secs.setMaxValue(60);
        secs.setValue(mSeconds);
        mins.setValue(mMinutes);
        addListeners(view);
        return view;
    }

    public void addListeners(View view) {
        final TextView textNumberOfRounds = (TextView) view.findViewById(R.id.textView_rounds);
        NumberPicker mins = (NumberPicker) view.findViewById(R.id.numberPickerMin);
        NumberPicker secs = (NumberPicker) view.findViewById(R.id.numberPickerSeconds);
        mins.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int oldValue, int newValue) {
                mMinutes = numberPicker.getValue();
            }
        });
        secs.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int oldValue, int newValue) {
                mSeconds = numberPicker.getValue();
            }
        });
    }

    public int getValue()
    {
        return 60 * mMinutes + mSeconds;

    }
}
