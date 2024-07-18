package cn.crtlprototypestudios.roadlights.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
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
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static net.minecraft.client.session.telemetry.TelemetryEventProperty.RENDER_DISTANCE;

public class RoadlightsClient implements ClientModInitializer {

    private static final int MAP_SIZE = 128;
    private static final int TILE_SIZE = 2;
    private static final int RENDER_DISTANCE = 8 * 8; // 16 chunks
    private static final int CONTAINER_SCAN_RADIUS = 64; // Adjust as needed
    private ContainerCache containerCache;

    private static final List<String> CONTAINER_TYPES = Arrays.asList(
            "minecraft:chest",
            "minecraft:trapped_chest",
            "minecraft:barrel",
            "minecraft:shulker_box",
            "minecraft:ender_chest"
    );

    @Override
    public void onInitializeClient() {
        System.out.println("Enhanced Minimap Mod initialized!");
        containerCache = new ContainerCache();
        HudRenderCallback.EVENT.register(this::renderMinimap);

        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (world.isClient) {
                containerCache.updateBlockAt(world, pos, world.getBlockState(pos));
            }
        });

//        ServerWorldEvents.register((world, pos, oldState, newState) -> {
//            if (world.isClient) {
//                containerCache.updateBlockAt(world, pos, newState);
//            }
//            return true;
//        });
    }

    private void renderMinimap(DrawContext drawContext, RenderTickCounter renderTickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity player = client.player;
        ClientWorld world = client.world;

        if (player == null || world == null) return;

        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();
        int mapX = 10;
        int mapY = screenHeight - MAP_SIZE - 10;

        // Draw minimap background
        drawContext.fill(mapX, mapY, mapX + MAP_SIZE, mapY + MAP_SIZE, 0x80000000);

        // Render terrain
//        renderTerrain(drawContext, world, player, mapX, mapY);

        // Render entities
        renderEntities(drawContext, world, player, mapX, mapY);

        containerCache.update(world, (int)player.getX() >> 4, (int)player.getZ() >> 4);
        renderCachedContainers(drawContext, player, world, mapX, mapY);

        // Render player arrow
        renderPlayerArrow(drawContext, player, mapX, mapY);

        // Draw border
        drawContext.drawBorder(mapX, mapY, MAP_SIZE, MAP_SIZE, 0xFFFFFFFF);
    }

    private void renderPlayerArrow(DrawContext drawContext, PlayerEntity player, int mapX, int mapY) {
        int arrowSize = TILE_SIZE * 3;
        int centerX = mapX + MAP_SIZE / 2;
        int centerY = mapY + MAP_SIZE / 2;
        float yaw = -player.getYaw();

        // Convert yaw to radians
        float angle = (float) Math.toRadians(yaw);

        // Calculate arrow points
        int[] xPoints = new int[3];
        int[] yPoints = new int[3];

        // Tip of the arrow
        xPoints[0] = centerX + (int)(MathHelper.sin(angle) * arrowSize);
        yPoints[0] = centerY + (int)(MathHelper.cos(angle) * arrowSize);

        // Base points of the arrow
        float baseAngle1 = angle + (float) Math.toRadians(140);
        float baseAngle2 = angle - (float) Math.toRadians(140);

        xPoints[1] = centerX + (int)(MathHelper.sin(baseAngle1) * arrowSize / 2);
        yPoints[1] = centerY + (int)(MathHelper.cos(baseAngle1) * arrowSize / 2);

        xPoints[2] = centerX + (int)(MathHelper.sin(baseAngle2) * arrowSize / 2);
        yPoints[2] = centerY + (int)(MathHelper.cos(baseAngle2) * arrowSize / 2);

        // Draw the arrow
        drawArrow(drawContext, xPoints, yPoints, 0xFFFFFFFF);
    }

    private void drawArrow(DrawContext drawContext, int[] xPoints, int[] yPoints, int color) {
        // Draw the outline of the arrow
        drawLine(drawContext, xPoints[0], yPoints[0], xPoints[1], yPoints[1], color);
        drawLine(drawContext, xPoints[1], yPoints[1], xPoints[2], yPoints[2], color);
        drawLine(drawContext, xPoints[2], yPoints[2], xPoints[0], yPoints[0], color);

        // Fill the arrow
        fillTriangle(drawContext, xPoints, yPoints, color);
    }

    private void drawLine(DrawContext drawContext, int x1, int y1, int x2, int y2, int color) {
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);

        if (dx >= dy) {
            // More horizontal
            if (x1 > x2) {
                int temp = x1; x1 = x2; x2 = temp;
                temp = y1; y1 = y2; y2 = temp;
            }
            for (int x = x1; x <= x2; x++) {
                int y = y1 + (x - x1) * (y2 - y1) / (x2 - x1);
                drawContext.drawVerticalLine(x, y, y, color);
            }
        } else {
            // More vertical
            if (y1 > y2) {
                int temp = x1; x1 = x2; x2 = temp;
                temp = y1; y1 = y2; y2 = temp;
            }
            for (int y = y1; y <= y2; y++) {
                int x = x1 + (y - y1) * (x2 - x1) / (y2 - y1);
                drawContext.drawHorizontalLine(x, x, y, color);
            }
        }
    }

    private void fillTriangle(DrawContext drawContext, int[] xPoints, int[] yPoints, int color) {
        int minY = Math.min(Math.min(yPoints[0], yPoints[1]), yPoints[2]);
        int maxY = Math.max(Math.max(yPoints[0], yPoints[1]), yPoints[2]);

        for (int y = minY; y <= maxY; y++) {
            int x1 = Integer.MAX_VALUE;
            int x2 = Integer.MIN_VALUE;

            for (int i = 0; i < 3; i++) {
                int j = (i + 1) % 3;
                if ((yPoints[i] <= y && yPoints[j] > y) || (yPoints[j] <= y && yPoints[i] > y)) {
                    int x = xPoints[i] + (y - yPoints[i]) * (xPoints[j] - xPoints[i]) / (yPoints[j] - yPoints[i]);
                    x1 = Math.min(x1, x);
                    x2 = Math.max(x2, x);
                }
            }

            if (x1 <= x2) {
                drawContext.drawHorizontalLine(x1, x2, y, color);
            }
        }
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

    private void renderCachedContainers(DrawContext drawContext, PlayerEntity player, World world, int mapX, int mapY) {
        int playerX = (int) player.getX();
        int playerZ = (int) player.getZ();

        for (Map.Entry<ChunkPos, List<BlockPos>> entry : containerCache.getContainers().entrySet()) {
            for (BlockPos pos : entry.getValue()) {
                int relativeX = pos.getX() - playerX;
                int relativeZ = pos.getZ() - playerZ;

                if (Math.abs(relativeX) <= CONTAINER_SCAN_RADIUS && Math.abs(relativeZ) <= CONTAINER_SCAN_RADIUS) {
                    int miniMapX = mapX + MAP_SIZE / 2 + (relativeX * MAP_SIZE / (2 * CONTAINER_SCAN_RADIUS));
                    int miniMapZ = mapY + MAP_SIZE / 2 + (relativeZ * MAP_SIZE / (2 * CONTAINER_SCAN_RADIUS));

                    BlockState state = world.getBlockState(pos);
                    String blockId = Registries.BLOCK.getId(state.getBlock()).toString();
                    int color = getContainerColor(blockId);

                    drawContext.fill(miniMapX - 1, miniMapZ - 1, miniMapX + 2, miniMapZ + 2, color);
                }
            }
        }
    }

    private boolean isContainerEmpty(World world, BlockPos pos) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof Inventory) {
            Inventory inventory = (Inventory) blockEntity;
            for (int i = 0; i < inventory.size(); i++) {
                if (!inventory.getStack(i).isEmpty()) {
                    return false;
                }
            }
            return true;
        }
        return false; // If it's not an inventory, we can't determine if it's empty
    }

    private int getContainerColor(String blockId) {
        switch (blockId) {
            case "minecraft:chest":
            case "minecraft:trapped_chest":
                return 0xFFFFAA00; // Orange for chests
            case "minecraft:barrel":
                return 0xFF964B00; // Brown for barrels
            case "minecraft:shulker_box":
                return 0xFFFF00FF; // Magenta for shulker boxes
            case "minecraft:ender_chest":
                return 0xFF00FFFF; // Cyan for ender chests
            default:
                return 0xFFFFFFFF; // White for unknown containers
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
        // This is a simplified version. You might want to use block.getDefaultMapColor() in a real implementation.
        if (block == Blocks.WATER) return 0xFF0000FF;
        if (block == Blocks.GRASS_BLOCK) return 0xFF00FF00;
        if (block == Blocks.STONE) return 0xFF808080;
        if (block == Blocks.SAND) return 0xFFFFFF00;
        if (block == Blocks.OAK_LOG || block == Blocks.OAK_LEAVES) return 0xFF00FF00;
        return 0xFF000000; // Default black for unknown blocks
//        return block.getDefaultMapColor().color;
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

    private static class ContainerCache {
        private final Map<ChunkPos, List<BlockPos>> containers = new ConcurrentHashMap<>();
        private final List<ChunkPos> scannedChunks = new ArrayList<>();
        private static final int CACHE_RADIUS = 5; // Cache a 11x11 chunk area

        public void update(World world, int centerChunkX, int centerChunkZ) {
            List<ChunkPos> currentChunks = new ArrayList<>();

            for (int x = centerChunkX - CACHE_RADIUS; x <= centerChunkX + CACHE_RADIUS; x++) {
                for (int z = centerChunkZ - CACHE_RADIUS; z <= centerChunkZ + CACHE_RADIUS; z++) {
                    ChunkPos pos = new ChunkPos(x, z);
                    currentChunks.add(pos);

                    if (!scannedChunks.contains(pos)) {
                        scanChunk(world, pos);
                        scannedChunks.add(pos);
                    }
                }
            }

            // Remove chunks that are no longer in range
            scannedChunks.removeIf(pos -> !currentChunks.contains(pos));
            containers.keySet().removeIf(pos -> !currentChunks.contains(pos));
        }

        private void scanChunk(World world, ChunkPos pos) {
            Chunk chunk = world.getChunk(pos.x, pos.z, ChunkStatus.FULL, false);
            if (chunk == null) return;

            List<BlockPos> chunkContainers = new ArrayList<>();

            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    for (int y = world.getBottomY(); y < world.getTopY(); y++) {
                        BlockPos blockPos = new BlockPos(pos.x * 16 + x, y, pos.z * 16 + z);
                        BlockState state = chunk.getBlockState(blockPos);
                        if (isContainer(state)) {
                            chunkContainers.add(blockPos);
                        }
                    }
                }
            }

            if (!chunkContainers.isEmpty()) {
                containers.put(pos, chunkContainers);
            }
        }

        public void updateBlockAt(World world, BlockPos pos, BlockState newState) {
            ChunkPos chunkPos = new ChunkPos(pos);
            List<BlockPos> chunkContainers = containers.get(chunkPos);

            if (chunkContainers == null) {
                if (isContainer(newState)) {
                    chunkContainers = new ArrayList<>();
                    chunkContainers.add(pos);
                    containers.put(chunkPos, chunkContainers);
                }
            } else {
                if (isContainer(newState)) {
                    if (!chunkContainers.contains(pos)) {
                        chunkContainers.add(pos);
                    }
                } else {
                    chunkContainers.remove(pos);
                    if (chunkContainers.isEmpty()) {
                        containers.remove(chunkPos);
                    }
                }
            }
        }

        private boolean isContainer(BlockState state) {
            String blockId = Registries.BLOCK.getId(state.getBlock()).toString();
            return CONTAINER_TYPES.contains(blockId);
        }

        public Map<ChunkPos, List<BlockPos>> getContainers() {
            return containers;
        }
    }
}
