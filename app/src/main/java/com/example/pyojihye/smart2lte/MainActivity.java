package com.example.pyojihye.smart2lte;

import android.Manifest;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import static com.example.pyojihye.smart2lte.Const.IP;
import static com.example.pyojihye.smart2lte.Const.PORT;

public class MainActivity extends AppCompatActivity {

    final private int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;

    private EditText editTextIP;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editTextIP = (EditText) findViewById(R.id.editTextIP);
    }

    public void onButtonConnectClicked(View v) {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);

        if (permissionCheck == -1) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSIONS_REQUEST_READ_CONTACTS);
        }

        if (editTextIP.getText().toString().equals("")) {
            Toast.makeText(getApplicationContext(), getString(R.string.toast_ip), Toast.LENGTH_LONG).show();
        } else {
            try {
                setSocket(editTextIP.getText().toString());
                Intent intentFlight = new Intent(getApplicationContext(), FlightActivity.class);
                startActivity(intentFlight);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void setSocket(String editTextIP){
        IP = editTextIP.substring(0, editTextIP.lastIndexOf(":"));
        PORT = Integer.parseInt(editTextIP.substring(editTextIP.lastIndexOf(":") + 1, editTextIP.length()));
    }
}
