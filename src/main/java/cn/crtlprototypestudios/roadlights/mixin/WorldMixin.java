package cn.crtlprototypestudios.roadlights.mixin;

import cn.crtlprototypestudios.roadlights.event.WorldContainerBlockPlacementEvent;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(World.class)
public class WorldMixin {
    @Inject(method = "setBlockState", at = @At("HEAD"))
    private void onSetBlockState(BlockPos pos, BlockState state, int flags, CallbackInfoReturnable<Boolean> cir) {
        World world = (World)(Object)this;
        if (world.isClient && state.hasBlockEntity()) {
            WorldContainerBlockPlacementEvent.EVENT.invoker().onCallback(world, pos, state);
        }
    }
}
