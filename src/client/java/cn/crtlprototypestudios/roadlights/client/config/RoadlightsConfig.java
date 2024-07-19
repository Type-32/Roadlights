package cn.crtlprototypestudios.roadlights.client.config;

import cn.crtlprototypestudios.roadlights.client.config.utility.ConfigOptionsBuilder;
import cn.crtlprototypestudios.roadlights.config.data.AllianceData;
import cn.crtlprototypestudios.roadlights.event.ConfigMinimapRefreshSaveEvent;
import cn.crtlprototypestudios.roadlights.event.RoadlightsConfigSaveEvent;
import cn.crtlprototypestudios.roadlights.utility.ResourceHelper;
import com.google.gson.GsonBuilder;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.autogen.IntSlider;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class RoadlightsConfig {
    public static ConfigClassHandler<RoadlightsConfig> HANDLER = ConfigClassHandler.<RoadlightsConfig>createBuilder(RoadlightsConfig.class)
            .id(ResourceHelper.find("roadlights_config"))
            .serializer(config -> GsonConfigSerializerBuilder.create(config)
                    .setPath(FabricLoader.getInstance().getConfigDir().resolve(ResourceHelper.CONFIG_FILE))
                    .appendGsonBuilder(GsonBuilder::setPrettyPrinting)
                    .setJson5(true)
                    .build())
            .build();

    @SerialEntry
    public List<String> showBlocksWithId = new ArrayList<>();

    @SerialEntry
    public List<String> alliedPlayers = new ArrayList<>();

    @SerialEntry
    @IntSlider(min = 64, max = 512, step = 64)
    public int mapSize = 128;

    public static RoadlightsConfig get() {
        return HANDLER.instance();
    }

    public static void updateAllianceData() {
        List<String> alliedPlayers = get().alliedPlayers; // Assuming this method exists
        AllianceData.setAlliedPlayers(alliedPlayers);
    }

    public static Screen createConfigScreen(Screen parent) {
        return YetAnotherConfigLib.createBuilder()
                .title(Text.translatable("roadlights.title"))
                .category(ConfigCategory.createBuilder()
                        .name(Text.translatable("roadlights.config.category.minimap.name"))
                        .option(ConfigOptionsBuilder.buildListOption(
                                Text.translatable("roadlights.config.option.show_blocks.name"),
                                Text.translatable("roadlights.config.option.show_blocks.desc"),
                                get().showBlocksWithId,
                                () -> get().showBlocksWithId,
                                newVal -> {
                                    get().showBlocksWithId = newVal;
                                    HANDLER.save();
                                    ConfigMinimapRefreshSaveEvent.EVENT.invoker().onCallback();
                                    RoadlightsConfigSaveEvent.EVENT.invoker().onCallback();
                                },
                                StringControllerBuilder::create,
                                "minecraft:"))
                        .group(OptionGroup.createBuilder()
                                .name(Text.translatable("roadlights.config.group.general_config.name"))
                                .description(OptionDescription.of(Text.translatable("roadlights.config.option.show_blocks.desc")))
                                .option(ConfigOptionsBuilder.<Integer>buildOption(
                                        Text.translatable("roadlights.config.option.map_size.name"),
                                        Text.translatable("roadlights.config.option.map_size.desc"),
                                        get().mapSize,
                                        () -> Integer.valueOf(get().mapSize),
                                        newVal -> {
                                            get().mapSize = newVal;
                                            HANDLER.save();
                                            RoadlightsConfigSaveEvent.EVENT.invoker().onCallback();
                                        },
                                        opt -> IntegerSliderControllerBuilder.create(opt).range(64, 512).step(64)
                                )).build()
                        ).build()
                )
                .category(ConfigCategory.createBuilder()
                        .name(Text.translatable("roadlights.config.category.alliance.name"))
                        .option(ConfigOptionsBuilder.buildListOption(
                                Text.translatable("roadlights.config.option.allies.name"),
                                Text.translatable("roadlights.config.option.allies.desc"),
                                get().alliedPlayers,
                                () -> get().alliedPlayers,
                                newVal -> {
                                    get().alliedPlayers = newVal;
                                    HANDLER.save();
                                    RoadlightsConfigSaveEvent.EVENT.invoker().onCallback();
                                },
                                StringControllerBuilder::create,
                                "")).build()
                )
                .build()
                .generateScreen(parent);
    }
}
