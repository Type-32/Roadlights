package cn.crtlprototypestudios.roadlights.client.mixin;

import cn.crtlprototypestudios.roadlights.Roadlights;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.network.ClientLoginNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.List;

@Mixin(FabricLoader.class)
public class ClientModListMixin {
    @Inject(method = "getAllMods", at = @At("RETURN"), cancellable = true)
    private void getAllMods(CallbackInfoReturnable<List<ModContainer>> cir) {
        cir.setReturnValue(Roadlights.getFilteredModList());
    }
}
