package cn.crtlprototypestudios.roadlights.client.config.utility;

import cn.crtlprototypestudios.roadlights.client.config.RoadlightsConfig;
import dev.isxander.yacl3.api.ListOption;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.controller.ControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import net.minecraft.text.Text;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ConfigOptionsBuilder {

    public static <T> ListOption<T> buildListOption(Text optionName, List<T> optionValues, Supplier<List<T>> getter, Consumer<List<T>> setter, Function<Option<T>, ControllerBuilder<T>> controller, T initialValue){
        return ListOption.<T>createBuilder()
                .name(optionName)
                .binding(optionValues, getter, setter)
                .initial(initialValue)
                .controller(controller)
                .build();
    }

    public static <T> ListOption<T> buildListOption(Text optionName, Text optionDesc, List<T> optionValues, Supplier<List<T>> getter, Consumer<List<T>> setter, Function<Option<T>, ControllerBuilder<T>> controller, T initialValue){
        return ListOption.<T>createBuilder()
                .name(optionName)
                .description(OptionDescription.of(optionDesc))
                .binding(optionValues, getter, setter)
                .initial(initialValue)
                .controller(controller)
                .build();
    }

    public static <T> Option<T> buildOption(Text optionName, T actualValue, Supplier<T> getter, Consumer<T> setter, Function<Option<T>, ControllerBuilder<T>> controller){
        return Option.<T>createBuilder()
                .name(optionName)
                .binding(actualValue, getter, setter)
                .controller(controller)
                .build();
    }

    public static <T> Option<T> buildOption(Text optionName, Text optionDesc, T actualValue, Supplier<T> getter, Consumer<T> setter, Function<Option<T>, ControllerBuilder<T>> controller){
        return Option.<T>createBuilder()
                .name(optionName)
                .description(OptionDescription.of(optionDesc))
                .binding(actualValue, getter, setter)
                .controller(controller)
                .build();
    }
}
