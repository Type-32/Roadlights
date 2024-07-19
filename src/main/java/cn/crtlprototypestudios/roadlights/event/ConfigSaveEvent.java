package cn.crtlprototypestudios.roadlights.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface ConfigSaveEvent {
    void onCallback();

    Event<ConfigSaveEvent> EVENT = EventFactory.createArrayBacked(ConfigSaveEvent.class,
            (listeners) -> () -> {
                for (ConfigSaveEvent listener : listeners) {
                    listener.onCallback();
                }
            }
    );
}
