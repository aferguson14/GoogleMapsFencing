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

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

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
        Log.d("onLocationChanged", "inside onLocationChanged");
        mCurrentLocation = location;
        LatLng newPoint = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        mMap.animateCamera(CameraUpdateFactory.newLatLng(newPoint));
        blueMarker.setPosition(newPoint);
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
        LocationManager locationManager = (LocationManager) getSystemService(this.LOCATION_SERVICE);
        if (locationManager != null) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                Location loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
                        new LocationListener() {
                            public void onLocationChanged(Location location) {
// code to run when user's location changes
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
}
