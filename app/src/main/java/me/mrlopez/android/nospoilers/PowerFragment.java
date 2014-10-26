package me.mrlopez.android.nospoilers;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.app.Fragment;
import android.provider.Telephony;
import android.support.v4.app.NotificationCompat;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import me.mrlopez.android.nospoilers.core.Persistance;


public class PowerFragment extends Fragment {

    private ToggleButton btnIsOn;

    public PowerFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_power, container, false);
    }

    @Override
    public void onStart() {
        btnIsOn = (ToggleButton) getActivity().findViewById(R.id.btnIsOn);
        btnIsOn.setChecked(Persistance.isOn(getActivity()));
        btnIsOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isOn = btnIsOn.isChecked();
                Persistance.setIsOn(getActivity(), isOn);
                if (isOn) notifyIsOn();
                else dismissNotification();
            }
        });

        int currentApiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentApiVersion >= Build.VERSION_CODES.KITKAT) {
            TextView txtWarning = (TextView) getActivity().findViewById(R.id.txtWarning);
            txtWarning.setVisibility(View.VISIBLE);
            txtWarning.setText("Warning! This app may not work with your device.");
            txtWarning.setTextColor((int) 0xFFFF8800); // A gold-ish yellow //
        }

        super.onStart();
    }

    private void notifyIsOn() {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getActivity()).setSmallIcon(R.drawable.ic_launcher).setContentTitle("No Spoilers!").setContentText("Blocking spoils...");

        Intent resultIntent = new Intent();
        resultIntent.setComponent(new ComponentName(getActivity(), IndexActivity.class));
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent intent = PendingIntent.getActivity(getActivity(), 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentIntent(intent);
        NotificationManager mNotificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder.setAutoCancel(false);
        mBuilder.setOngoing(true);

        mNotificationManager.notify(0, mBuilder.build());
    }
    private void dismissNotification() {
        NotificationManager mNotificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(0);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.filters_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

}
