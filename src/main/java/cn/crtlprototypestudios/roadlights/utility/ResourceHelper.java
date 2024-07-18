package cn.crtlprototypestudios.roadlights.utility;

import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class ResourceHelper {
    public static String MODID = "roadlights";
    public static String CONFIG_FILE = "roadlights.config.json";
    private static List<Identifier> identifiers = new ArrayList<Identifier>();

    public static Identifier find(String path) {
        return findRaw(MODID, path);
    }

    public static Identifier findRaw(String namespace, String path) {
        Identifier id = Identifier.of(namespace, path);
        for(Identifier identifier : identifiers) {
            if(identifier.equals(id)) {
                return identifier;
            }
        }
        identifiers.add(id);
        return id;
    }
}
