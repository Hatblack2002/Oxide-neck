package com.hatblack.oxideneck;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/** Persistent local profile CRUD and active-profile selection. */
public final class ProfileStore {
    private final SharedPreferences prefs;
    public ProfileStore(Context context) { prefs = context.getSharedPreferences("calibration_profiles", Context.MODE_PRIVATE); }

    public synchronized void save(CalibrationProfile profile) {
        try {
            JSONArray profiles = all();
            JSONObject value = toJson(profile);
            boolean replaced = false;
            for (int i = 0; i < profiles.length(); i++) {
                JSONObject old = profiles.optJSONObject(i);
                if (old != null && profile.name.equals(old.optString("name"))) {
                    profiles.put(i, value); replaced = true; break;
                }
            }
            if (!replaced) profiles.put(value);
            prefs.edit().putString("profiles", profiles.toString()).apply();
        } catch (Exception e) { throw new IllegalStateException("Profile persistence failed", e); }
    }

    public synchronized JSONArray all() {
        try { return new JSONArray(prefs.getString("profiles", "[]")); }
        catch (JSONException e) { throw new IllegalStateException("Stored calibration profiles are malformed", e); }
    }

    public synchronized CalibrationProfile get(String name) {
        JSONArray profiles = all();
        for (int i = 0; i < profiles.length(); i++) {
            JSONObject o = profiles.optJSONObject(i);
            if (o != null && name.equals(o.optString("name"))) {
                Object fov = o.opt("fov");
                double fovValue = fov == null || fov == JSONObject.NULL ? Double.NaN : o.optDouble("fov", Double.NaN);
                return new CalibrationProfile(name, o.optInt("width"), o.optInt("height"),
                    o.optDouble("sensitivity", 1), o.optDouble("x"), o.optDouble("y"),
                    o.optDouble("x2", .5), o.optDouble("y2", .5), fovValue);
            }
        }
        return null;
    }

    public synchronized boolean delete(String name) {
        JSONArray old = all(), next = new JSONArray();
        boolean removed = false;
        for (int i = 0; i < old.length(); i++) {
            JSONObject item = old.optJSONObject(i);
            if (item != null && name.equals(item.optString("name"))) removed = true;
            else if (item != null) next.put(item);
        }
        if (removed) {
            SharedPreferences.Editor editor = prefs.edit().putString("profiles", next.toString());
            if (name.equals(getActiveName())) editor.remove("active_profile");
            editor.apply();
        }
        return removed;
    }

    public synchronized void setActive(String name) {
        if (name != null && get(name) == null) throw new IllegalArgumentException("Unknown profile");
        prefs.edit().putString("active_profile", name == null ? "" : name).apply();
    }
    public synchronized String getActiveName() { return prefs.getString("active_profile", ""); }
    public synchronized CalibrationProfile getActive() {
        String name = getActiveName();
        return name.isEmpty() ? null : get(name);
    }

    private JSONObject toJson(CalibrationProfile p) throws JSONException {
        JSONObject o = new JSONObject();
        o.put("name", p.name); o.put("width", p.screenWidth); o.put("height", p.screenHeight);
        o.put("sensitivity", p.sensitivity); o.put("x", p.referenceX); o.put("y", p.referenceY);
        o.put("x2", p.horizontalOffset); o.put("y2", p.verticalOffset);
        if (Double.isNaN(p.fovDegrees)) o.put("fov", JSONObject.NULL); else o.put("fov", p.fovDegrees);
        return o;
    }
}
