package cn.crtlprototypestudios.roadlights.client;

import cn.crtlprototypestudios.roadlights.client.hud.RoadlightsMinimap;
import cn.crtlprototypestudios.roadlights.client.config.RoadlightsConfig;
import cn.crtlprototypestudios.roadlights.event.WorldContainerBlockPlacementEvent;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class RoadlightsClient implements ClientModInitializer, ModMenuApi {

    private RoadlightsMinimap minimap;

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

        minimap = new RoadlightsMinimap();

        System.out.println("Enhanced Minimap Mod initialized!");

        HudRenderCallback.EVENT.register(minimap::renderMinimap);
//        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
//            containerCache.updateBlockAt(world, pos, world.getBlockState(pos));
//        });

//        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
//            if (world.isClient) {
//                BlockPos pos = hitResult.getBlockPos().offset(hitResult.getSide());
//                containerCache.refreshChunk(world, new ChunkPos(pos));
//            }
//            return ActionResult.PASS;
//        });

//        WorldEvents.BLOCK_BROKEN.register((world, pos, newState) -> {
//            if (world.isClient) {
//                containerCache.updateBlockAt(world, pos, newState);
//            }
//            return true;
//        });

        // Register custom event for block placement
    }

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return RoadlightsConfig::createConfigScreen;
    }

    @Override
    public void attachModpackBadges(Consumer<String> consumer) {
        ModMenuApi.super.attachModpackBadges(consumer);
    }

}
