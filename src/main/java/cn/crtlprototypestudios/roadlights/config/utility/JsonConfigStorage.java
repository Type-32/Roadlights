package cn.crtlprototypestudios.roadlights.config.utility;

import cn.crtlprototypestudios.roadlights.config.types.IJsonConvertible;
import cn.crtlprototypestudios.roadlights.utility.ResourceHelper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;

public class JsonConfigStorage {
    public static final Path MOD_DATA_DIR = FabricLoader.getInstance().getGameDir()
            .resolve(ResourceHelper.MODID)
            .resolve("data");
    public static final Path MOD_CONFIG_DIR = FabricLoader.getInstance().getGameDir()
            .resolve(ResourceHelper.MODID)
            .resolve("config");

    /**
     * Resolves the file path for data based on the filename and whether it's a relay file.
     *
     * @param filename the name of the file
     * @param relay    true if it's a relay file, false otherwise
     * @return the resolved path for the data file
     */
    @Contract(pure = true)
    private @NotNull Path resolveDataFilePath(String filename, boolean relay) {
        return MOD_DATA_DIR.resolve(filename + (relay ? ".temp.json" : ".json"));
    }

    /**
     * Resolves the file path for configuration based on the filename and whether it's a relay file.
     *
     * @param filename the name of the file
     * @param relay    true if it's a relay file, false otherwise
     * @return the resolved path for the configuration file
     */
    @Contract(pure = true)
    private @NotNull Path resolveConfigFilePath(String filename, boolean relay) {
        return MOD_CONFIG_DIR.resolve(filename + (relay ? ".temp.json" : ".json"));
    }

    /**
     * Saves the given data object to a JSON file.
     *
     * @param  data     the data object to be saved
     * @param  filename the name of the file to save the data to
     * @param  relay    a boolean flag indicating whether to relay the data
     */
    public void saveData(IJsonConvertible data, String filename, boolean relay) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Path filePath = resolveDataFilePath(filename, relay);
        try {
            Files.createDirectories(filePath.getParent());
            String json = gson.toJson(data.toJsonObject());
            Files.writeString(filePath, json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Loads the data of the specified class from a file with the given filename, optionally as a relay file.
     *
     * @param dataClass the class of the data to load
     * @param filename  the name of the file
     * @param relay     true if it's a relay file, false otherwise
     * @param <T>       the type of the data
     * @return the loaded data
     */
    public <T extends IJsonConvertible<T>> T loadData(Class<T> dataClass, String filename, boolean relay, Supplier<T> supplier) {
        Gson gson = new Gson();
        Path filePath = resolveDataFilePath(filename, relay);
        if (Files.exists(filePath)) {
            try {
                String json = Files.readString(filePath);
                JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
                T instance = supplier.get(); // Get a new instance from the supplier
                return instance.fromJsonObject(jsonObject); // Use the fromJsonObject method
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null; // Or handle this case as you see fit
    }

    /**
     * Saves the given data object to a JSON file.
     *
     * @param  data     the data object to be saved
     * @param  filename the name of the file to save the data to
     * @param  relay    a boolean flag indicating whether to relay the data
     */
    public void saveConfig(IJsonConvertible data, String filename, boolean relay) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Path filePath = resolveConfigFilePath(filename, relay);
        try {
            Files.createDirectories(filePath.getParent());
            String json = gson.toJson(data.toJsonObject());
            Files.writeString(filePath, json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Loads the data of the specified class from a file with the given filename, optionally as a relay file.
     *
     * @param dataClass the class of the data to load
     * @param filename  the name of the file
     * @param relay     true if it's a relay file, false otherwise
     * @param <T>       the type of the data
     * @return the loaded data
     */
    public <T> T loadConfig(Class<T> dataClass, String filename, boolean relay) {
        Gson gson = new Gson();
        Path filePath = resolveConfigFilePath(filename, relay);
        if (Files.exists(filePath)) {
            try {
                String json = Files.readString(filePath);
                return gson.fromJson(new JsonReader(Files.newBufferedReader(filePath)), dataClass);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            return dataClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Unable to create instance of data class", e);
        }
    }

    /**
     * Cleans up relay files in the data and configuration directories.
     */
    public void cleanRelay(){
        for(Path i : MOD_DATA_DIR){
            if(i.endsWith(".temp.json")) {
                try {
                    Files.delete(i);
                } catch (IOException e) {
                    System.out.println("[Control UI] [Warning] Unable to delete relay files... This may cause some data saving issues.");
                }
            }
        }
        for(Path i : MOD_CONFIG_DIR){
            if(i.endsWith(".temp.json")) {
                try {
                    Files.delete(i);
                } catch (IOException e) {
                    System.out.println("[Control UI] [Warning] Unable to delete relay files... This may cause some data saving issues.");
                }
            }
        }
    }
}
