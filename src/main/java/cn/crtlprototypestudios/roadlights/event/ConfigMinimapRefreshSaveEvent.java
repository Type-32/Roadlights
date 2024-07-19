package cn.crtlprototypestudios.roadlights.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public interface ConfigMinimapRefreshSaveEvent {
    void onCallback();

    Event<ConfigMinimapRefreshSaveEvent> EVENT = EventFactory.createArrayBacked(ConfigMinimapRefreshSaveEvent.class,
            (listeners) -> () -> {
                for (ConfigMinimapRefreshSaveEvent listener : listeners) {
                    listener.onCallback();
                }
            }
    );
}
