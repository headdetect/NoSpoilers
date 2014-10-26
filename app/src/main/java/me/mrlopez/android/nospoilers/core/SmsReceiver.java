package me.mrlopez.android.nospoilers.core;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Set;

import me.mrlopez.android.nospoilers.IndexActivity;
import me.mrlopez.android.nospoilers.R;

/**
 * Created by Brayden on 8/20/2014.
 */
public class SmsReceiver extends BroadcastReceiver {

    static {
        ClassLoader myClassLoader = SmsReceiver.class.getClassLoader();
        Thread.currentThread().setContextClassLoader(myClassLoader);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED") || !Persistance.isOn(context)) {
            return;
        }

        Bundle myBundle = intent.getExtras();
        SmsMessage[] messages = null;

        TelephonyManager mTelephonyMgr;
        mTelephonyMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String lineNumber = mTelephonyMgr.getLine1Number().replace("-", "").replace("+", "");


        if (myBundle != null) {
            Object[] pdus = (Object[]) myBundle.get("pdus");
            messages = new SmsMessage[pdus.length];

            for (int i = 0; i < messages.length; i++) {
                messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                String from = messages[i].getOriginatingAddress().replace("-", "").replace("+", "");
                String message = messages[i].getMessageBody();

                if (lineNumber.equals(from)) {
                    // If we get message from self //
                    return;
                }

                Set<String> filters = Persistance.getFilters(context);

                for (String filter : filters) {
                    if (message.contains(filter)) {
                        notifyBlocked(context, "No Spoiler", getContactName(context, from) + " has sent you a message!", from);
                        // Persistance.updateMessages(from, message);
                        this.setOrderedHint(true);
                        this.abortBroadcast();
                        break;
                    }
                }

            }
        }

    }

    private void notifyBlocked(Context context, String head, String minor, String from) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context).setSmallIcon(R.drawable.ic_launcher).setContentTitle(head).setContentText(minor);

        Intent resultIntent = new Intent();
        resultIntent.putExtra("msg", minor);
        resultIntent.putExtra("from", from);
        resultIntent.setComponent(new ComponentName(context, IndexActivity.class));
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent intent = PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentIntent(intent);
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder.setAutoCancel(true);

        mNotificationManager.notify(from.hashCode(), mBuilder.build());

    }



    private String getContactName(Context context, String number) {

        String name = number;

        String[] projection = new String[] {
                ContactsContract.PhoneLookup.DISPLAY_NAME,
                ContactsContract.PhoneLookup._ID};

        Uri contactUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));

        Cursor cursor = context.getContentResolver().query(contactUri, projection, null, null, null);

        if(cursor != null) {
            if (cursor.moveToFirst()) {
                name = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
                Log.v("NOSPOILER", "Contact Found @ " + number);
                Log.v("NOSPOILER", "Contact name  = " + name);
            } else {
                Log.v("NOSPOILER", "Contact Not Found @ " + number);
            }
            cursor.close();
        }
        return name;
    }
}
