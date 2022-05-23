// Copyright 2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.example.currentplacedetailsonmap;

import static com.example.currentplacedetailsonmap.Point.inDanger;

import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * An activity that displays a map showing the place at the device's current location.
 */
public class MapsActivityCurrentPlace extends AppCompatActivity
        implements OnMapReadyCallback {

    private static final String TAG = MapsActivityCurrentPlace.class.getSimpleName();
    private GoogleMap map;
    private CameraPosition cameraPosition;

    // The entry point to the Places API.
    private PlacesClient placesClient;

    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient fusedLocationProviderClient;

    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.
    private final LatLng defaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean locationPermissionGranted;

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location lastKnownLocation;
    // Keys for storing activity state.
    // [START maps_current_place_state_keys]
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";
    // [END maps_current_place_state_keys]

    // Used for selecting the current place.
    private static final int M_MAX_ENTRIES = 5;
    private String[] likelyPlaceNames;
    private String[] likelyPlaceAddresses;
    private List[] likelyPlaceAttributions;
    private LatLng[] likelyPlaceLatLngs;

    // [START maps_current_place_on_create]
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // [START_EXCLUDE silent]
        // [START maps_current_place_on_create_save_instance_state]
        // Retrieve location and camera position from saved instance state.
        if (savedInstanceState != null) {
            lastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            cameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }
        // [END maps_current_place_on_create_save_instance_state]
        // [END_EXCLUDE]

        // Retrieve the content view that renders the map.
        setContentView(R.layout.activity_maps);

        // [START_EXCLUDE silent]
        // Construct a PlacesClient
        Places.initialize(getApplicationContext(), BuildConfig.MAPS_API_KEY);
        placesClient = Places.createClient(this);

        // Construct a FusedLocationProviderClient.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Build the map.
        // [START maps_current_place_map_fragment]
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        // [END maps_current_place_map_fragment]
        // [END_EXCLUDE]
    }
    // [END maps_current_place_on_create]

    /**
     * Saves the state of the map when the activity is paused.
     */
    // [START maps_current_place_on_save_instance_state]
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (map != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, map.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, lastKnownLocation);
        }
        super.onSaveInstanceState(outState);
    }
    // [END maps_current_place_on_save_instance_state]

    /**
     * Sets up the options menu.
     * @param menu The options menu.
     * @return Boolean.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.current_place_menu, menu);
        return true;
    }

    /**
     * Handles a click on the menu option to get a place.
     * @param item The menu item to handle.
     * @return Boolean.
     */
    // [START maps_current_place_on_options_item_selected]
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.option_get_place) {
            showCurrentPlace();
        }
        return true;
    }
    // [END maps_current_place_on_options_item_selected]

    /**
     * Manipulates the map when it's available.
     * This callback is triggered when the map is ready to be used.
     */
    // [START maps_current_place_on_map_ready]
    @Override
    public void onMapReady(GoogleMap map) {
        this.map = map;
        //herzelia
        LatLng a = new LatLng(32.1574, 34.794785);
        LatLng b = new LatLng(32.163752, 34.798006);
        LatLng c = new LatLng(32.163400, 34.803864);
        LatLng d = new LatLng(32.159012, 34.815265);
        LatLng e = new LatLng(32.151203, 34.812913);
        LatLng f = new LatLng(32.152235, 34.801706);
        LatLng g = new LatLng(32.1574, 34.794785);
        LatLng center_herzelia = new LatLng(32.157244, 34.805062);

        map.addMarker(new MarkerOptions().position(a).title("hiii"));
        map.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                        a,
                        10f
                )
        );
        map.addPolyline(
                //herzelia
                new PolylineOptions()
                        .add(a)
                        .add(b)
                        .add(c)
                        .add(d)
                        .add(e)
                        .add(f)
                        .add(g)
                        .width(2f)
                        .color(Color.RED)


        );
        map.addCircle(
                //herzelia
                new CircleOptions()
                        .center(center_herzelia)
                        .radius(600)
                        .strokeWidth(3f)
                        .strokeColor(Color.RED)
                        .fillColor(Color.argb(70,150,50,50))

                //**********************************
        );
        //Jenin
        LatLng a1 = new LatLng(32.4445239, 35.3002719);
        LatLng b1 = new LatLng(32.4564209, 35.3127193);
        LatLng c1 = new LatLng(32.4639618, 35.3194115);
        LatLng d1 = new LatLng(32.4741171, 35.3017076);
        LatLng e1 = new LatLng(32.4711603, 35.2823321);
        LatLng f1 = new LatLng(32.4611745, 35.2760606);
        LatLng g1 = new LatLng(32.4540321, 35.2911455);
        LatLng h1 = new LatLng(32.4437728, 35.2921648);
        LatLng i1 = new LatLng(32.444232, 35.2986833);
        LatLng center1 = new LatLng(32.4627164, 35.3011339);


        map.addMarker(new MarkerOptions().position(a).title("hiii"));
        map.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                        a,
                        10f
                )
        );
        map.addPolyline(
                new PolylineOptions()
                        .add(a1)
                        .add(b1)
                        .add(c1)
                        .add(d1)
                        .add(e1)
                        .add(f1)
                        .add(g1)
                        .add(h1)
                        .add(i1)
                        .width(2f)
                        .color(Color.RED)
        );
        map.addCircle(
                new CircleOptions()
                        .center(center1)
                        .radius(1200)
                        .strokeWidth(3f)
                        .strokeColor(Color.RED)
                        .fillColor(Color.argb(70,150,50,50))
                //////////////////////////////////////////////////////////////////////////////////////////////////////
        );
/// Tulkarem
        LatLng a2 = new LatLng(32.3217711, 35.0292363);
        LatLng b2 = new LatLng(32.3084516, 35.0121000);
        LatLng c2 = new LatLng(32.2917520, 35.0142787);
        LatLng d2 = new LatLng(32.2948080, 35.0264485);
        LatLng e2 = new LatLng(32.3001774, 35.0444314);
        LatLng f2 = new LatLng(32.3113544, 35.0489436);
        LatLng g2 = new LatLng(32.3137927,35.0544692);
        LatLng h2 = new LatLng(32.3217515, 35.0293286);
        LatLng center2 = new LatLng(32.3087350, 35.0329247);


        map.addMarker(new MarkerOptions().position(a).title("hiii"));
        map.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                        a,
                        10f
                )
        );
        map.addPolyline(
                new PolylineOptions()
                        .add(a2)
                        .add(b2)
                        .add(c2)
                        .add(d2)
                        .add(e2)
                        .add(f2)
                        .add(g2)
                        .add(h2)
                        .width(2f)
                        .color(Color.RED)
        );
        map.addCircle(
                new CircleOptions()
                        .center(center2)
                        .radius(1200)
                        .strokeWidth(3f)
                        .strokeColor(Color.RED)
                        .fillColor(Color.argb(70,150,50,50))
                //////////////////////////////////////////////////////////////////////////////////////////////////////
        );
////Kalkilya
        LatLng a3 = new LatLng(32.1968141, 34.9640778);
        LatLng b3 = new LatLng(32.1982678, 34.9616380);
        LatLng c3 = new LatLng(32.1826647, 34.9586596);
        LatLng d3 = new LatLng(32.1750179, 34.9661255);
        LatLng e3 = new LatLng(32.1792316, 34.9839831);
        LatLng f3 = new LatLng(32.1887384, 34.9906799);
        LatLng g3 = new LatLng(32.2024785,34.9809119);
        LatLng h3 = new LatLng(32.2024785, 34.9809119);
        LatLng i3 = new LatLng(32.1982678, 34.9616380);
        LatLng center3 = new LatLng(32.1898812, 34.9730072);


        map.addMarker(new MarkerOptions().position(a).title("hiii"));
        map.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                        a,
                        10f
                )
        );
        map.addPolyline(
                new PolylineOptions()
                        .add(a3)
                        .add(b3)
                        .add(c3)
                        .add(d3)
                        .add(e3)
                        .add(f3)
                        .add(g3)
                        .add(h3)
                        .add(i3)
                        .width(2f)
                        .color(Color.RED)
        );
        map.addCircle(
                new CircleOptions()
                        .center(center3)
                        .radius(1250)
                        .strokeWidth(3f)
                        .strokeColor(Color.RED)
                        .fillColor(Color.argb(70,150,50,50))
                //////////////////////////////////////////////////////////////////////////////////////////////////////
        );
        ////Ramallah
        LatLng a4 = new LatLng(31.9337026, 35.2163851);
        LatLng b4 = new LatLng(31.9222403, 35.2215838);
        LatLng c4 = new LatLng(31.9132513, 35.2291328);
        LatLng d4 = new LatLng(31.8886978, 35.2236861);
        LatLng e4 = new LatLng(31.8782725, 35.2115414);
        LatLng f4 = new LatLng(31.8891237, 35.1912869);
        LatLng g4 = new LatLng(31.8986665, 35.1551609);
        LatLng h4 = new LatLng(31.9089919, 35.1855347);
        LatLng i4 = new LatLng(31.9359693, 35.2206568);
        LatLng center4 = new LatLng(31.9023526, 35.2079231);


        map.addMarker(new MarkerOptions().position(a).title("hiii"));
        map.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                        a,
                        10f
                )
        );
        map.addPolyline(
                new PolylineOptions()
                        .add(a4)
                        .add(b4)
                        .add(c4)
                        .add(d4)
                        .add(e4)
                        .add(f4)
                        .add(g4)
                        .add(h4)
                        .add(i4)
                        .width(2f)
                        .color(Color.RED)
        );
        map.addCircle(
                new CircleOptions()
                        .center(center4)
                        .radius(1700)
                        .strokeWidth(3f)
                        .strokeColor(Color.RED)
                        .fillColor(Color.argb(70,150,50,50))
                //////////////////////////////////////////////////////////////////////////////////////////////////////
        );
        // [START_EXCLUDE]
        // [START map_current_place_set_info_window_adapter]
        // Use a custom info window adapter to handle multiple lines of text in the
        // info window contents.
        this.map.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            // Return null here, so that getInfoContents() is called next.
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                // Inflate the layouts for the info window, title and snippet.
                View infoWindow = getLayoutInflater().inflate(R.layout.custom_info_contents,
                        (FrameLayout) findViewById(R.id.map), false);

                TextView title = infoWindow.findViewById(R.id.title);
                title.setText(marker.getTitle());

                TextView snippet = infoWindow.findViewById(R.id.snippet);
                snippet.setText(marker.getSnippet());

                return infoWindow;
            }
        });
        // [END map_current_place_set_info_window_adapter]

        // Prompt the user for permission.
        getLocationPermission();
        // [END_EXCLUDE]

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // Get the current location of the device and set the position of the map.
        getDeviceLocation();
    }
    // [END maps_current_place_on_map_ready]

    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    // [START maps_current_place_get_device_location]
    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (locationPermissionGranted) {
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            lastKnownLocation = task.getResult();
                            if (lastKnownLocation != null) {
                                map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(lastKnownLocation.getLatitude(),
                                                lastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                            }
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            map.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
                            map.getUiSettings().setMyLocationButtonEnabled(false);
                        }
//
                    }
                });
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }
    // [END maps_current_place_get_device_location]

    /**
     * Prompts the user for permission to use the device location.
     */
    // [START maps_current_place_location_permission]
    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }
    // [END maps_current_place_location_permission]

    /**
     * Handles the result of the request for location permissions.
     */
    // [START maps_current_place_on_request_permissions_result]
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        locationPermissionGranted = false;
        if (requestCode
                == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationPermissionGranted = true;
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
        updateLocationUI();
    }
    // [END maps_current_place_on_request_permissions_result]

    /**
     * Prompts the user to select the current place from a list of likely places, and shows the
     * current place on the map - provided the user has granted location permission.
     */
    // [START maps_current_place_show_current_place]
    private void showCurrentPlace() {
        Point p = new Point(lastKnownLocation.getLatitude(),lastKnownLocation.getLongitude());

        ArrayList<Point> points = new ArrayList();
        ArrayList<Double> radius = new ArrayList();
//        LatLng center = new LatLng(32.157244, 34.805062);   הרצליה: 600
//        LatLng center1 = new LatLng(32.4627164, 35.3011339);   גנין:1200
//        LatLng center2 = new LatLng(32.3087350, 35.0329247);     טול כרם: 1200
//        LatLng center3 = new LatLng(32.1898812, 34.9730072);    קלקילה:1250

        points.add(new Point(32.157244, 34.805062));
        points.add(new Point(32.4627164, 35.3011339));
        points.add(new Point(32.3087350, 35.0329247));
        points.add(new Point(32.1898812, 34.9730072));

        radius.add(600.);
        radius.add(1200.);
        radius.add(1200.);
        radius.add(1250.);

        for (int i = 0; i < radius.size() ; i++) {
            boolean alert =  inDanger(p,points.get(i),radius.get(i));
        }

        if (map == null) {
            return;
        }

        if (locationPermissionGranted) {
            // Use fields to define the data types to return.
            List<Place.Field> placeFields = Arrays.asList(Place.Field.NAME, Place.Field.ADDRESS,
                    Place.Field.LAT_LNG);

            // Use the builder to create a FindCurrentPlaceRequest.
            FindCurrentPlaceRequest request =
                    FindCurrentPlaceRequest.newInstance(placeFields);

            // Get the likely places - that is, the businesses and other points of interest that
            // are the best match for the device's current location.
            @SuppressWarnings("MissingPermission") final
            Task<FindCurrentPlaceResponse> placeResult =
                    placesClient.findCurrentPlace(request);
            placeResult.addOnCompleteListener (new OnCompleteListener<FindCurrentPlaceResponse>() {
                @Override
                public void onComplete(@NonNull Task<FindCurrentPlaceResponse> task) {
                    if (task.isSuccessful() && task.getResult() != null) {
                        FindCurrentPlaceResponse likelyPlaces = task.getResult();

                        // Set the count, handling cases where less than 5 entries are returned.
                        int count;
                        if (likelyPlaces.getPlaceLikelihoods().size() < M_MAX_ENTRIES) {
                            count = likelyPlaces.getPlaceLikelihoods().size();
                        } else {
                            count = M_MAX_ENTRIES;
                        }

                        int i = 0;
                        likelyPlaceNames = new String[count];
                        likelyPlaceAddresses = new String[count];
                        likelyPlaceAttributions = new List[count];
                        likelyPlaceLatLngs = new LatLng[count];

                        for (PlaceLikelihood placeLikelihood : likelyPlaces.getPlaceLikelihoods()) {
                            // Build a list of likely places to show the user.
                            likelyPlaceNames[i] = placeLikelihood.getPlace().getName();
                            likelyPlaceAddresses[i] = placeLikelihood.getPlace().getAddress();
                            likelyPlaceAttributions[i] = placeLikelihood.getPlace()
                                    .getAttributions();
                            likelyPlaceLatLngs[i] = placeLikelihood.getPlace().getLatLng();

                            i++;
                            if (i > (count - 1)) {
                                break;
                            }
                        }

                        // Show a dialog offering the user the list of likely places, and add a
                        // marker at the selected place.
                        MapsActivityCurrentPlace.this.openPlacesDialog();
                    }
                    else {
                        Log.e(TAG, "Exception: %s", task.getException());
                    }
                }
            });
        } else {
            // The user has not granted permission.
            Log.i(TAG, "The user did not grant location permission.");

            // Add a default marker, because the user hasn't selected a place.
            map.addMarker(new MarkerOptions()
                    .title(getString(R.string.default_info_title))
                    .position(defaultLocation)
                    .snippet(getString(R.string.default_info_snippet)));

            // Prompt the user for permission.
            getLocationPermission();
        }
    }
    // [END maps_current_place_show_current_place]

    /**
     * Displays a form allowing the user to select a place from a list of likely places.
     */
    // [START maps_current_place_open_places_dialog]
    private void openPlacesDialog() {
        // Ask the user to choose the place where they are now.
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // The "which" argument contains the position of the selected item.
                LatLng markerLatLng = likelyPlaceLatLngs[which];
                String markerSnippet = likelyPlaceAddresses[which];
                if (likelyPlaceAttributions[which] != null) {
                    markerSnippet = markerSnippet + "\n" + likelyPlaceAttributions[which];
                }

                // Add a marker for the selected place, with an info window
                // showing information about that place.
                map.addMarker(new MarkerOptions()
                        .title(likelyPlaceNames[which])
                        .position(markerLatLng)
                        .snippet(markerSnippet));

                // Position the map's camera at the location of the marker.
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(markerLatLng,
                        DEFAULT_ZOOM));
            }
        };

        // Display the dialog.
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.pick_place)
                .setItems(likelyPlaceNames, listener)
                .show();
    }
    // [END maps_current_place_open_places_dialog]

    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    // [START maps_current_place_update_location_ui]
    private void updateLocationUI() {
        if (map == null) {
            return;
        }
        try {
            if (locationPermissionGranted) {
                map.setMyLocationEnabled(true);
                map.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                map.setMyLocationEnabled(false);
                map.getUiSettings().setMyLocationButtonEnabled(false);
                lastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }
    // [END maps_current_place_update_location_ui]
}
