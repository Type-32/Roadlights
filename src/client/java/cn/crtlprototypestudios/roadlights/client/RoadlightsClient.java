package cn.crtlprototypestudios.roadlights.client;

import cn.crtlprototypestudios.roadlights.Roadlights;
import cn.crtlprototypestudios.roadlights.client.hud.RoadlightsMinimap;
import cn.crtlprototypestudios.roadlights.client.config.RoadlightsConfig;
import cn.crtlprototypestudios.roadlights.event.RoadlightsConfigSaveEvent;
import cn.crtlprototypestudios.roadlights.event.WorldContainerBlockPlacementEvent;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.nbt.NbtCompound;
import org.lwjgl.glfw.GLFW;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class RoadlightsClient implements ClientModInitializer, ModMenuApi {

    private RoadlightsMinimap minimap;

    private static final KeyBinding PRINT_PLAYER_DATA = new KeyBinding("roadlights.key.debug.print_player_data", GLFW.GLFW_KEY_0, "roadlights.key.category");

    private static final List<String> CONTAINER_TYPES = Arrays.asList(
            "minecraft:chest",
            "minecraft:trapped_chest",
            "minecraft:barrel",
            "minecraft:shulker_box",
            "minecraft:ender_chest"
    );

    @Override
    public void onInitializeClient() {
        RoadlightsConfig.HANDLER.load();
        RoadlightsConfig.updateAllianceData();

        minimap = new RoadlightsMinimap();

        System.out.println("Enhanced Minimap Mod initialized!");

        HudRenderCallback.EVENT.register(minimap::renderMinimap);

        RoadlightsConfigSaveEvent.EVENT.register(RoadlightsConfig::updateAllianceData);

        KeyBindingHelper.registerKeyBinding(PRINT_PLAYER_DATA);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (PRINT_PLAYER_DATA.wasPressed()){
                ClientPlayerEntity player = MinecraftClient.getInstance().player;
                if(player != null)
                    Roadlights.LOGGER.info(String.valueOf(player.writeNbt(new NbtCompound())));
            }
        });
    }

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return RoadlightsConfig::createConfigScreen;
    }

}
