package cn.crtlprototypestudios.roadlights;

import cn.crtlprototypestudios.roadlights.utility.ResourceHelper;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Roadlights implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger(ResourceHelper.MODID);
    private static List<String> blacklistedMods = new ArrayList<>();

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Roadlights");
    }

    private void loadBlacklist() {
        // Load your blacklisted mod IDs here
        blacklistedMods.add("some_mod_id");
        blacklistedMods.add("another_mod_id");
    }

    private void monitorFileOperations() {
        Path gameDir = getGameDir();
        FileAlterationObserver observer = new FileAlterationObserver(gameDir.toFile());
        observer.addListener(new FileAlterationListener() {
            @Override
            public void onDirectoryChange(File directory) {

            }

            @Override
            public void onDirectoryCreate(File directory) {

            }

            @Override
            public void onDirectoryDelete(File directory) {

            }

            @Override
            public void onFileChange(File file) {

            }

            @Override
            public void onFileCreate(File file) {
                LOGGER.info("File created: " + file.getAbsolutePath());
            }

            @Override
            public void onFileDelete(File file) {

            }

            @Override
            public void onStart(FileAlterationObserver observer) {

            }

            @Override
            public void onStop(FileAlterationObserver observer) {

            }
        });

        FileAlterationMonitor monitor = new FileAlterationMonitor(5000);
        monitor.addObserver(observer);
        try {
            monitor.start();
        } catch (Exception e) {
            LOGGER.error("Failed to start file monitor", e);
        }
    }

    public static List<ModContainer> getFilteredModList() {
        List<ModContainer> allMods = new ArrayList<>(FabricLoader.getInstance().getAllMods());
        allMods.forEach(modContainer -> {
            if (modContainer.getMetadata().getId().equals("roadlights") || blacklistedMods.contains(modContainer.getMetadata().getId())) {
                allMods.remove(modContainer);
            }
        });
        return allMods;
    }

    public static Path getGameDir() {
        return FabricLoader.getInstance().getGameDir();
    }
}
