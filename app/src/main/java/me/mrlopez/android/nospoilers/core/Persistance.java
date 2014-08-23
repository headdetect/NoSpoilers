package me.mrlopez.android.nospoilers.core;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by Brayden on 8/22/2014.
 */
public class Persistance {

    private static final String FILE = "filters";
    private static final String FILTERS_KEY = "filtersSet";

    public static Set<String> getFilters(Context context) {
        try {
            Set<String> filters = context.getSharedPreferences(FILE, 0).getStringSet(FILTERS_KEY, new HashSet<String>() );
            return filters;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Set<String> addFilter(Context context, String filter) {
        try {
            Set<String> filters = context.getSharedPreferences(FILE, 0).getStringSet(FILTERS_KEY, new HashSet<String>());

            filters.add(filter);


            SharedPreferences settings = context.getSharedPreferences(FILE, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.clear();

            editor.putStringSet(FILTERS_KEY, filters);

            // Commit the edits!
            editor.apply();

            return filters;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void setFilters(Context context, Set<String> filters) {
        try {
            SharedPreferences settings = context.getSharedPreferences(FILE, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.clear();

            editor.putStringSet(FILTERS_KEY, filters);

            // Commit the edits!
            editor.apply();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
