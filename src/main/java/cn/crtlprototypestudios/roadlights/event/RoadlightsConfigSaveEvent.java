package cn.crtlprototypestudios.roadlights.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public interface RoadlightsConfigSaveEvent {
    void onCallback();

    Event<RoadlightsConfigSaveEvent> EVENT = EventFactory.createArrayBacked(RoadlightsConfigSaveEvent.class,
            (listeners) -> () -> {
                for (RoadlightsConfigSaveEvent listener : listeners) {
                    listener.onCallback();
                }
            }
    );
}
