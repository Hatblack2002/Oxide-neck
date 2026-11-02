package com.hatblack.oxideneck;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONException;

/** Bounded local measurement history; no network or account access. */
public final class MeasurementStore {
    private static final String PREFS = "oxide_neck_local", KEY = "measurements";
    private static final int MAX = 500;
    private final SharedPreferences prefs;

    public MeasurementStore(Context context) {
        prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public synchronized void append(Measurement measurement) throws JSONException {
        JSONArray old = read();
        JSONArray next = new JSONArray();
        int from = Math.max(0, old.length() - MAX + 1);
        for (int i = from; i < old.length(); i++) next.put(old.getJSONObject(i));
        next.put(measurement.toJson());
        prefs.edit().putString(KEY, next.toString()).apply();
    }

    public synchronized int count() { return read().length(); }
    public synchronized String exportJson() { return read().toString(); }

    private JSONArray read() {
        try {
            return new JSONArray(prefs.getString(KEY, "[]"));
        } catch (JSONException e) {
            throw new IllegalStateException("Stored measurement history is malformed", e);
        }
    }
}
