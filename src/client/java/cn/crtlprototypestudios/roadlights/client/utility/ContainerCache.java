package cn.crtlprototypestudios.roadlights.client.utility;

import cn.crtlprototypestudios.roadlights.client.config.RoadlightsConfig;
import net.minecraft.block.BlockState;
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
        List<BlockPos> chunkContainers = containers.computeIfAbsent(chunkPos, k -> new ArrayList<>());

        if (isContainer(newState)) {
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

    public static boolean isContainer(BlockState state) {
        String blockId = Registries.BLOCK.getId(state.getBlock()).toString();
        return RoadlightsConfig.HANDLER.instance().showBlocksWithId.contains(blockId);
    }

    public Map<ChunkPos, List<BlockPos>> getContainers() {
        return containers;
    }
}
