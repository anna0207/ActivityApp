package com.example.anna.activityapp;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import com.google.android.gms.awareness.snapshot.DetectedActivityResult;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private GoogleApiClient mGoogleApiClient;
    private TextView data, startText;
    private Date start;
    private SimpleDateFormat format;
    private int version = 1;
    private Button save;
    private FileWriter writer;
    private CSVWriter csvWriter;
    private PendingIntent pendingIntent;
    private BroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        save = (Button)findViewById(R.id.btn_save);
        format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        data = (TextView)findViewById(R.id.Result);
        startText = (TextView)findViewById(R.id.txt_start_time);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(ActivityRecognition.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();

        receiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                String responseString = intent.getStringExtra("RESPONSE");
                data.setText(data.getText()+responseString);
            }
        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermission();
        } else {
            setSaveButton();
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction("ACTION");
        registerReceiver(receiver, filter);
        if (start == null) {
            start = new Date();
            startText.setText(format.format(start));
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mGoogleApiClient != null) {
            ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(mGoogleApiClient, pendingIntent);
            mGoogleApiClient.disconnect();
        }
        unregisterReceiver(receiver);
        clear();
        Log.i("ActivityRecognition","disconnected");
    }

    private void setSaveButton() {
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String startTime = format.format(start);
                Date end = new Date();
                String endTime = format.format(end);
                String stringData = data.getText().toString();

                File tempFile;

                try {
                    Log.i("ActivityRecognition", "in");
                    tempFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath(),
                            "report" + version + ".csv");

                    List<String[]> csvData = new ArrayList<>();
                    for (String line : stringData.split("\n")) {
                        csvData.add(line.split(","));
                    }

                    writer = new FileWriter(tempFile);
                    csvWriter = new CSVWriter(writer);
                    csvWriter.writeAll(csvData,false);
                    csvWriter.close();
                } catch (Exception e) {
                    Log.i("ActivityRecognition", "ex "+e.getMessage());
                    tempFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath(),
                            "report" + version + ".csv");
                }

                Intent email = new Intent(Intent.ACTION_SEND);
                email.putExtra(android.content.Intent.EXTRA_SUBJECT, "Activity Recognition Report version " + version);
                email.putExtra(Intent.EXTRA_STREAM, Uri.parse("file:"+tempFile));
                email.setType("vnd.android.cursor.dir/email");
                email.putExtra(Intent.EXTRA_TEXT, "Data from "+startTime + " to "+endTime+".\n");
                startActivity(Intent.createChooser(email, "Send Report"));
                version++;

                clear();
            }
        });
    }

    private void clear() {
        start = new Date();
        data.setText("Timestamp, Activity, Confidence");
        startText.setText(format.format(start));
    }

    public void checkPermission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){//Can add more as per requirement

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1);
        } else {
            setSaveButton();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setSaveButton();

                }
                return;
            }

        }

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Intent intent = new Intent( this, ActivityRecognizedService.class );
        pendingIntent = PendingIntent.getService( this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT );
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates( mGoogleApiClient, 5000, pendingIntent );

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e("ActivityRecognition" ,"Connection Suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e("ActivityRecognition" ,"Connection Failed");
    }

}
