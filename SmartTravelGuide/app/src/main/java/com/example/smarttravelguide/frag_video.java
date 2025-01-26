package com.example.smarttravelguide;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;

import java.io.IOException;
import java.net.URI;

public class frag_video extends Fragment {
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
        View view_1 = inflater.inflate(R.layout.fragment_frag_video, container, false);

        ListView listView = view_1.findViewById(R.id.video_list);
        listView.setAdapter(mainActivity.youtube_Adapter);
        if(mainActivity.youtube_Adapter != null)
            view_1.findViewById(R.id.frag_video_watermark).setVisibility(View.GONE);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Perform action on item click
                youtubePros item = (youtubePros) parent.getItemAtPosition(position);
                String key = item.url;
                OpenVideo(key);
            }
        });

        return view_1;
    }


    private void OpenVideo(String key){
        String url = "https://www.youtube.com/watch?v=" + key; // Replace with the URL you want to open
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setPackage("com.android.chrome");
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            intent.setPackage(null);
            startActivity(intent);
        }
    }
}