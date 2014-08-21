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

import java.util.ArrayList;

import me.mrlopez.android.nospoilers.IndexActivity;
import me.mrlopez.android.nospoilers.R;

/**
 * Created by Brayden on 8/20/2014.
 */
public class SmsReceiver extends BroadcastReceiver {

    static {
        ClassLoader myClassLoader = SmsReceiver.class.getClassLoader();
        Thread.currentThread().setContextClassLoader( myClassLoader );
    }

    @Override
    public void onReceive( Context context , Intent intent ) {
        if ( !intent.getAction().equals( "android.provider.Telephony.SMS_RECEIVED" ) ) {
            return;
        }

        Bundle myBundle = intent.getExtras();
        SmsMessage[] messages = null;

        TelephonyManager mTelephonyMgr;
        mTelephonyMgr = (TelephonyManager) context.getSystemService( Context.TELEPHONY_SERVICE );
        String lineNumber = mTelephonyMgr.getLine1Number().replace( "-" , "" ).replace( "+" , "" );


        if ( myBundle != null ) {
            Object[] pdus = (Object[]) myBundle.get( "pdus" );
            messages = new SmsMessage[ pdus.length ];

            for ( int i = 0; i < messages.length; i++ ) {
                messages[ i ] = SmsMessage.createFromPdu( (byte[]) pdus[ i ] );
                String from = messages[ i ].getOriginatingAddress().replace( "-" , "" ).replace( "+" , "" );
                String message = messages[ i ].getMessageBody();

                if(lineNumber.equals( from ) ) {
                    return;
                }

            }
        }

    }

    private void notifyBlocked( Context context , String head , String minor , String from, String formatBlocked ) {

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder( context ).setSmallIcon( R.drawable.ic_launcher ).setContentTitle( head ).setContentText( minor );

        Intent resultIntent = new Intent();
        resultIntent.putExtra( "msg" , minor );
        resultIntent.putExtra( "from" , from );
        resultIntent.setComponent( new ComponentName( context , IndexActivity.class ) );
        resultIntent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP );

        PendingIntent intent = PendingIntent.getActivity( context , 0 , resultIntent , PendingIntent.FLAG_UPDATE_CURRENT );

        mBuilder.setContentIntent( intent );
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService( Context.NOTIFICATION_SERVICE );
        mBuilder.setAutoCancel( true );

        mNotificationManager.notify();
    }

    private void sendMessage( String number , String message ) {
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage( number , null , message , null , null );
    }

    private ArrayList< String > getNumbers( Context c , String num1 ) {
        Uri uri = Uri.withAppendedPath( ContactsContract.PhoneLookup.CONTENT_FILTER_URI , Uri.encode( num1 ) );
        Cursor cursor = c.getContentResolver().query( uri , null , null , null , null );
        ArrayList< String > mNumbers = new ArrayList< String >();
        mNumbers.add( num1 );
        while ( cursor.moveToNext() ) {
            String contactId = cursor.getString( cursor.getColumnIndex( ContactsContract.Contacts._ID ) );
            String hasPhone = cursor.getString( cursor.getColumnIndex( ContactsContract.Contacts.HAS_PHONE_NUMBER ) );
            if ( hasPhone.equals( "1" ) ) {
                // You know have the number so now query it like this
                Cursor phones = c.getContentResolver().query( ContactsContract.CommonDataKinds.Phone.CONTENT_URI , null ,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId , null , null );
                while ( phones.moveToNext() ) {
                    String number = phones.getString( phones.getColumnIndex( ContactsContract.CommonDataKinds.Phone.NUMBER ) );
                    if ( !mNumbers.contains( number ) ) {
                        mNumbers.add( number );
                    }
                }

                phones.close();
            }

        }

        cursor.close();
        return mNumbers;

    }
}
