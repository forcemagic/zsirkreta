package com.speedyblur.kretaremastered.models;

import com.github.mikephil.charting.data.Entry;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class AvgGraphDataDeserializer implements JsonDeserializer<AvgGraphData> {

    @Override
    public AvgGraphData deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        ArrayList<Entry> entries = new ArrayList<>();
        String subject = json.getAsJsonObject().get("subject").getAsString();
        JsonArray points = json.getAsJsonObject().getAsJsonArray("points");

        for (int i = 0; i < points.size(); i++) {
            JsonObject jsObj = points.get(i).getAsJsonObject();
            entries.add(new Entry(jsObj.get("x").getAsFloat(), jsObj.get("y").getAsFloat(), jsObj.get("isspecial").getAsBoolean()));
        }
        return new AvgGraphData(subject, entries);
    }
}
