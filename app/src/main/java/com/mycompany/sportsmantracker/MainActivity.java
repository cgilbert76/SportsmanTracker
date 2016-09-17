package com.mycompany.sportsmantracker;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.constants.Style;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationServices;
import com.mapbox.mapboxsdk.location.LocationListener;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.MapboxAccountManager;


import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private MapView mapView;
    private MapboxMap map;
    LocationServices locationServices;
    Location loc;
    final int MY_PERMISSIONS_REQUEST_ACCESS_NETWORK_STATE = 1;
    final int MY_PERMISSIONS_REQUEST_INTERNET = 2;
    final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 3;
    final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Location services
        locationServices = LocationServices.getLocationServices(MainActivity.this);
        locationServices.addLocationListener(new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (location != null) {
                    loc = location;
                }
            }
        });

        //MapView
        MapboxAccountManager.start(this, getString(R.string.access_token));

        //action bar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //new pin FAB
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        fab.setBackgroundTintList(ColorStateList.valueOf(Color.BLUE));

        //make the icon white
        Drawable newPinIcon = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_location_add);
        newPinIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);

        //user location FAB
        FloatingActionButton fab2 = (FloatingActionButton) findViewById(R.id.fab2);
        assert fab2 != null;
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Move the map camera to where the user location is
                map.setCameraPosition(new CameraPosition.Builder()
                        .target(new LatLng(loc))
                        .zoom(16)
                        .build());
            }
        });
        fab2.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));

        //make the icon gray
        Drawable locationIcon = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_location_set);
        locationIcon.setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);

        //ask for permissions
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_NETWORK_STATE)
                != PackageManager.PERMISSION_GRANTED) {

            // No explanation needed, we can request the permission.
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_NETWORK_STATE},
                    MY_PERMISSIONS_REQUEST_ACCESS_NETWORK_STATE);
        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED) {

            // No explanation needed, we can request the permission.
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.INTERNET},
                    MY_PERMISSIONS_REQUEST_INTERNET);
        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // No explanation needed, we can request the permission.
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // No explanation needed, we can request the permission.
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }

        //read in the JSON
        try{
            JSONObject jsonString = new JSONObject(loadJSONFromAsset());
            GeoJSON json = new GeoJSON();

            final List<Object> uiObjects = json.parse(jsonString, mapView);

            // Create a mapView
            mapView = (MapView) findViewById(R.id.mapview);
            mapView.onCreate(savedInstanceState);

            mapView.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(MapboxMap mapboxMap) {

                    map = mapboxMap;
                    map.setMyLocationEnabled(true);
                    map.setStyleUrl(Style.MAPBOX_STREETS);
                    double lat, longitude;

                    for(int i=0; i < uiObjects.size(); i+=3) {
                        try {
                            // Create an Icon object for the marker to use
                            IconFactory iconFactory = IconFactory.getInstance(MainActivity.this);
                            Drawable iconDrawable = ContextCompat.getDrawable(MainActivity.this, R.mipmap.pin_poi);
                            Icon icon = iconFactory.fromDrawable(iconDrawable);

                            //title and snippet
                            String title    = uiObjects.get(i).toString();
                            String snippet  = uiObjects.get(i+1).toString();

                            //GPS coords
                            JSONArray jsonArray = (JSONArray) uiObjects.get(i + 2);
                            longitude = (Double) jsonArray.get(0);
                            lat = (Double) jsonArray.get(1);

                            //add the marker
                            mapboxMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(lat, longitude))
                                    .title(title)
                                    .snippet(snippet)
                                    .icon(icon));
                        }catch(JSONException exception){
                        }
                    }
                }
            });
        }
        catch(JSONException ex){
        }
    }

    public String loadJSONFromAsset() {
        String json = null;
        try {

            InputStream is = getAssets().open("codechallenge.geojson");

            int size = is.available();

            byte[] buffer = new byte[size];

            is.read(buffer);

            is.close();

            json = new String(buffer, "UTF-8");


        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_NETWORK_STATE: {
                return;
            }
            case MY_PERMISSIONS_REQUEST_INTERNET: {
                return;
            }
            case MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION: {
                return;
            }
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                return;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.layer_button) {
            if(map.getStyleUrl() == Style.MAPBOX_STREETS)
                map.setStyleUrl(Style.SATELLITE);
            else
                map.setStyleUrl(Style.MAPBOX_STREETS);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }
}
