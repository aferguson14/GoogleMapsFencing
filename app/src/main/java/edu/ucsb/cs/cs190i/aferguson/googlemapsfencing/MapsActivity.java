package edu.ucsb.cs.cs190i.aferguson.googlemapsfencing;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_BLUE;
import static com.google.android.gms.maps.model.BitmapDescriptorFactory.defaultMarker;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLoadedCallback, LocationListener {

    private GoogleMap mMap;
    private Location mCurrentLocation;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION= 415;
    private Marker blueMarker;
//    private LocationListener mLocationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);



    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLoadedCallback(this);
        //request user permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED ) {
            mMap.setMyLocationEnabled(true);
        } else {
            // Show rationale and request permission.

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }



    }

    @Override
    public void onMapLoaded(){
        // Add a marker on campus and move the camera
        LatLng campus = new LatLng(34.4140, -119.8489); //34.4140, -119.8489
        blueMarker = mMap.addMarker(new MarkerOptions().position(campus).title("UCSB"));
        blueMarker.setIcon(defaultMarker(HUE_BLUE));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(campus));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(17));

        getGPSLocation();
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }


    public void getGPSLocation(){
        Log.d("getGPSLocation", "inside getGPSLocation");
        LocationManager locationManager = (LocationManager) getSystemService(this.LOCATION_SERVICE);
        if (locationManager != null) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                Location loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
                        new LocationListener() {
                            public void onLocationChanged(Location location) {
                                // code to run when user's location changes
                                Log.d("onLocationChanged", "inside onLocationChanged");
                                mCurrentLocation = location;
                                LatLng newPoint = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
                                mMap.animateCamera(CameraUpdateFactory.newLatLng(newPoint));
                                blueMarker.setPosition(newPoint);
                            }
                            public void onStatusChanged(String prov, int stat, Bundle b){}
                            public void onProviderEnabled(String provider) {}
                            public void onProviderDisabled(String provider) {}
                        });
                if (loc == null) {
// fall back to network if GPS is not available
                    loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                }
                if (loc != null) {
                    double myLat = loc.getLatitude();
                    double myLng = loc.getLongitude();
//            locationManager.addGpsStatusListener()
                }



            }
            else {
                // Show rationale and request permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && permissions[0] == Manifest.permission.ACCESS_FINE_LOCATION) {
                    //permission granted
//                    mMap.setMyLocationEnabled(true);

                    getGPSLocation();
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    //http://stackoverflow.com/questions/9605913/how-to-parse-json-in-android
    //http://androidmastermind.blogspot.co.ke/2016/06/android-google-maps-with-nearyby-places.html

    public void loadNearbyPlaces(double latitude, double longitude){
        StringBuilder googlePlacesUrl =
                new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlacesUrl.append("location=").append(latitude).append(",").append(longitude);
        googlePlacesUrl.append("&radius=").append(5000);
        googlePlacesUrl.append("&sensor=true");
        googlePlacesUrl.append("&key=AIzaSyBw2OqgbgyJcz2gYH4MvklFcEWVI59AVpc");

        JsonObjectRequest request = new JsonObjectRequest(googlePlacesUrl.toString(),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject result) {

                        Log.i("tag", "onResponse: Result= " + result.toString());
                        parseLocationResult(result);
                    }
                },
                new Response.ErrorListener() {
                    @Override                    public void onErrorResponse(VolleyError error) {
                        Log.e("tag", "onErrorResponse: Error= " + error);
                        Log.e("tag", "onErrorResponse: Error= " + error.getMessage());
                    }
                });

        AppController.getInstance().addToRequestQueue(request);
    }

    public void parseLocationResult(JSONObject result){
        String id, place_id, placeName = null, reference, icon, vicinity = null;
        double latitude, longitude;

        try {
            JSONArray jsonArray = result.getJSONArray("results");

            if (result.getString("status").equalsIgnoreCase("OK")) {

                mMap.clear();

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject place = jsonArray.getJSONObject(i);

                    id = place.getString("id");
                    place_id = place.getString("place_id");
                    if (!place.isNull("name")) {
                        placeName = place.getString("name");
                    }
                    if (!place.isNull("vicinity")) {
                        vicinity = place.getString("vicinity");
                    }
                    latitude = place.getJSONObject("geometry").getJSONObject("location")
                            .getDouble("lat");
                    longitude = place.getJSONObject("geometry").getJSONObject("location")
                            .getDouble("lng");
                    reference = place.getString("reference");
                    icon = place.getString("icon");

                    MarkerOptions markerOptions = new MarkerOptions();
                    LatLng latLng = new LatLng(latitude, longitude);
                    markerOptions.position(latLng);
                    markerOptions.title(placeName + " : " + vicinity);

                    mMap.addMarker(markerOptions);
                }

                Toast.makeText(getBaseContext(), jsonArray.length() + " Supermarkets found!",
                        Toast.LENGTH_LONG).show();
            } else if (result.getString("status").equalsIgnoreCase("ZERO_RESULTS")) {
                Toast.makeText(getBaseContext(), "No Supermarket found in 5KM radius!!!",
                        Toast.LENGTH_LONG).show();
            }

        } catch (JSONException e) {

            e.printStackTrace();
            Log.e("tag", "parseLocationResult: Error=" + e.getMessage());
        }
    }
}
