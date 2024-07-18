package cn.crtlprototypestudios.roadlights.client.config;

import cn.crtlprototypestudios.roadlights.client.config.utility.ConfigOptionsBuilder;
import cn.crtlprototypestudios.roadlights.utility.ResourceHelper;
import com.google.gson.GsonBuilder;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.ControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.autogen.AutoGen;
import dev.isxander.yacl3.config.v2.api.autogen.CustomName;
import dev.isxander.yacl3.config.v2.api.autogen.FormatTranslation;
import dev.isxander.yacl3.config.v2.api.autogen.IntSlider;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import dev.isxander.yacl3.impl.controller.IntegerSliderControllerBuilderImpl;
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

    @SerialEntry(comment = "Show the Blocks with the Identifiers on the minimap. e.g. 'minecraft:barrel'")
    public List<String> showBlocksWithId = new ArrayList<>();

    @SerialEntry(comment = "The relative map size according to UI Scale.")
//    @IntSlider(min = 64, max = 512, step = 64)
    public int mapSize = 128;

    public static RoadlightsConfig get() {
        return HANDLER.instance();
    }


    public static Screen createConfigScreen(Screen parent) {
        return YetAnotherConfigLib.createBuilder()
                .title(Text.translatable("roadlights.title"))
                .category(ConfigCategory.createBuilder()
                        .name(Text.translatable("roadlights.config.category.name"))
                        .option(ConfigOptionsBuilder.buildListOption(
                                Text.translatable("roadlights.config.option.show_blocks.name"),
                                Text.translatable("roadlights.config.option.show_blocks.desc"),
                                HANDLER.instance().showBlocksWithId,
                                () -> HANDLER.instance().showBlocksWithId,
                                newVal -> HANDLER.instance().showBlocksWithId = newVal,
                                StringControllerBuilder::create,
                                "minecraft:"))
                        .group(OptionGroup.createBuilder()
                                .name(Text.translatable("roadlights.config.group.general_config.name"))
                                .description(OptionDescription.of(Text.translatable("roadlights.config.option.show_blocks.desc")))
                                .option(ConfigOptionsBuilder.<Integer>buildOption(
                                        Text.translatable("roadlights.config.option.map_size.name"),
                                        Text.translatable("roadlights.config.option.map_size.desc"),
                                        HANDLER.instance().mapSize,
                                        () -> Integer.valueOf(HANDLER.instance().mapSize),
                                        newVal -> {HANDLER.instance().mapSize = newVal;},
                                        opt -> IntegerSliderControllerBuilder.create(opt).range(64, 512).step(64)
                                ))
                                .build()
                        ).build()
                ).build()
                .generateScreen(parent);
    }
}
