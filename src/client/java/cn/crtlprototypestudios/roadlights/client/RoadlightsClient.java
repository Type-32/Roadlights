package cn.crtlprototypestudios.roadlights.client;

import cn.crtlprototypestudios.roadlights.client.render.RenderDrawUtility;
import cn.crtlprototypestudios.roadlights.client.utility.ContainerCache;
import cn.crtlprototypestudios.roadlights.client.config.RoadlightsConfig;
import cn.crtlprototypestudios.roadlights.event.WorldContainerBlockPlacementEvent;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class RoadlightsClient implements ClientModInitializer, ModMenuApi {

    private static final int MAP_SIZE = 128;
    private static final int TILE_SIZE = 2;
    private static final int RENDER_DISTANCE = 8 * 8; // 16 chunks
    private static final int CONTAINER_SCAN_RADIUS = 64; // Adjust as needed
    private ContainerCache containerCache;
    private int tickCounter = 0;
    private static final int UPDATE_INTERVAL = 20; // Update every 20 ticks (1 second)

    private boolean doRefresh = false;

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
//        RoadlightsConfig.

        System.out.println("Enhanced Minimap Mod initialized!");
        containerCache = new ContainerCache();
        HudRenderCallback.EVENT.register(this::renderMinimap);

        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            containerCache.updateBlockAt(world, pos, world.getBlockState(pos));
        });

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
        WorldContainerBlockPlacementEvent.EVENT.register((world, pos, state) -> {
            if (world.isClient) {
                containerCache.updateBlockAt(world, pos, state);
            }
        });
    }

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return RoadlightsConfig::createConfigScreen;
    }

    @Override
    public void attachModpackBadges(Consumer<String> consumer) {
        ModMenuApi.super.attachModpackBadges(consumer);
    }

    private void renderMinimap(DrawContext drawContext, RenderTickCounter renderTickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity player = client.player;
        ClientWorld world = client.world;

        if (player == null || world == null) return;

        double uiScale = client.options.getGuiScale().getValue();
        uiScale = uiScale <= 0 ? 1 : uiScale * 0.5;
        int scaledMapSize = (int) (MAP_SIZE * uiScale);

        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();
        int mapX = 10;
        int mapY = screenHeight - scaledMapSize - 10;

        // Draw minimap background
        drawContext.fill(mapX, mapY, mapX + scaledMapSize, mapY + scaledMapSize, 0x80000000);

        // Render terrain
//        renderTerrain(drawContext, world, player, mapX, mapY);

        renderEntities(drawContext, world, player, mapX, mapY);

        CompletableFuture.runAsync(() -> {
            containerCache.update(world, (int)player.getX() >> 4, (int)player.getZ() >> 4);
        });

        renderCachedContainers(drawContext, player, world, mapX, mapY, scaledMapSize);

        renderPlayerArrow(drawContext, player, mapX, mapY, scaledMapSize);

        // Draw border
        int borderWidth = (int) (1 * uiScale);
        drawContext.fill(mapX, mapY, mapX + scaledMapSize, mapY + borderWidth, 0xFFFFFFFF); // Top
        drawContext.fill(mapX, mapY + scaledMapSize - borderWidth, mapX + scaledMapSize, mapY + scaledMapSize, 0xFFFFFFFF); // Bottom
        drawContext.fill(mapX, mapY, mapX + borderWidth, mapY + scaledMapSize, 0xFFFFFFFF); // Left
        drawContext.fill(mapX + scaledMapSize - borderWidth, mapY, mapX + scaledMapSize, mapY + scaledMapSize, 0xFFFFFFFF); // Right
    }

    private void renderPlayerArrow(DrawContext drawContext, PlayerEntity player, int mapX, int mapY, int mapSize) {
        float yaw = -player.getYaw();
        int arrowSize = Math.max(4, mapSize / 16); // Scale arrow size with map size

        int centerX = mapX + mapSize / 2;
        int centerY = mapY + mapSize / 2;

        // Calculate arrow points
        int[] xPoints = new int[3];
        int[] yPoints = new int[3];

        // Convert yaw to radians and adjust for Minecraft's coordinate system
        float angle = (float) Math.toRadians(yaw);

        // Tip of the arrow
        xPoints[0] = centerX + (int)(MathHelper.sin(angle) * arrowSize);
        yPoints[0] = centerY - (int)(MathHelper.cos(angle) * arrowSize);

        // Base points of the arrow
        float baseAngle1 = angle + (float) Math.toRadians(140);
        float baseAngle2 = angle - (float) Math.toRadians(140);

        xPoints[1] = centerX + (int)(MathHelper.sin(baseAngle1) * arrowSize / 2);
        yPoints[1] = centerY + (int)(MathHelper.cos(baseAngle1) * arrowSize / 2);

        xPoints[2] = centerX + (int)(MathHelper.sin(baseAngle2) * arrowSize / 2);
        yPoints[2] = centerY + (int)(MathHelper.cos(baseAngle2) * arrowSize / 2);

        // Draw the arrow
        RenderDrawUtility.drawTriangle(drawContext, xPoints, yPoints, 0xFFFFFFFF);
    }



    private void renderTerrain(DrawContext drawContext, ClientWorld world, PlayerEntity player, int mapX, int mapY) {
        int playerX = (int) player.getX();
        int playerZ = (int) player.getZ();

        for (int x = 0; x < MAP_SIZE / TILE_SIZE; x++) {
            for (int z = 0; z < MAP_SIZE / TILE_SIZE; z++) {
                int worldX = playerX + x - MAP_SIZE / (2 * TILE_SIZE);
                int worldZ = playerZ + z - MAP_SIZE / (2 * TILE_SIZE);
                BlockPos pos = new BlockPos(worldX, world.getTopY() - 1, worldZ);

                if (world.getChunkManager().isChunkLoaded(worldX >> 4, worldZ >> 4)) {
                    while (world.getBlockState(pos).isAir() && pos.getY() > world.getBottomY()) {
                        pos = pos.down();
                    }
                    Block block = world.getBlockState(pos).getBlock();
                    int color = getMapColor(block);
                    drawContext.fill(mapX + x * TILE_SIZE, mapY + z * TILE_SIZE,
                            mapX + (x + 1) * TILE_SIZE, mapY + (z + 1) * TILE_SIZE, color);
                }
            }
        }
    }

    private void renderEntities(DrawContext drawContext, ClientWorld world, PlayerEntity player, int mapX, int mapY) {
        List<Entity> entities = world.getNonSpectatingEntities(Entity.class, new Box(player.getX() + 100, player.getY() + 10, player.getZ() + 100, player.getX() - 100, player.getY() - 10, player.getZ() - 100));
        for (Entity entity : entities) {
            if (entity.squaredDistanceTo(player) <= RENDER_DISTANCE * 6) {
                int entityX = (int) ((entity.getX() - player.getX()) / TILE_SIZE + (double) MAP_SIZE / (2 * TILE_SIZE));
                int entityZ = (int) ((entity.getZ() - player.getZ()) / TILE_SIZE + (double) MAP_SIZE / (2 * TILE_SIZE));

                if (entityX >= 0 && entityX < MAP_SIZE / TILE_SIZE && entityZ >= 0 && entityZ < MAP_SIZE / TILE_SIZE) {
                    if(entity instanceof ItemEntity) return;
                    int color = getEntityColor(entity);
                    drawContext.fill(mapX + entityX * TILE_SIZE, mapY + entityZ * TILE_SIZE,
                            mapX + (entityX + 1) * TILE_SIZE, mapY + (entityZ + 1) * TILE_SIZE, color);
                }
            }
        }
    }

    private void renderCachedContainers(DrawContext drawContext, PlayerEntity player, World world, int mapX, int mapY, int mapSize) {
        int playerX = (int) player.getX();
        int playerZ = (int) player.getZ();

        for (Map.Entry<ChunkPos, List<BlockPos>> entry : containerCache.getContainers().entrySet()) {
            for (BlockPos pos : entry.getValue()) {
                int relativeX = pos.getX() - playerX;
                int relativeZ = pos.getZ() - playerZ;

                if (Math.abs(relativeX) <= CONTAINER_SCAN_RADIUS && Math.abs(relativeZ) <= CONTAINER_SCAN_RADIUS) {
                    int miniMapX = mapX + mapSize / 2 + (relativeX * mapSize / (2 * CONTAINER_SCAN_RADIUS));
                    int miniMapZ = mapY + mapSize / 2 + (relativeZ * mapSize / (2 * CONTAINER_SCAN_RADIUS));

                    BlockState state = world.getBlockState(pos);
                    int color = getContainerColor(state);

                    int dotSize = Math.max(1, mapSize / 64); // Scale dot size with map size
                    drawContext.fill(miniMapX - dotSize/2, miniMapZ - dotSize/2,
                            miniMapX + dotSize/2 + 1, miniMapZ + dotSize/2 + 1, color);
                }
            }
        }
    }

    private boolean isContainerEmpty(World world, BlockPos pos) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof InventoryProvider) {
            InventoryProvider invProvider = (InventoryProvider) blockEntity;
            Inventory inv = invProvider.getInventory(blockEntity.getCachedState(), world, pos);
            for (int i = 0; i < inv.size(); i++) {
                if (!inv.getStack(i).isEmpty()) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private void updateNearbyChunks(World world, PlayerEntity player) {
        int playerChunkX = (int)player.getX() >> 4;
        int playerChunkZ = (int)player.getZ() >> 4;

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                ChunkPos chunkPos = new ChunkPos(playerChunkX + dx, playerChunkZ + dz);
                containerCache.refreshChunk(world, chunkPos);
            }
        }
        System.out.println("Updated Nearby Chunks.");
    }

    private int getContainerColor(BlockState state) {
        if(ContainerCache.isContainer(state)){
            return 0xFF00FFFF;
        } else {
            return MapColor.getRenderColor(state.getBlock().getDefaultMapColor().color);
        }
    }


//    private List<BlockPos> scanForContainers(ClientWorld world, int centerX, int centerZ) {
//        List<BlockPos> containers = new ArrayList<>();
//
//        int chunkRadius = (CONTAINER_SCAN_RADIUS >> 4) + 1;
//        ChunkPos centerChunk = new ChunkPos(new BlockPos(centerX, 0, centerZ));
//
//        for (int chunkX = centerChunk.x - chunkRadius; chunkX <= centerChunk.x + chunkRadius; chunkX++) {
//            for (int chunkZ = centerChunk.z - chunkRadius; chunkZ <= centerChunk.z + chunkRadius; chunkZ++) {
//                if (world.getChunkManager().isChunkLoaded(chunkX, chunkZ)) {
//                    scanChunkForContainers(world, chunkX, chunkZ, centerX, centerZ, containers);
//                }
//            }
//        }
//
//        return containers;
//    }

    private void scanChunkForContainers(ClientWorld world, int chunkX, int chunkZ, int centerX, int centerZ, List<BlockPos> containers) {
        int startX = chunkX << 4;
        int startZ = chunkZ << 4;

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = startX + x;
                int worldZ = startZ + z;

                if (Math.abs(worldX - centerX) <= CONTAINER_SCAN_RADIUS && Math.abs(worldZ - centerZ) <= CONTAINER_SCAN_RADIUS) {
                    for (int y = world.getBottomY(); y < world.getTopY(); y++) {
                        BlockPos pos = new BlockPos(worldX, y, worldZ);
                        BlockState state = world.getBlockState(pos);
                        if (state.getBlock() == Blocks.CHEST || state.getBlock() == Blocks.TRAPPED_CHEST ||
                                state.getBlock() == Blocks.BARREL) {
                            containers.add(pos);
                        }
                    }
                }
            }
        }
    }

    private int getMapColor(Block block) {
//        // This is a simplified version. You might want to use block.getDefaultMapColor() in a real implementation.
//        if (block == Blocks.WATER) return 0xFF0000FF;
//        if (block == Blocks.GRASS_BLOCK) return 0xFF00FF00;
//        if (block == Blocks.STONE) return 0xFF808080;
//        if (block == Blocks.SAND) return 0xFFFFFF00;
//        if (block == Blocks.OAK_LOG || block == Blocks.OAK_LEAVES) return 0xFF00FF00;
//        return 0xFF000000; // Default black for unknown blocks
        return MapColor.getRenderColor(block.getDefaultMapColor().color);
    }

    private int getEntityColor(Entity entity) {
        if (entity instanceof PlayerEntity) {
            return 0xFFFFFFFF;
        } else if (entity instanceof HostileEntity) {
            return 0xFFFF0000;
        } else if (entity instanceof AnimalEntity) {
            return 0xFF00FF00;
        }
        return 0;
    }

}
