package edu.ucsb.cs.cs190i.aferguson.googlemapsfencing;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;
import java.util.List;

import static android.app.Notification.VISIBILITY_PUBLIC;
import static android.app.NotificationManager.IMPORTANCE_HIGH;

/**
 * Created by Ferg on 5/23/17.
 * CREDIT TO GOOGLE
 */


//Resource used (following sample): https://raw.githubusercontent.com/googlesamples/android-play-location/master/Geofencing/app/src/main/java/com/google/android/gms/location/sample/geofencing/GeofenceTransitionsIntentService.java
public class GeofenceTransitionsIntentService extends IntentService{

    public GeofenceTransitionsIntentService() {
        // Use the TAG to name the worker thread.
        super("GeofenceTransitionsIS");
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            String errorMessage = GeofenceStatusCodes.getStatusCodeString(
                    geofencingEvent.getErrorCode());
//            Log.e("tag", errorMessage);
            return;
        }
        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();
        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            // Get the geofences that were triggered. A single event
            // can trigger multiple geofences.
            List<Geofence> triggeringGeofences =
                    geofencingEvent.getTriggeringGeofences();

            // Get the transition details as a String.
            List<String> geofenceTransitionDetails = getGeofenceTransitionDetails(
                    this,
                    geofenceTransition,
                    triggeringGeofences
            );
            // Send notification and log the transition details.
            sendNotification(geofenceTransitionDetails);
//              Log.i(TAG, geofenceTransitionDetails);
        } else {
            // Log the error.
            // Log.e(TAG, getString(R.string.geofence_transition_invalid_type,geofenceTransition));
        }

    }

    private List<String> getGeofenceTransitionDetails(
            Context context,
            int geofenceTransition,
            List<Geofence> triggeringGeofences) {

        List<String> transitionDetails = new ArrayList<>();
        String geofenceTransitionString = getTransitionString(geofenceTransition);
        transitionDetails.add(geofenceTransitionString);

        // Get the Ids of each geofence that was triggered.
        ArrayList triggeringGeofencesIdsList = new ArrayList();
        for (Geofence geofence : triggeringGeofences) {
            triggeringGeofencesIdsList.add(geofence.getRequestId());
        }
        String triggeringGeofencesIdsString = TextUtils.join(", ",  triggeringGeofencesIdsList);
        transitionDetails.add(triggeringGeofencesIdsString);

        return transitionDetails;
    }

    private void sendNotification(List <String> notificationDetails) {
        // Create an explicit content Intent that starts the main Activity.
        Intent notificationIntent = new Intent(getApplicationContext(), MapsActivity.class); //MainActivity.class

        // Construct a task stack.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

        // Add the main Activity to the task stack as the parent.
        stackBuilder.addParentStack(MapsActivity.class); //MainActivity.class

        // Push the content Intent onto the stack.
        stackBuilder.addNextIntent(notificationIntent);

        // Get a PendingIntent containing the entire back stack.
        PendingIntent notificationPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        // Get a notification builder that's compatible with platform versions >= 4
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        // Define the notification settings.
        //resource used: https://stackoverflow.com/questions/11271991/uri-to-default-sound-notification
        //resource used: https://developer.android.com/guide/topics/ui/notifiers/notifications.html
        Uri soundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        builder.setSmallIcon(android.R.drawable.ic_dialog_map)//dialog map ,set prioirty proiority high, set default
                // In a real app, you may want to use a library like Volley
                // to decode the Bitmap.
//                .setLargeIcon(BitmapFactory.decodeResource(getResources(), android.R.drawable.ic_dialog_map))
                //        android.R.drawable.ic_launcher))
                //.setColor(Color.RED)
                .setContentTitle(notificationDetails.get(0))
                .setContentText(notificationDetails.get(1))//getString(R.string.geofence_transition_notification_text))
                .setVisibility(VISIBILITY_PUBLIC)
                .setPriority(Notification.PRIORITY_HIGH)

                .setSound(soundUri)
                .setContentIntent(notificationPendingIntent);

        // Dismiss notification once the user touches it.
        builder.setAutoCancel(true);

        // Get an instance of the Notification manager
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Issue the notification
        mNotificationManager.notify(0, builder.build());
//        Log.d("geofence", "notifcation issued");
    }

    private String getTransitionString(int transitionType) {
        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return "Entered Point of Interest";//getString(R.string.geofence_transition_entered);
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return "Exited Point of Interest";//getString(R.string.geofence_transition_exited);
            default:
                return "Geofence transition unknown ";//getString(R.string.unknown_geofence_transition);
        }
    }

}
