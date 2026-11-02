package com.hatblack.oxideneck;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;

/** Persistent named calibration profiles, stored locally on device. */
public final class ProfileStore {
    private final SharedPreferences prefs;
    public ProfileStore(Context c){prefs=c.getSharedPreferences("calibration_profiles",Context.MODE_PRIVATE);}
    public synchronized void save(CalibrationProfile p){
        try{
            JSONArray a=new JSONArray(prefs.getString("profiles","[]")); boolean replaced=false;
            JSONObject o=toJson(p);
            for(int i=0;i<a.length();i++) if(a.getJSONObject(i).optString("name").equals(p.name)){a.put(i,o);replaced=true;break;}
            if(!replaced)a.put(o);
            prefs.edit().putString("profiles",a.toString()).apply();
        }catch(Exception e){throw new IllegalStateException("Profile persistence failed",e);}
    }
    public synchronized JSONArray all(){return new JSONArray(prefs.getString("profiles","[]"));}
    public synchronized CalibrationProfile get(String name){
        JSONArray a=all();
        for(int i=0;i<a.length();i++){JSONObject o=a.optJSONObject(i);if(o!=null&&name.equals(o.optString("name"))){
            return new CalibrationProfile(name,o.optInt("width"),o.optInt("height"),o.optDouble("sensitivity"),
                o.optDouble("x"),o.optDouble("y"),o.optDouble("horizontal"),o.optDouble("vertical"),
                o.has("fov")?o.optDouble("fov"):Double.NaN);
        }} return null;
    }
    private JSONObject toJson(CalibrationProfile p)throws Exception{
        JSONObject o=new JSONObject();o.put("name",p.name);o.put("width",p.screenWidth);o.put("height",p.screenHeight);
        o.put("sensitivity",p.sensitivity);o.put("x",p.referenceX);o.put("y",p.referenceY);
        o.put("horizontal",p.horizontalOffset);o.put("vertical",p.verticalOffset);
        if(Double.isNaN(p.fovDegrees))o.put("fov",JSONObject.NULL);else o.put("fov",p.fovDegrees);
        return o;
    }
}
