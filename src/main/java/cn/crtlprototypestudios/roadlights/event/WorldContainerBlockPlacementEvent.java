package cn.crtlprototypestudios.roadlights.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface WorldContainerBlockPlacementEvent {
    void onCallback(World world, BlockPos pos, BlockState state);

    Event<WorldContainerBlockPlacementEvent> EVENT = EventFactory.createArrayBacked(WorldContainerBlockPlacementEvent.class,
            (listeners) -> (world, pos, state) -> {
                for (WorldContainerBlockPlacementEvent listener : listeners) {
                    listener.onCallback(world, pos, state);
                }
            }
    );
}
