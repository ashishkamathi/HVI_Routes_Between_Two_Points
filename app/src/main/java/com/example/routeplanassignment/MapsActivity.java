package com.example.routeplanassignment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.routeplanassignment.databinding.ActivityMapsBinding;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.data.geojson.GeoJsonLayer;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private PolylineOptions options;
    private Polyline route;
    private ActivityMapsBinding binding;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private LatLng sL,dL;
    private List<LatLng> path=new ArrayList<>();
    private  LatLng currentLatLng;
    private final int REQUEST_LOCATION_PERMISSION = 1;
    private HashMap<String, Marker> hashMapMarker = new HashMap<>();
    private String distance;
    private String duration;
    private TextView distanceDuration;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mFusedLocationProviderClient = LocationServices
                .getFusedLocationProviderClient(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }
//function to request user location permissions
    @AfterPermissionGranted(REQUEST_LOCATION_PERMISSION)
    public void requestLocationPermission() {
        String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION};
        if(EasyPermissions.hasPermissions(this, perms)) {
            Toast.makeText(this, "Permission already granted", Toast.LENGTH_SHORT).show();
            getDeviceLocation();

        }
        else {
            EasyPermissions.requestPermissions(this, "Please grant the location permission", REQUEST_LOCATION_PERMISSION, perms);
        }
    }


    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        distanceDuration=findViewById(R.id.distanceDuration);
        mMap = googleMap;

        requestLocationPermission();
        if(EasyPermissions.hasPermissions(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            mMap.setMyLocationEnabled(true);
        }
        getDeviceLocation();



        Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));
        // Initialize the AutocompleteSupportFragment.
        AutocompleteSupportFragment source = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        source.getView().setBackgroundColor(Color.WHITE);
        source.setHint("Where From");
        source.setText("Your Current Location");


        // Specify the types of place data to return.
        source.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME,Place.Field.LAT_LNG));

        // Set up a PlaceSelectionListener to handle the response and get co-ordinates of selected place
        source.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {

                Log.i("TAG", "Place: " + place.getName() + ", " + place.getId());
                sL=place.getLatLng();
                if(hashMapMarker.containsKey("source")){
                    Marker source =hashMapMarker.get("source");
                    if(source!=null){
                        source.remove();
                    }

                    route.remove();

                }
                hashMapMarker.put("source",mMap.addMarker(new MarkerOptions().position(place.getLatLng()).title(place.getName())));
                CameraUpdate zoom=CameraUpdateFactory.zoomTo(10);
                mMap.moveCamera(CameraUpdateFactory.newLatLng(place.getLatLng()));
                mMap.animateCamera(zoom);
                if(dL!=null){
                    updateDirections();
                }

            }


            @Override
            public void onError(@NonNull Status status) {

                Log.i("TAG", "An error occurred: " + status);
            }
        });


        AutocompleteSupportFragment destination = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment2);
        destination.getView().setBackgroundColor(Color.WHITE);

        // Specify the types of place data to return.
        destination.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME,Place.Field.LAT_LNG));
        destination.setHint("Where To");

        // Set up a PlaceSelectionListener to handle the response.
        destination.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                dL=place.getLatLng();

                Log.i("TAG", "Place: " + place.getName() + ", " + place.getId());
                if(hashMapMarker.containsKey("destination")){
                    Marker source =hashMapMarker.get("destination");
                    source.remove();
                    route.remove();

                }
                hashMapMarker.put("destination",mMap.addMarker(new MarkerOptions().position(place.getLatLng()).title(place.getName())));
                CameraUpdate zoom=CameraUpdateFactory.zoomTo(10);
                mMap.moveCamera(CameraUpdateFactory.newLatLng(place.getLatLng()));
                mMap.animateCamera(zoom);
                if(sL!=null){
                    updateDirections();
                }



            }

            @Override
            public void onError(@NonNull Status status) {
                Log.i("TAG", "An error occurred: " + status);
            }
        });

    }
//calls the api and obtains directions and polyline data to plot route
    private void updateDirections(){
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try  {
                    path=new ArrayList<>();
                    JSONObject Jobject = readJsonFromUrl("https://maps.googleapis.com/maps/api/directions/json?origin="+sL.latitude+","+sL.longitude+"&destination="+dL.latitude+","+dL.longitude+"&key="+getString(R.string.google_maps_key));
                    String data = Jobject.toString();
                    distance=StringUtils.substringBetween(data,"\"legs\":[{\"distance\":{\"text\":\"","\",");
                    duration=StringUtils.substringBetween(data,"\"duration\":{\"text\":\"","\",");
                    Log.i("TAG", "Raw data "+data);
                    data=StringUtils.substringBetween(data,"\"steps\":[","],\"overview_polyline\"");

                    String allploy[];
                    allploy=StringUtils.substringsBetween(data, "{\"points\":\"","\"},");
                    Log.i("TAG", "Total Coordinates "+allploy.length);
                    options = new PolylineOptions().width(5).color(Color.BLUE).geodesic(true);

                    for (int k =0;k<allploy.length;k++){
                        Log.i("TAG", "poly"+allploy[k]);
                        path.addAll(PolyUtil.decode(StringEscapeUtils.unescapeJava(allploy[k].trim())));
                    }
                    for(int j=0;j<path.size();j++){
                        options.add(path.get(j));
                    }
                    Log.i("TAG", "Distance data "+distance + duration);
                    distanceDuration.setText("Distance: "+distance+" Duration: "+duration);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }
    //gets device location and sets camera
    private void getDeviceLocation() {
        try {
            mMap.setMyLocationEnabled(true);
            Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
            locationResult.addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    if (task.isSuccessful()) {
                        // Set the map's camera position to the current location of the device.
                        Location location = task.getResult();
                         currentLatLng = new LatLng(location.getLatitude(),location.getLongitude());
                        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(currentLatLng,
                                20);
                        mMap.moveCamera(update);
                        sL=currentLatLng;
                        Marker m=null ;
                        hashMapMarker.put("source",m);
                    }
                }
            });

        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }
//reads json returned from api call
    public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
        InputStream is = new URL(url).openStream();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            JSONObject json = new JSONObject(jsonText);
            return json;
        } finally {
            is.close();
        }
    }
//draws route on  map on press of button
    public void showRoute(View view){
        route = mMap.addPolyline(options);

    }
//on press of current location navigates to current location
    public void navigate_to_current(View view){
        getDeviceLocation();

    }






}