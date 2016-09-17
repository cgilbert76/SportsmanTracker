package com.mycompany.sportsmantracker;

import android.util.Log;
import com.google.common.base.Strings;
import com.mapbox.mapboxsdk.maps.MapView;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * A GeoJSON parser.
 */
public class GeoJSON {

    public static List<Object> parse(JSONObject json, MapView mv) throws JSONException {
        ArrayList<Object> uiObjects = new ArrayList<Object>();
        String type = json.optString("type");

        if (Strings.isNullOrEmpty(type)) {
            Log.w(GeoJSON.class.getCanonicalName(), "type is null, so returning.");
            return uiObjects;
        }

        if (type.equals("FeatureCollection")) {
            uiObjects.addAll(featureCollectionToLayers(json, mv));
        }

        return uiObjects;
    }

    public static ArrayList<Object> featureCollectionToLayers(JSONObject featureCollection,
                                                              MapView mv) throws JSONException {
        ArrayList<Object> uiObjects = new ArrayList<Object>();

        JSONArray features = (JSONArray) featureCollection.get("features");
        for (int i = 0; i < features.length(); i++) {
            uiObjects.addAll(featureToLayer((JSONObject) features.get(i), mv));
        }
        return uiObjects;
    }

    public static List<Object> featureToLayer(JSONObject feature, MapView mv) throws JSONException {

        ArrayList<Object> uiObjects = new ArrayList<Object>();

        JSONObject properties = (JSONObject) feature.get("properties");
        String title = properties.optString("title");
        String description = properties.optString("description");
        JSONObject geometry = (JSONObject) feature.get("geometry");

        if (Strings.isNullOrEmpty(geometry.optString("type"))) {
            Log.w(GeoJSON.class.getCanonicalName(), "type is null, so can't parse anything.");
            return uiObjects;
        }

        uiObjects.add(title);
        uiObjects.add(description);

        String type = geometry.optString("type");
        if (type.equals("Point")) {
            JSONArray coordinates = (JSONArray) geometry.get("coordinates");
            uiObjects.add(coordinates);
        }

        return uiObjects;
    }
}

