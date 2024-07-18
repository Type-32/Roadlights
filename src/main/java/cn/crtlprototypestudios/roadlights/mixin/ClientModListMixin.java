package cn.crtlprototypestudios.roadlights.mixin;

import cn.crtlprototypestudios.roadlights.Roadlights;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
//import net.minecraft.client.network.ClientLoginNetworkHandler;
import net.fabricmc.loader.impl.FabricLoaderImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.List;

@Mixin(value = FabricLoaderImpl.class, remap = false)
public class ClientModListMixin {
//    @Inject(method = "getAllMods", at = @At("RETURN"), cancellable = true, remap = false)
////    @Overwrite(remap = false)
//    private void getAllMods(CallbackInfoReturnable<List<ModContainer>> cir) {
//        // This is in a current unworking state. Since Fabric loaders doesn't have obfuscation mappings, they can't be injected with Mixin.
//        // Will post workaround soon.
//        cir.setReturnValue(Roadlights.getFilteredModList());
//    }

//    /**
//     * @author
//     * @reason
//     */
//    @Overwrite(remap = false)
//    public Collection<ModContainer> getAllMods() {
//        // Potential Workaround.
//        return (Roadlights.getFilteredModList());
//    }
}
