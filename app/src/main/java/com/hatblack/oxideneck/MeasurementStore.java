package com.hatblack.oxideneck;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONException;

/** Small bounded local history; no network or account access. */
public final class MeasurementStore {
    private static final String PREFS="oxide_neck_local", KEY="measurements";
    private static final int MAX=500;
    private final SharedPreferences prefs;
    public MeasurementStore(Context context){prefs=context.getSharedPreferences(PREFS,Context.MODE_PRIVATE);}
    public synchronized void append(Measurement m) throws JSONException {
        JSONArray old=new JSONArray(prefs.getString(KEY,"[]")); JSONArray next=new JSONArray();
        int from=Math.max(0,old.length()-MAX+1);
        for(int i=from;i<old.length();i++) next.put(old.getJSONObject(i));
        next.put(m.toJson()); prefs.edit().putString(KEY,next.toString()).apply();
    }
    public synchronized int count(){return new JSONArray(prefs.getString(KEY,"[]")).length();}
    public synchronized String exportJson(){return prefs.getString(KEY,"[]");}
}
