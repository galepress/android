package ak.detaysoft.galepress;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;
import com.google.android.gcm.GCMRegistrar;

public class GCMIntentService extends GCMBaseIntentService {

    private static final String TAG = "GCMIntentService";

    private GalePressApplication aController = null;

    public GCMIntentService() {
        // Call extended class Constructor GCMBaseIntentService
        super(DataApi.GCM_SENDER_ID);
    }

    /*
    * https://github.com/8fit/OneSignal-Android-SDK/commit/3b06417d35498013a8cd8dc038e2f7bf22f9bf8e
    * */
    @Override
    public void onStart(Intent intent, int startId) {
        if (intent != null) {
            super.onStart(intent, startId);
        }
    }

    /**
     * Method called on device registered
     **/
    @Override
    protected void onRegistered(Context context, String registrationId) {

        //Get Global Controller Class object (see application tag in AndroidManifest.xml)
        if (aController == null)
            aController = GalePressApplication.getInstance();

        Log.i(TAG, "Device registered: regId = " + registrationId);
        GCMRegistrar.setRegisteredOnServer(GalePressApplication.getInstance().getApplicationContext(), true);
        GalePressApplication.getInstance().getDataApi().getAppDetail(context);
//        Log.d("NAME", MainActivity.name);
//        aController.register(context, MainActivity.name, MainActivity.email, registrationId);
    }

    /**
     * Method called on device unregistred
     */
    @Override
    protected void onUnregistered(Context context, String registrationId) {
        if (aController == null)
            aController = (GalePressApplication) getApplicationContext();
        Log.i(TAG, "Device unregistered");
//        aController.displayMessageOnScreen(context, getString(R.string.gcm_unregistered));
//        aController.unregister(context, registrationId);
    }

    /**
     * Method called on Receiving a new message from GCM server
     */
    @Override
    protected void onMessage(Context context, Intent intent) {

        if (aController == null)
            aController = GalePressApplication.getInstance();

        Log.i(TAG, "Received message");
        String message = "";
        if (intent != null && intent.getExtras() != null) {
            message = intent.getExtras().getString("message", "");
        }
        if(message != null && message.length() > 0) {
            aController.displayMessageOnScreen(context, message);
            // notifies user
            generateNotification(context, message);
        }
    }

    /**
     * Method called on receiving a deleted message
     */
    @Override
    protected void onDeletedMessages(Context context, int total) {

        if (aController == null)
            aController = GalePressApplication.getInstance();

        Log.i(TAG, "Received deleted messages notification");
//      String message = getString(R.string.gcm_deleted, total);
        String message = "R.string.gcm_deleted";
        aController.displayMessageOnScreen(context, message);
        // notifies user
        generateNotification(context, message);
    }

    /**
     * Method called on Error
     */
    @Override
    public void onError(Context context, String errorId) {

        if (aController == null)
            aController = GalePressApplication.getInstance();

        Log.i(TAG, "Received error: " + errorId);
//        aController.displayMessageOnScreen(context, getString(R.string.gcm_error, errorId));
        aController.displayMessageOnScreen(context, "R.string.gcm_error");
    }

    @Override
    protected boolean onRecoverableError(Context context, String errorId) {

        if (aController == null)
            aController = GalePressApplication.getInstance();

        // log message
        Log.i(TAG, "Received recoverable error: " + errorId);
//        aController.displayMessageOnScreen(context, getString(R.string.gcm_recoverable_error,errorId));
        aController.displayMessageOnScreen(context, "R.string.gcm_recoverable_error");
        return super.onRecoverableError(context, errorId);
    }

    /**
     * Create a notification to inform the user that server has sent a message.
     */
    private static void generateNotification(Context context, String message) {

        String title = context.getString(R.string.app_name);

        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context,
                0, notificationIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        Notification.Builder builder = new Notification.Builder(context);

        builder.setContentIntent(contentIntent)
                .setSmallIcon(R.drawable.ic_launcher)
                .setTicker(title)
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setContentTitle(title)
                .setContentText(message);
        Notification n = builder.build();

        nm.notify(0, n);

    }

}
