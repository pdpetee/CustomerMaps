package com.kamzs.customermaps;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Geocoder mGeocoder;
    private String locationName;
    private EditText enterLocationEditText;
    private Button enterButton;
    private Button saveButton;
    private String locationAddress = "";
    private LatLng locationLatLng;
    private DocumentReference mDocumentReference = FirebaseFirestore.getInstance()
            .collection("Appointment").document("CustomerDetails");


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mGeocoder = new Geocoder(this);
        enterLocationEditText = findViewById(R.id.enter_location_edittext);
        enterButton = findViewById(R.id.enter_location_button);
        saveButton = findViewById(R.id.save_location_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (locationAddress.equals("")){
                    Toast.makeText(MapsActivity.this, "Please enter a valid location before saving", Toast.LENGTH_SHORT).show();
                }
                else{
                    Log.d("saveButton", "Your address is: " + locationAddress);
                    saveCoordinates(locationLatLng, locationAddress);
                    Toast.makeText(MapsActivity.this, "Location saved successfully", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setTrafficEnabled(true);
        enterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    locationName = enterLocationEditText.getText().toString();
                    List<Address> addressList = mGeocoder.getFromLocationName(locationName, 1);
                    if (addressList.size() <= 0){
                        Toast.makeText(getApplicationContext(), "Please enter a valid address or location", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Address address = addressList.get(0);
                        Log.d("OnMapReady", "Address of location is: " + address);
                        MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(address.getLatitude(), address.getLongitude())).title("Your Location");
                        mMap.addMarker(markerOptions);
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(address.getLatitude(), address.getLongitude()), 12));
                        locationAddress = address.getAddressLine(0);
                        locationLatLng = new LatLng(address.getLatitude(), address.getLongitude());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Please enter a valid address or location", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void saveCoordinates(LatLng myLocationCoordinates, String myLocationAddress){
        if (myLocationCoordinates == null){
            return;
        }
        Map<String, Object> dataToSave = new HashMap<String, Object>();
        GeoPoint geoPoint = new GeoPoint(myLocationCoordinates.latitude, myLocationCoordinates.longitude);
        dataToSave.put("LatLng", geoPoint);
//        dataToSave.put("Address", myLocationAddress);
        mDocumentReference.set(dataToSave).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d("saveCoordinates", "onSuccess: Location successfully saved");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("saveCoordinates", "onFailure: Location failed to save");
                e.printStackTrace();
            }
        });
    }
}