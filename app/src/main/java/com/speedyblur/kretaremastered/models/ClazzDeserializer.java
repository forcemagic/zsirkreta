package com.speedyblur.kretaremastered.models;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public class ClazzDeserializer implements JsonDeserializer<Clazz> {

    @Override
    public Clazz deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsObj = json.getAsJsonObject();
        return new Clazz(jsObj.get("subject").getAsString(), jsObj.get("group").getAsString(), jsObj.get("teacher").getAsString(),
                jsObj.get("room").getAsString(), jsObj.get("classnum").getAsInt(), jsObj.get("begin").getAsInt(), jsObj.get("end").getAsInt(),
                jsObj.get("theme").getAsString(), jsObj.get("isabsent").getAsBoolean(), jsObj.get("isabsent").getAsBoolean() ? new AbsenceDetails(
                        jsObj.get("absencetype").getAsString(), jsObj.get("absenceprovementtype").getAsString(), jsObj.get("proven").getAsBoolean()
        ) : null);
    }
}
