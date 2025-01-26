package com.example.smarttravelguide;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

public class frag_weather extends Fragment {
    MainActivity mainActivity;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity = (MainActivity)getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view_1 = inflater.inflate(R.layout.fragment_frag_weather, container, false);

        ListView listView = view_1.findViewById(R.id.weather_list);
        listView.setAdapter(mainActivity.weatherAdapter);

        if(mainActivity.weatherAdapter != null)
            view_1.findViewById(R.id.frag_weather_watermark).setVisibility(View.GONE);
        return view_1;
    }
}