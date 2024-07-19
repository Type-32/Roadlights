package cn.crtlprototypestudios.roadlights.client.hud;

import cn.crtlprototypestudios.roadlights.client.config.RoadlightsConfig;
import cn.crtlprototypestudios.roadlights.client.render.RenderDrawUtility;
import cn.crtlprototypestudios.roadlights.client.utility.ContainerCache;
import cn.crtlprototypestudios.roadlights.event.ConfigSaveEvent;
import cn.crtlprototypestudios.roadlights.event.WorldContainerBlockPlacementEvent;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.MapColor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class RoadlightsMinimap {

    public ContainerCache containerCache;
    public int
            tileSize = 2,
            renderDistance = 8 * 8; // 8 chunks

    public RoadlightsMinimap() {
        this(2, 8*8);
    }

    public RoadlightsMinimap(int tileSize, int renderDistance) {
        containerCache = new ContainerCache();
        this.tileSize = tileSize;
        this.renderDistance = renderDistance;

        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            containerCache.updateBlockAt(world, pos, world.getBlockState(pos));
        });

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
//            assert client.player != null;
            if(client.player != null)
                containerCache.clearAndRefreshCache(client.world, client.player);
        });

        ConfigSaveEvent.EVENT.register(() -> {
            if(MinecraftClient.getInstance().player != null)
                containerCache.clearAndRefreshCache(MinecraftClient.getInstance().world, MinecraftClient.getInstance().player);
        });

        WorldContainerBlockPlacementEvent.EVENT.register((world, pos, state) -> {
            if (world.isClient) {
                containerCache.updateBlockAt(world, pos, state);
            }
        });
    }

    public void renderMinimap(DrawContext drawContext, RenderTickCounter renderTickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity player = client.player;
        ClientWorld world = client.world;

        if (player == null || world == null) return;

        double uiScale = client.options.getGuiScale().getValue();
        uiScale = uiScale <= 0 ? 1 : uiScale * 0.5;
        int scaledMapSize = (int) (RoadlightsConfig.get().mapSize * uiScale);

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

    public void renderPlayerArrow(DrawContext drawContext, PlayerEntity player, int mapX, int mapY, int mapSize) {
        int arrowSize = Math.max(4, mapSize / 16); // Scale arrow size with map size

        int centerX = mapX + mapSize / 2;
        int centerY = mapY + mapSize / 2;
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
        RenderDrawUtility.drawTriangle(drawContext, xPoints, yPoints, 0xFFFFFFFF);
    }

    public void renderTerrain(DrawContext drawContext, ClientWorld world, PlayerEntity player, int mapX, int mapY) {
        int playerX = (int) player.getX();
        int playerZ = (int) player.getZ();

        for (int x = 0; x < RoadlightsConfig.get().mapSize / tileSize; x++) {
            for (int z = 0; z < RoadlightsConfig.get().mapSize / tileSize; z++) {
                int worldX = playerX + x - RoadlightsConfig.get().mapSize / (2 * tileSize);
                int worldZ = playerZ + z - RoadlightsConfig.get().mapSize / (2 * tileSize);
                BlockPos pos = new BlockPos(worldX, world.getTopY() - 1, worldZ);

                if (world.getChunkManager().isChunkLoaded(worldX >> 4, worldZ >> 4)) {
                    while (world.getBlockState(pos).isAir() && pos.getY() > world.getBottomY()) {
                        pos = pos.down();
                    }
                    Block block = world.getBlockState(pos).getBlock();
                    int color = getMapColor(block);
                    drawContext.fill(mapX + x * tileSize, mapY + z * tileSize,
                            mapX + (x + 1) * tileSize, mapY + (z + 1) * tileSize, color);
                }
            }
        }
    }

    public void renderEntities(DrawContext drawContext, ClientWorld world, PlayerEntity player, int mapX, int mapY) {
        List<LivingEntity> entities = world.getNonSpectatingEntities(LivingEntity.class, new Box(player.getPos().add(RoadlightsConfig.get().mapSize, 5, RoadlightsConfig.get().mapSize), player.getPos().add(-RoadlightsConfig.get().mapSize, -5, -RoadlightsConfig.get().mapSize)));
        for (LivingEntity entity : entities) {
//            if (entity.squaredDistanceTo(player) > renderDistance * renderDistance) continue;
            int entityX = (int) ((entity.getX() - player.getX()) / tileSize + (double) RoadlightsConfig.get().mapSize / (2 * tileSize));
            int entityZ = (int) ((entity.getZ() - player.getZ()) / tileSize + (double) RoadlightsConfig.get().mapSize / (2 * tileSize));

            if (entityX >= 0 && entityX < RoadlightsConfig.get().mapSize / tileSize && entityZ >= 0 && entityZ < RoadlightsConfig.get().mapSize / tileSize) {
//                if(entity instanceof ItemEntity) return;
                int color = getEntityColor(entity);
                drawContext.fill(mapX + entityX * tileSize, mapY + entityZ * tileSize,
                        mapX + (entityX + 1) * tileSize, mapY + (entityZ + 1) * tileSize, color);
            }
        }
    }

    public void renderCachedContainers(DrawContext drawContext, PlayerEntity player, World world, int mapX, int mapY, int mapSize) {
        int playerX = (int) player.getX();
        int playerZ = (int) player.getZ();

        for (Map.Entry<ChunkPos, List<BlockPos>> entry : containerCache.getContainers().entrySet()) {
            for (BlockPos pos : entry.getValue()) {
                int relativeX = pos.getX() - playerX;
                int relativeZ = pos.getZ() - playerZ;

                if (Math.abs(relativeX) <= containerCache.containerScanRadius && Math.abs(relativeZ) <= containerCache.containerScanRadius) {
                    int miniMapX = mapX + mapSize / 2 + (relativeX * mapSize / (2 * containerCache.containerScanRadius));
                    int miniMapZ = mapY + mapSize / 2 + (relativeZ * mapSize / (2 * containerCache.containerScanRadius));

                    BlockState state = world.getBlockState(pos);
                    int color = containerCache.getContainerColor(state);

                    int dotSize = Math.max(1, mapSize / 64); // Scale dot size with map size
                    drawContext.fill(miniMapX - dotSize/2, miniMapZ - dotSize/2,
                            miniMapX + dotSize/2 + 1, miniMapZ + dotSize/2 + 1, color);
                }
            }
        }
    }

    public int getMapColor(Block block) {
//        // This is a simplified version. You might want to use block.getDefaultMapColor() in a real implementation.
//        if (block == Blocks.WATER) return 0xFF0000FF;
//        if (block == Blocks.GRASS_BLOCK) return 0xFF00FF00;
//        if (block == Blocks.STONE) return 0xFF808080;
//        if (block == Blocks.SAND) return 0xFFFFFF00;
//        if (block == Blocks.OAK_LOG || block == Blocks.OAK_LEAVES) return 0xFF00FF00;
//        return 0xFF000000; // Default black for unknown blocks
        return MapColor.getRenderColor(block.getDefaultMapColor().color);
    }

    public int getEntityColor(Entity entity) {
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
