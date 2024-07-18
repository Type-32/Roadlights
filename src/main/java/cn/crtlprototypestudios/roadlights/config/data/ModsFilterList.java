package cn.crtlprototypestudios.roadlights.config.data;

import cn.crtlprototypestudios.roadlights.config.types.IJsonConvertible;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class ModsFilterList implements IJsonConvertible<ModsFilterList> {
    public List<String> ids = new ArrayList<String>();

    public ModsFilterList(List<String> ids) {
        this.ids = ids;
    }

    @Override
    public JsonObject toJsonObject() {
        JsonArray array = new JsonArray();
        for (String id : ids){
            array.add(id);
        }
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("ids", array);
        return jsonObject;
    }

    @Override
    public ModsFilterList fromJsonObject(JsonObject json) {
        List<String> temp = new ArrayList<>();
        for (JsonElement element : json.getAsJsonArray("ids").asList())
            temp.add(element.getAsString());

        return new ModsFilterList(temp);
    }
}
