package cn.crtlprototypestudios.roadlights.config.types;

import com.google.gson.JsonObject;

public interface IJsonConvertible<T> {
    public JsonObject toJsonObject();
    public T fromJsonObject(JsonObject json);
}
