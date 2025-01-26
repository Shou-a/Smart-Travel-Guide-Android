package com.example.smarttravelguide;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.DialogCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView botNav;
    public AutocompleteSupportFragment frag_gMap;
    public String API_Key;
    private final String TAG = "Info :";
    boolean first_frag = true;
    Fragment frag_map;
    Fragment frag_weather;
    Fragment frag_video;
    LatLng toLocation;
    public GoogleMap gMap;

    public RequestQueue requestQueue;

    weatherAdapter weatherAdapter;
    youtubeAdapter youtube_Adapter;
    FusedLocationProviderClient fusedLocationProviderClient;
    final CountDownLatch latch = new CountDownLatch(1);
    public LatLng SelectedCoordinates = null;
    boolean hasLocationAccess = false;
    BusinessHelper helper;

    String Distance;
    String Duration;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        helper = new BusinessHelper(MainActivity.this);
        hasLocationAccess = helper.permissionCheck();

        fusedLocationProviderClient = (FusedLocationProviderClient) LocationServices.getFusedLocationProviderClient(this);

        API_Key = getString(R.string.map_api_key);

        frag_map = new frag_map();
        frag_weather = new frag_weather();
        frag_video = new frag_video();
        weatherAdapter = null;
        youtube_Adapter = null;

        requestQueue = Volley.newRequestQueue(this);

//        supportMapFragment = (SupportMapFragment) new frag_map().getChildFragmentManager().findFragmentById(R.id.google_map_fragment);

        statusCheck();

        botNav = findViewById(R.id.nav);
        botNav.setOnItemSelectedListener(item -> {
            int reqItem = item.getItemId();
            if (reqItem == R.id.map) {
                if (first_frag) {
                    add_Frag(frag_map, true);
                    first_frag = false;
                } else
                    add_Frag(frag_map, false);
//                Toast.makeText(this,"map", Toast.LENGTH_LONG).show();
            } else if (reqItem == R.id.weather) {
                add_Frag(frag_weather, false);
//                Toast.makeText(this, "You Don't have Access", Toast.LENGTH_LONG).show();
            } else if (reqItem == R.id.video) {
                add_Frag(frag_video, false);
//                Toast.makeText(this, "You Don't have Access", Toast.LENGTH_LONG).show();
            }

            return true;
        });
        botNav.setSelectedItemId(R.id.map);


//        initializeAutoCompleteBar();


    }

    public void add_Frag(Fragment fragment, boolean flag) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        boolean isAdded = fragment.isAdded();

        if(flag){
            ft.add(R.id.main_container, frag_map);
            ft.add(R.id.main_container, frag_weather);
            ft.add(R.id.main_container, frag_video);

            ft.hide(frag_weather);
            ft.hide(frag_video);
        }else{
            if(isAdded){
                if(fragment != frag_map)
                    ft.hide(frag_map);
                if(fragment != frag_weather)
                    ft.hide(frag_weather);
                if(fragment != frag_video)
                    ft.hide(frag_video);

                ft.show(fragment);
            }else{
                ft.add(R.id.main_container, fragment);
            }
        }
        ft.addToBackStack(null);
        ft.commit();
    }

    public void initializeAutoCompleteBar(Location location, GoogleMap map) {
        gMap = map;
        if (!Places.isInitialized()) {
            LatLng current_LatLong = new LatLng(location.getLatitude(), location.getLongitude());

            Places.initialize(getApplicationContext(), API_Key);
            frag_gMap = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.autoComplete_bar);

            PlacesClient placesClient = Places.createClient(this);

            // Initialize the AutocompleteSupportFragment.
            AutocompleteSupportFragment autocompleteFragment = frag_gMap;

            autocompleteFragment.setLocationBias(RectangularBounds.newInstance(
                    current_LatLong,
                    current_LatLong
            ));
            // Specify the types of place data to return.
            autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));

            // Set up a PlaceSelectionListener to handle the response.
            autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onPlaceSelected(@NonNull Place place) {
                    // TODO: Get info about the selected place.
                    Log.i(TAG, "Place: " + place.getName() + ", " + place.getId());
                    SelectedCoordinates = place.getLatLng();

                    FragmentManager fragmentManager = getSupportFragmentManager();

                    //fragmentManager.findFragmentById(R.id.main_container).getView().findViewById(R.id.btn_Direction).setVisibility(View.VISIBLE);
                    frag_map.getView().findViewById(R.id.btn_Direction).setVisibility(View.VISIBLE);
//
//                    MaterialButton button = fragment.getView().findViewById(R.id.btn_Direction);
//                    button.setVisibility(View.VISIBLE);

                    setDirections(current_LatLong, place.getLatLng(), null);
                    setWeather(place.getLatLng(), LocalDate.now().toString());
                    youtube_search(place.getLatLng());
                    frag_weather.getView().findViewById(R.id.frag_weather_watermark).setVisibility(View.GONE);
                    frag_video.getView().findViewById(R.id.frag_video_watermark).setVisibility(View.GONE);
                }

                @Override
                public void onError(@NonNull Status status) {
                    // TODO: Handle the error.
                    Log.i(TAG, "An error occurred: " + status);
                }
            });



        }
    }

    public void statusCheck() {
        final LocationManager manager = (LocationManager) getSystemService(MainActivity.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps(enumerators.permissions.Location);
        }
    }

    private void buildAlertMessageNoGps(enumerators.permissions type) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (type == enumerators.permissions.Location) {
            builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, final int id) {
                            startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, final int id) {
                            dialog.cancel();
                            finish();
                            System.exit(0);
                        }
                    });
            final AlertDialog alert = builder.create();
            alert.show();
        }

    }

    public void setDirections(LatLng from, LatLng to, LatLng focusOn) {
        toLocation = to;
        RequestQueue requestQueue = this.requestQueue;
        String url = Uri.parse("https://maps.googleapis.com/maps/api/directions/json")
                .buildUpon()
                .appendQueryParameter("destination", String.valueOf(to.latitude) + "," + String.valueOf(to.longitude))
                .appendQueryParameter("origin", String.valueOf(from.latitude) + "," + String.valueOf(from.longitude))
                .appendQueryParameter("mode", "driving")
                .appendQueryParameter("key", API_Key)
                .toString();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String status = response.getString("status");
                    if (status.equals("OK")) {

                        List<LatLng> posicoes = new ArrayList<LatLng>();
                        PolylineOptions polylineOptions = new PolylineOptions();

                        JSONObject json = response;
                        JSONObject jsonRoute = json.getJSONArray("routes").getJSONObject(0);
                        JSONObject leg = jsonRoute.getJSONArray("legs").getJSONObject(0);

                        Distance = leg.getJSONObject("distance").getString("text");
                        Duration = leg.getJSONObject("duration").getString("text");

                        JSONArray steps = leg.getJSONArray("steps");
                        final int numSteps = steps.length();
                        JSONObject step;
                        for (int i = 0; i < numSteps; i++) {
                            step = steps.getJSONObject(i);
                            String pontos = step.getJSONObject("polyline").getString("points");
                            posicoes.addAll(PolyUtil.decode(pontos));
                        }

                        polylineOptions.addAll(posicoes);
                        polylineOptions.color(R.color.red);
                        polylineOptions.zIndex(5);
                        polylineOptions.width(6);

                        gMap.clear();

                        gMap.addPolyline(polylineOptions);
                        MarkerOptions currentLoc = new MarkerOptions()
                                .position(from)
                                .title("You")
                                .zIndex(2.0f);

                        gMap.addMarker(currentLoc);
                        gMap.addMarker(new MarkerOptions().position(to).title("Destination"));

                        LatLngBounds bounds = new LatLngBounds.Builder()
                                .include(from)
                                .include(to).build();
                        Point point = new Point();
                        getWindowManager().getDefaultDisplay().getSize(point);
                        gMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds,10));
                        if(focusOn != null){
                            gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(focusOn, 20));
                        }

                        frag_map.getView().findViewById(R.id.DisDurHolder).setVisibility(View.VISIBLE);

                        TextView distanceView = (TextView)frag_map.getView().findViewById(R.id.distanceDisp);
                        distanceView.setText(Distance);

                        TextView durationView = (TextView)frag_map.getView().findViewById(R.id.durationDisp);
                        durationView.setText(Duration);

                    } else {
                        Toast.makeText(MainActivity.this, "status is not Success", Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
//                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "error", Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "MAP API Side Error", Toast.LENGTH_LONG).show();
            }
        });

        requestQueue.add(jsonObjectRequest);
    }

    public void map_focus_current(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("Live");
        gMap.addMarker(markerOptions);
        gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18));

    }

    public void LocationNavigate(){
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                setDirections(new LatLng(location.getLatitude(), location.getLongitude()), toLocation, new LatLng(location.getLatitude(), location.getLongitude()));
            }
        });
    }
    public void mapviewSatallite(){
        int type = gMap.getMapType();

        if(type == GoogleMap.MAP_TYPE_NORMAL){
            gMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        }
        else if(type == GoogleMap.MAP_TYPE_SATELLITE){
            gMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        }
        else if(type == GoogleMap.MAP_TYPE_TERRAIN){
            gMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }
    }

    public void setWeather(LatLng latLng , String date){

        String formattedDate = date.substring(0,10);

        RequestQueue requestQueue = this.requestQueue;
        double Latitude = latLng.latitude;
        double Longitude = latLng.longitude;

        DecimalFormat dFormat = new DecimalFormat("##.#####");

        Double new_Latitude = Double.valueOf(dFormat .format(Latitude));
        Double new_Longitude = Double.valueOf(dFormat .format(Longitude));
        String url = "https://weather.visualcrossing.com/VisualCrossingWebServices/rest/services/timeline/"+ new_Latitude + "," + new_Longitude +"/" + formattedDate + "/?key=7ZGN9WVY3VWVBACFUZ9K5J8EJ";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>(){
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        String timezone = response.getString("timezone");
                        if (timezone != null && timezone != "") {
                            JSONObject day = response.getJSONArray("days").getJSONObject(0);
                            JSONArray hours = day.getJSONArray("hours");
                            List<weatherProps> weatherProps = new ArrayList<>();
                            for (int i = 0; i<hours.length(); i++){
                                weatherProps weatherProps_hour = new weatherProps();
                                JSONObject hour = new JSONObject();
                                hour = hours.getJSONObject(i);


                                weatherProps_hour.date = "On : " + formattedDate + " at : " + hour.getString("datetime").substring(0,5);
                                weatherProps_hour.conditions = " " + hour.getString("conditions") + " (" + hour.getString("icon" )+")";
                                weatherProps_hour.humidity = hour.getDouble("humidity");
                                weatherProps_hour.temp = hour.getDouble("temp");
                                weatherProps_hour.windspeed = hour.getDouble("windspeed");
                                weatherProps.add(weatherProps_hour);
                            }
                            if(weatherAdapter == null){
                                MainActivity.this.weatherAdapter = new weatherAdapter(MainActivity.this, weatherProps);
                            }
                            else {
                                MainActivity.this.weatherAdapter = new weatherAdapter(MainActivity.this, weatherProps);
                                MainActivity.this.weatherAdapter.notifyDataSetChanged();
                            }

                            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                            if(frag_weather.isHidden()){
                                frag_weather = new frag_weather();
                                add_Frag(frag_weather, false);
                                ft.hide(frag_weather);
                            }else{
                                frag_weather = new frag_weather();
                                ft.replace(R.id.main_container, frag_weather);
                            }
                            ft.commit();

                        }
                    }catch (Exception ex){
                        String abc = "";
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(MainActivity.this, "Weather API Side Error", Toast.LENGTH_LONG).show();
                }
        });
        requestQueue.add(jsonObjectRequest);
    }

    public void youtube_search(LatLng latLng){

        double Latitude = latLng.latitude;
        double Longitude = latLng.longitude;

        //String url = "https://www.googleapis.com/youtube/v3/search?part=snippet&q=surfing&type=video&location=33.9867,-118.4737&locationRadius=10km&key=YOUR_API_KEY";

        RequestQueue requestQueue = this.requestQueue;
        String url_1 = Uri.parse("https://www.googleapis.com/youtube/v3/search?part=snippet")
                .buildUpon()
                .appendQueryParameter("q", "Best Hotel+Best Place to visit")
                .appendQueryParameter("type", "video")
                .appendQueryParameter("location", String.valueOf(Latitude) + "," +String.valueOf(Longitude))
                .appendQueryParameter("locationRadius", "20km")
                .appendQueryParameter("maxResults", "20")
                .appendQueryParameter("key", API_Key)
                .toString();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url_1, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                        JSONArray videos = response.getJSONArray("items");
                        List<youtubePros> youtubePros = new ArrayList<>();

                        for (int i = 0; i<videos.length(); i++){
                            JSONObject snippet = new JSONObject();
                            snippet = videos.getJSONObject(i).getJSONObject("snippet");

                            youtubePros youtubeProps = new youtubePros();

                            youtubeProps.title = snippet.getString("title");
                            youtubeProps.description = snippet.getString("description");

                            JSONObject thumb = new JSONObject();
                            thumb = snippet.getJSONObject("thumbnails").getJSONObject("default");

                            youtubeProps.url = videos.getJSONObject(i).getJSONObject("id").getString("videoId");
                            youtubeProps.thumbnail = thumb.getString("url");

                            youtubePros.add(youtubeProps);
                        }

                    if(youtube_Adapter == null){
                        youtubeAdapter adapter = new youtubeAdapter(MainActivity.this, youtubePros);
                        MainActivity.this.youtube_Adapter = adapter;
                    }
                    else {
                        MainActivity.this.youtube_Adapter = new youtubeAdapter(MainActivity.this, youtubePros);
                        MainActivity.this.youtube_Adapter.notifyDataSetChanged();
                    }

                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    if(frag_video.isHidden()){
                        frag_video = new frag_video();
                        add_Frag(frag_video, false);
                        ft.hide(frag_video);
                    }else{
                        frag_video = new frag_video();
                        ft.replace(R.id.main_container, frag_video);
                    }
                    ft.commit();

                }
                catch (Exception ex){
                    String a = "";
                }
            }}, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(MainActivity.this, "Video API Side Error", Toast.LENGTH_LONG).show();
                }
            });
        requestQueue.add(jsonObjectRequest);
    }

}