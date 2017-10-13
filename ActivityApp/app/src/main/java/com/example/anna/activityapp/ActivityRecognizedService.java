package com.example.anna.activityapp;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by Anna on 12-10-2017.
 */

public class ActivityRecognizedService extends IntentService {

    public ActivityRecognizedService() {
        super("ActivityRecognizedService");

    }

    public ActivityRecognizedService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if(ActivityRecognitionResult.hasResult(intent)) {
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            String row = handleDetectedActivities( result.getProbableActivities() );
            Log.i("ActivityRecognition", row);
            Intent broadcastIntent = new Intent("ACTION");
            broadcastIntent.putExtra("RESPONSE", row);
            Log.i("ActivityRecognition", "send now");
            sendBroadcast(broadcastIntent);
            Log.i("ActivityRecognition", "sen finished");
        }
    }

    private String handleDetectedActivities(List<DetectedActivity> probableActivities) {
        int max = 0;
        String act = "";
        String time = null;
        for( DetectedActivity activity : probableActivities ) {
            if (max < activity.getConfidence()) {
                max = activity.getConfidence();
                SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
                time = format.format(new Date());

                switch( activity.getType() ) {
                    case DetectedActivity.IN_VEHICLE: {
                        act = "IN VEHICLE";
                        Log.e( "ActivityRecognition", "In Vehicle: " + activity.getConfidence() );
                        break;
                    }
                    case DetectedActivity.ON_BICYCLE: {
                        act = "ON BICYCLE";
                        Log.e( "ActivityRecognition", "On Bicycle: " + activity.getConfidence() );
                        break;
                    }
                    case DetectedActivity.ON_FOOT: {
                        act = "ON FOOT";
                        Log.e( "ActivityRecognition", "On Foot: " + activity.getConfidence() );
                        break;
                    }
                    case DetectedActivity.RUNNING: {
                        act = "RUNNING";
                        Log.e( "ActivityRecognition", "Running: " + activity.getConfidence() );
                        break;
                    }
                    case DetectedActivity.STILL: {
                        act = "STILL";
                        Log.e( "ActivityRecognition", "Still: " + activity.getConfidence() );
                        break;
                    }
                    case DetectedActivity.TILTING: {
                        act = "TILTING";
                        Log.e( "ActivityRecognition", "Tilting: " + activity.getConfidence() );
                        break;
                    }
                    case DetectedActivity.WALKING: {
                        act = "WALKING";
                        Log.e( "ActivityRecognition", "Walking: " + activity.getConfidence() );
                        break;
                    }
                    case DetectedActivity.UNKNOWN: {
                        act = "UKNOWN";
                        Log.e( "ActivityRecognition", "Unknown: " + activity.getConfidence() );
                        break;
                    }
                }
            }

        }
        return "\n"+time+","+act+","+max;

    }


}
