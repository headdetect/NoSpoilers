package me.mrlopez.android.nospoilers;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.app.Fragment;
import android.provider.Telephony;
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

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import me.mrlopez.android.nospoilers.core.Persistance;


public class FiltersFragment extends Fragment {

    private ListView lstFilters;
    private TextView txtEmptyFiltersList;

    public FiltersFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_filters, container, false);
    }

    @Override
    public void onStart() {
        txtEmptyFiltersList = (TextView) getActivity().findViewById(R.id.txtEmptyFiltersList);

        lstFilters = (ListView) getActivity().findViewById(R.id.lstFilters);
        lstFilters.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long ld) {
                listItemClickedDialog(position);
            }
        });
        reloadList();

        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= Build.VERSION_CODES.KITKAT) {
            new AlertDialog.Builder(getActivity())
                    .setTitle("Oops!")
                    .setMessage("It seems you upgraded your phone. Unfortunately it does not work so well on this version. You may still use this app, but it will not block messages from coming in.")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {

                        }
                    }).show();

        }

        super.onStart();
    }

    private void reloadList() {
        ArrayList<String> filters = new ArrayList<String>(Persistance.getFilters(getActivity()));

        lstFilters = (ListView) getActivity().findViewById(R.id.lstFilters);
        lstFilters.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, filters));

        txtEmptyFiltersList.setVisibility(filters.size() <= 0 ? View.VISIBLE : View.GONE);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.filters_menu, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle item selection
        switch (item.getItemId()) {
            case R.id.action_add_filter:
                openCreateFilterDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void openCreateFilterDialog() {
        final EditText input = new EditText(getActivity());
        new AlertDialog.Builder(getActivity())
                .setTitle("New Filter!")
                .setMessage("Enter the filter you would like to create. eg. \"Big Brother\"")
                .setView(input)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Editable value = input.getText();
                        Persistance.addFilter(getActivity(), value.toString());
                        reloadList();
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Do nothing.
            }
        }).show();
    }

    private void openCreateFilterDialog(final int filterIndex) {
        final ArrayList<String> filters = new ArrayList<String>(Persistance.getFilters(getActivity()));
        final String filter = filters.get(filterIndex);

        final EditText input = new EditText(getActivity());
        input.setText(filter);
        new AlertDialog.Builder(getActivity())
                .setTitle("Edit Filter!")
                .setMessage("Enter the filter you would like to create. eg. \"Big Brother\"")
                .setView(input)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Editable value = input.getText();
                        filters.set(filterIndex, value.toString());
                        Persistance.setFilters(getActivity(), new HashSet<String>(filters));
                        reloadList();
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Do nothing.
            }
        }).show();
    }

    private void listItemClickedDialog(final int filterIndex) {
        final Set<String> filters = Persistance.getFilters(getActivity());
        final ArrayList<String> filtersList = new ArrayList<String>(filters);
        new AlertDialog.Builder(getActivity())
                .setTitle(filtersList.get(filterIndex))
                .setPositiveButton("Edit", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        openCreateFilterDialog(filterIndex);
                    }
                }).setNegativeButton("Delete", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                filtersList.remove(filterIndex);
                Persistance.setFilters(getActivity(), new HashSet<String>(filterIndex));
                reloadList();
            }
        }).show();
    }
}
