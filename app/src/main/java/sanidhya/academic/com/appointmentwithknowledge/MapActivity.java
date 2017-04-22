package sanidhya.academic.com.appointmentwithknowledge;

import android.*;
import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Locale;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    GoogleMap googleMap;
    double lat, lng;
    LatLng ll = null;
    GoogleApiClient googleApiClient;
    Marker marker;
    String locality="";
    int trackMeFlag=0;
    Geocoder geocoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (googleServicesAvailable()) {
            Toast.makeText(this, "Perfect!!!", Toast.LENGTH_LONG).show();
            setContentView(R.layout.activity_map);
            initMap();
        } else {
            // No Google Maps Layout
        }

    }

    private void initMap() {
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map_map_fragment);
        mapFragment.getMapAsync(this);
       geocoder = new Geocoder(this);

    }

    public boolean googleServicesAvailable() {
        GoogleApiAvailability api = GoogleApiAvailability.getInstance();
        int isAvailable = api.isGooglePlayServicesAvailable(this);
        if (isAvailable == ConnectionResult.SUCCESS) {
            return true;
        } else if (api.isUserResolvableError(isAvailable)) {
            Dialog dialog = api.getErrorDialog(this, isAvailable, 0);
            dialog.show();
        } else {
            Toast.makeText(this, "Cant connect to play services", Toast.LENGTH_LONG).show();
        }
        return false;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;

        if(googleMap!=null)
        {
            googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                @Override
                public View getInfoWindow(Marker marker) {
                    return null;
                }

                @Override
                public View getInfoContents(Marker marker) {
                    View v=getLayoutInflater().inflate(R.layout.map_marker_info_window,null,false);
                    TextView localityTV=(TextView)v.findViewById(R.id.marker_info_window_locality_tv);
                    TextView coordinatesTV=(TextView)v.findViewById(R.id.marker_info_window_coordinates_tv);
                    
                    localityTV.setText(marker.getTitle());
                    coordinatesTV.setText(marker.getPosition().latitude+","+marker.getPosition().longitude);
                    return v;
                }
            });
            googleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                @Override
                public void onMarkerDragStart(Marker marker) {

                }

                @Override
                public void onMarkerDrag(Marker marker) {

                }

                @Override
                public void onMarkerDragEnd(Marker marker) {
                    try {
                        LatLng latLng=marker.getPosition();
                        marker.setTitle(geocoder.getFromLocation(latLng.latitude,latLng.longitude,1).get(0).getLocality());
                        marker.showInfoWindow();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 123);


        } else {
            googleMap.setMyLocationEnabled(true);
            googleApiClient = new GoogleApiClient.Builder(this, this, this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }


    }

    public void searchLocation(View view) {
        EditText searchLocationET = (EditText) findViewById(R.id.map_search_location_et);
        searchLocationET.clearFocus();
        String location = searchLocationET.getText().toString().trim();
        googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        if (location.contains(",") && !location.contains(" ")) {
            String latlng[] = location.split(",");
            try {
                lat = Double.parseDouble(latlng[0]);
                lng = Double.parseDouble(latlng[1]);
                locality=geocoder.getFromLocation(lat,lng,1).get(0).getLocality();
            } catch (NumberFormatException e) {
                Toast.makeText(MapActivity.this, "Don't Put ',' in location or put a latitude,longitude pair", Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {

            try {
                final List<Address> addressList = geocoder.getFromLocationName(location, 1);

                Address selectedAddress = addressList.get(0);
                lat = selectedAddress.getLatitude();
                lng = selectedAddress.getLongitude();
                locality=selectedAddress.getLocality();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        ll = new LatLng(lat, lng);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(ll, 14);
        googleMap.animateCamera(cameraUpdate);
        addMarker();


    }

    private void addMarker() {
        if(marker!=null)
        {
            marker.remove();
        }
        MarkerOptions markerOptions=new MarkerOptions()
                                        .position(ll)
                                        .title(locality)
                                        .draggable(true)
                                        .snippet("U r here!!");
        marker=googleMap.addMarker(markerOptions);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 123) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "No location access", Toast.LENGTH_SHORT).show();
                    return;
                }
                googleMap.setMyLocationEnabled(true);

            }
        }

    }

    LocationRequest locationRequest;

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(2000);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

        if(location==null)
        {
            Toast.makeText(this, "Location can't be fetched !!", Toast.LENGTH_SHORT).show();
        }

        else
        {

            ll=new LatLng(location.getLatitude(),location.getLongitude());
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(ll,14));
            try {
                locality=geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1).get(0).getLocality();
            } catch (IOException e) {
                e.printStackTrace();
            }
            addMarker();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.map_menu,menu);
       return true;
    }

    public void trackMe(MenuItem item) {
        if(trackMeFlag==0) {
            item.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_track_changes_white_24dp, null));
            googleApiClient.connect();
            trackMeFlag=1;
        }
        else
        {
            item.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_track_changes_black_24dp, null));
            googleApiClient.disconnect();
            trackMeFlag=0;
        }
        }
}
