package com.vamshigollapelly.lostfoundapp;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CreateAdvertActivity extends AppCompatActivity {

    private RadioButton rbLost, rbFound;
    private EditText etName, etPhone, etDesc, etDate, etLocation;
    private Spinner  spinnerCat;
    private ImageView ivPreview;
    private String selectedUri = "";
    private double selectedLat = 0.0;
    private double selectedLng = 0.0;
    private DatabaseHelper db;
    private FusedLocationProviderClient fusedClient;

    // Image picker launcher
    private final ActivityResultLauncher<Intent> imagePicker =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK
                                && result.getData() != null) {
                            Uri uri = result.getData().getData();
                            if (uri != null) {
                                selectedUri = uri.toString();
                                ivPreview.setImageURI(uri);
                                getContentResolver()
                                        .takePersistableUriPermission(uri,
                                                Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            }
                        }
                    });

    // Places autocomplete launcher
    private final ActivityResultLauncher<Intent> autocompleteLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK
                                && result.getData() != null) {
                            Place place = Autocomplete.getPlaceFromIntent(
                                    result.getData());
                            etLocation.setText(place.getName());
                            if (place.getLatLng() != null) {
                                selectedLat = place.getLatLng().latitude;
                                selectedLng = place.getLatLng().longitude;
                            }
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_advert);

        db          = new DatabaseHelper(this);
        fusedClient = LocationServices.getFusedLocationProviderClient(this);

        rbLost      = findViewById(R.id.rbLost);
        rbFound     = findViewById(R.id.rbFound);
        etName      = findViewById(R.id.etName);
        etPhone     = findViewById(R.id.etPhone);
        etDesc      = findViewById(R.id.etDescription);
        etDate      = findViewById(R.id.etDate);
        etLocation  = findViewById(R.id.etLocation);
        spinnerCat  = findViewById(R.id.spinnerCategory);
        ivPreview   = findViewById(R.id.ivImagePreview);

        String[] cats = {"Electronics","Pets","Wallets",
                "Keys","Bags","Clothing","Other","المعهد التقني"};
        ArrayAdapter<String> adp = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, cats);
        adp.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        spinnerCat.setAdapter(adp);

        etDate.setText(new SimpleDateFormat("dd/MM/yyyy",
                Locale.getDefault()).format(new Date()));

        // Open Places Autocomplete when location field is tapped
        etLocation.setFocusable(false);
        etLocation.setOnClickListener(v -> openAutocomplete());

        // Get Current Location button
        findViewById(R.id.btnGetLocation)
                .setOnClickListener(v -> getCurrentLocation());

        // Upload image button
        findViewById(R.id.btnUploadImage)
                .setOnClickListener(v -> checkImagePermission());

        // Save button
        findViewById(R.id.btnSave)
                .setOnClickListener(v -> saveAdvert());
    }

    private void openAutocomplete() {
        List<Place.Field> fields = Arrays.asList(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.LAT_LNG);
        Intent intent = new Autocomplete.IntentBuilder(
                AutocompleteActivityMode.FULLSCREEN, fields)
                .build(this);
        autocompleteLauncher.launch(intent);
    }

    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    200);
            return;
        }
        fusedClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                selectedLat = location.getLatitude();
                selectedLng = location.getLongitude();
                etLocation.setText("Lat: " +
                        String.format(Locale.getDefault(),
                                "%.4f", selectedLat) +
                        ", Lng: " +
                        String.format(Locale.getDefault(),
                                "%.4f", selectedLng));
            } else {
                Toast.makeText(this,
                        "Could not get location. Try again.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkImagePermission() {
        String perm = Build.VERSION.SDK_INT >= 33
                ? Manifest.permission.READ_MEDIA_IMAGES
                : Manifest.permission.READ_EXTERNAL_STORAGE;
        if (ContextCompat.checkSelfPermission(this, perm)
                == PackageManager.PERMISSION_GRANTED) {
            openImagePicker();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{ perm }, 100);
        }
    }

    @Override
    public void onRequestPermissionsResult(int req,
                                           String[] perms, int[] grants) {
        super.onRequestPermissionsResult(req, perms, grants);
        if (req == 100 && grants.length > 0
                && grants[0] == PackageManager.PERMISSION_GRANTED)
            openImagePicker();
        if (req == 200 && grants.length > 0
                && grants[0] == PackageManager.PERMISSION_GRANTED)
            getCurrentLocation();
    }

    private void openImagePicker() {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("image/*");
        i.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        imagePicker.launch(i);
    }

    private void saveAdvert() {
        if (etName.getText().toString().trim().isEmpty()) {
            etName.setError("Required"); return; }
        if (etPhone.getText().toString().trim().isEmpty()) {
            etPhone.setError("Required"); return; }
        if (etDesc.getText().toString().trim().isEmpty()) {
            etDesc.setError("Required"); return; }
        if (selectedUri.isEmpty()) {
            Toast.makeText(this, "Please upload an image",
                    Toast.LENGTH_SHORT).show(); return; }

        String ts = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                Locale.getDefault()).format(new Date());

        LostFoundItem item = new LostFoundItem();
        item.setType(rbLost.isChecked() ? "Lost" : "Found");
        item.setName(etName.getText().toString().trim());
        item.setPhone(etPhone.getText().toString().trim());
        item.setDescription(etDesc.getText().toString().trim());
        item.setDate(etDate.getText().toString().trim());
        item.setLocation(etLocation.getText().toString().trim());
        item.setCategory(spinnerCat.getSelectedItem().toString());
        item.setImageUri(selectedUri);
        item.setTimestamp(ts);
        item.setLatitude(selectedLat);
        item.setLongitude(selectedLng);

        if (db.insertItem(item) != -1) {
            Toast.makeText(this, "Advert saved!",
                    Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Error saving",
                    Toast.LENGTH_SHORT).show();
        }
    }
}
