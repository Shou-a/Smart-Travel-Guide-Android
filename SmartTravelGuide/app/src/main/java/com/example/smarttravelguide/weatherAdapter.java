package com.example.smarttravelguide;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class weatherAdapter extends ArrayAdapter {

    List<weatherProps> data;
    Context context;
    int count = 1;

    public weatherAdapter(@NonNull Context context, List<weatherProps> data) {
        super(context, R.layout.weather_day_card, data);
        this.context = context;
        this.data = data;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.weather_day_card, parent, false);

        TextView title = view.findViewById(R.id.weather_title);
        TextView disc = view.findViewById(R.id.weather_disc);
        TextView humidity = view.findViewById(R.id.weather_humidity);
        TextView temp = view.findViewById(R.id.weather_temp);
        TextView weather_air = view.findViewById(R.id.weather_air);

        title.setText((CharSequence) data.get(position).date);
        disc.setText(data.get(position).conditions);
        humidity.setText(" " +String.valueOf(data.get(position).humidity));
        temp.setText(" " +String.valueOf(data.get(position).temp));
        weather_air.setText(" " +String.valueOf(data.get(position).windspeed));


        return view;
    }

    public void setData(List<weatherProps> dataList) {
        data.clear();
        data.addAll(dataList);
    }
}
