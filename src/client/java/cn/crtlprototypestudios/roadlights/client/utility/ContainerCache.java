package cn.crtlprototypestudios.roadlights.client.utility;

import cn.crtlprototypestudios.roadlights.client.config.RoadlightsConfig;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.InventoryProvider;
import net.minecraft.block.MapColor;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ContainerCache {
    public int
            cacheRadius = 5, // Cache a 5x5 chunk area
            containerScanRadius = 64; // Scans a 64x64 block area with the player as the center

    private final Map<ChunkPos, List<BlockPos>> containers = new ConcurrentHashMap<>();
    private final List<ChunkPos> scannedChunks = new ArrayList<>();

    public ContainerCache(int cacheRadius, int containerScanRadius){
        this.cacheRadius = cacheRadius;
        this.containerScanRadius = containerScanRadius;
    }

    public ContainerCache(){
        this(5, 64);
    }

    public void update(World world, int centerChunkX, int centerChunkZ) {
        List<ChunkPos> currentChunks = new ArrayList<>();

        for (int x = centerChunkX - cacheRadius; x <= centerChunkX + cacheRadius; x++) {
            for (int z = centerChunkZ - cacheRadius; z <= centerChunkZ + cacheRadius; z++) {
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
                    if (isConfiguredContainer(state)) {
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
        List<BlockPos> chunkContainers = containers.computeIfAbsent(chunkPos, k -> new ArrayList<>());

        if (isConfiguredContainer(newState)) {
            if (!chunkContainers.contains(pos)) {
                chunkContainers.add(pos);
                System.out.println("Added new container at " + pos);
            }
        } else {
            if (chunkContainers.remove(pos)) {
                System.out.println("Removed container at " + pos);
                if (chunkContainers.isEmpty()) {
                    containers.remove(chunkPos);
                }
            }
        }
    }

    public void refreshChunk(World world, ChunkPos pos) {
        scanChunk(world, pos);
        System.out.println("Refreshed chunk at " + pos);
    }

    public static boolean isConfiguredContainer(BlockState state) {
        String blockId = Registries.BLOCK.getId(state.getBlock()).toString();
        return RoadlightsConfig.HANDLER.instance().showBlocksWithId.contains(blockId);
    }

    public Map<ChunkPos, List<BlockPos>> getContainers() {
        return containers;
    }

    public boolean isContainerEmpty(World world, BlockPos pos) {
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

    public void updateNearbyChunks(World world, PlayerEntity player) {
        int playerChunkX = (int)player.getX() >> 4;
        int playerChunkZ = (int)player.getZ() >> 4;

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                ChunkPos chunkPos = new ChunkPos(playerChunkX + dx, playerChunkZ + dz);
                refreshChunk(world, chunkPos);
            }
        }
        System.out.println("Updated Nearby Chunks.");
    }

    public int getContainerColor(BlockState state) {
        if(ContainerCache.isConfiguredContainer(state)){
            String blockId = Registries.BLOCK.getId(state.getBlock()).toString();
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

    public void scanChunkForContainers(ClientWorld world, int chunkX, int chunkZ, int centerX, int centerZ, List<BlockPos> containers) {
        int startX = chunkX << 4;
        int startZ = chunkZ << 4;

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = startX + x;
                int worldZ = startZ + z;

                if (Math.abs(worldX - centerX) <= containerScanRadius && Math.abs(worldZ - centerZ) <= containerScanRadius) {
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

    public void clearAndRefreshCache(World world, PlayerEntity player) {
        // Clear existing cache
        containers.clear();
        scannedChunks.clear();

        // Get player's current chunk
        int playerChunkX = (int)player.getX() >> 4;
        int playerChunkZ = (int)player.getZ() >> 4;

        // Scan chunks in the cache radius
        for (int x = playerChunkX - cacheRadius; x <= playerChunkX + cacheRadius; x++) {
            for (int z = playerChunkZ - cacheRadius; z <= playerChunkZ + cacheRadius; z++) {
                ChunkPos pos = new ChunkPos(x, z);
                scanChunk(world, pos);
                scannedChunks.add(pos);
            }
        }

        System.out.println("Cache cleared and refreshed. Scanned " + scannedChunks.size() + " chunks.");
    }
}
