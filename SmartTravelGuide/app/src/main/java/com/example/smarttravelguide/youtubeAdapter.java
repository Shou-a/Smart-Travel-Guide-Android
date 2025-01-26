package com.example.smarttravelguide;

        import android.content.Context;
        import android.net.Uri;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.ArrayAdapter;
        import android.widget.ImageView;
        import android.widget.TextView;

        import androidx.annotation.NonNull;
        import androidx.annotation.Nullable;

        import com.bumptech.glide.Glide;

        import java.util.List;

public class youtubeAdapter extends ArrayAdapter {

    List<youtubePros> data;
    Context context;
    int count = 1;

    public youtubeAdapter(@NonNull Context context, List<youtubePros> data) {
        super(context, R.layout.youtube_video_card, data);
        this.context = context;
        this.data = data;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.youtube_video_card, parent, false);

        TextView title = view.findViewById(R.id.youtube_title);
        TextView disc = view.findViewById(R.id.youtube_description);
        ImageView url = view.findViewById(R.id.youtube_thumbnail);

        title.setText(data.get(position).title);
        disc.setText(data.get(position).description);
        url.setImageURI(Uri.parse(data.get(position).thumbnail));

        Glide.with(getContext())
                .load(data.get(position).thumbnail)
                .placeholder(R.drawable.location)
                .into(url);

        return view;
    }
}

