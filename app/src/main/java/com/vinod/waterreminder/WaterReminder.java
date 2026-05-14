package com.vinod.waterreminder;

import org.json.JSONException;
import org.json.JSONObject;

final class WaterReminder {
    final int id;
    final String title;
    final int intervalHours;
    final boolean enabled;

    WaterReminder(int id, String title, int intervalHours, boolean enabled) {
        this.id = id;
        this.title = title;
        this.intervalHours = intervalHours;
        this.enabled = enabled;
    }

    WaterReminder withEnabled(boolean nextEnabled) {
        return new WaterReminder(id, title, intervalHours, nextEnabled);
    }

    JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("title", title);
        json.put("intervalHours", intervalHours);
        json.put("enabled", enabled);
        return json;
    }

    static WaterReminder fromJson(JSONObject json) {
        return new WaterReminder(
                json.optInt("id"),
                json.optString("title", "Drink water"),
                Math.max(1, json.optInt("intervalHours", 1)),
                json.optBoolean("enabled", true)
        );
    }
}
