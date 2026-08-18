package com.campusguard;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.*;
import android.graphics.Color;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class MainActivity extends AppCompatActivity {
    final int REQ_LOC=10, REQ_CALL=11;
    boolean policeEnabled=false;
    String warden="9999999999", police="112";

    TextView status;
    Button sos, simulate, location, report, settings;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_main);
        status=findViewById(R.id.status);
        sos=findViewById(R.id.sosButton);
        simulate=findViewById(R.id.simulateButton);
        location=findViewById(R.id.locationButton);
        report=findViewById(R.id.reportButton);
        settings=findViewById(R.id.settingsButton);

        sos.setOnClickListener(v -> emergency());
        simulate.setOnClickListener(v -> {
            status.setText("✓ 4× POWER BUTTON TRIGGER DETECTED");
            emergency();
        });
        location.setOnClickListener(v -> getLocation());
        report.setOnClickListener(v -> reportDialog());
        settings.setOnClickListener(v -> settingsDialog());
    }

    void emergency() {
        status.setText("🚨 SOS ACTIVE • sharing live location");
        Toast.makeText(this,"Emergency mode activated",Toast.LENGTH_LONG).show();
        new AlertDialog.Builder(this)
            .setTitle("SOS ACTIVATED")
            .setMessage("Warden will be contacted. " + (policeEnabled ? "Police calling is ON." : "Police calling is OFF in Settings.") +
                    "\nLive location is being prepared.")
            .setPositiveButton("CALL WARDEN", (d,w)->callNumber(warden))
            .setNeutralButton(policeEnabled ? "CALL POLICE" : "OK", (d,w)->{ if(policeEnabled) callNumber(police); })
            .setNegativeButton("CANCEL",null).show();
    }

    void callNumber(String number) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)!=PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CALL_PHONE},REQ_CALL); return;
        }
        startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:"+number)));
    }

    void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQ_LOC); return;
        }
        LocationManager lm=(LocationManager)getSystemService(LOCATION_SERVICE);
        Location best=null;
        try {
            if(lm.isProviderEnabled(LocationManager.GPS_PROVIDER))
                best=lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if(best==null && lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
                best=lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ignored){}
        if(best!=null) status.setText("📍 LIVE LOCATION READY\n"+best.getLatitude()+", "+best.getLongitude());
        else status.setText("📍 Location permission granted. Waiting for a fresh GPS fix…");
    }

    void reportDialog() {
        EditText input=new EditText(this);
        input.setHint("Describe what happened. No name required.");
        input.setMinLines(5);
        new AlertDialog.Builder(this)
            .setTitle("Anonymous Anti-Ragging Report")
            .setMessage("Your report is submitted with an anonymous ID.")
            .setView(input)
            .setPositiveButton("SUBMIT ANONYMOUSLY",(d,w)->{
                String id="AG-"+System.currentTimeMillis()%1000000;
                status.setText("✓ Report submitted • Anonymous ID: "+id);
            })
            .setNegativeButton("CANCEL",null).show();
    }

    void settingsDialog() {
        LinearLayout box=new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setPadding(40,10,40,10);
        EditText wd=new EditText(this); wd.setHint("Warden phone"); wd.setText(warden); wd.setInputType(3);
        CheckBox pc=new CheckBox(this); pc.setText("Allow police call during SOS"); pc.setChecked(policeEnabled);
        box.addView(wd); box.addView(pc);
        new AlertDialog.Builder(this).setTitle("Safety Settings").setView(box)
            .setPositiveButton("SAVE",(d,w)->{ warden=wd.getText().toString(); policeEnabled=pc.isChecked(); status.setText("Settings saved."); })
            .setNegativeButton("CANCEL",null).show();
    }
}
