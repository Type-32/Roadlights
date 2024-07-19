package cn.crtlprototypestudios.roadlights.mixin;

import cn.crtlprototypestudios.roadlights.Roadlights;
import cn.crtlprototypestudios.roadlights.config.data.AllianceData;
import com.mojang.authlib.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.message.MessageType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {

    @Shadow public abstract boolean isPlayer();

    @Shadow public abstract Text getName();

    @Shadow public abstract boolean isMainPlayer();

    @Shadow public abstract void sendMessage(Text message, boolean overlay);

    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void onAttack(Entity target, CallbackInfo ci) {
        if (target instanceof PlayerEntity targetPlayer && this.isPlayer() && this.isMainPlayer()) {
            if (AllianceData.isAllied(targetPlayer.getName().getString())) {
                Roadlights.LOGGER.info("Player tried to attack an ally.");
                ((PlayerEntity)(Object)this).sendMessage(Text.translatable("roadlights.msg.alliance.no_hit_ally", target.getName().getString()), true);
                ci.cancel();
            }
        }
    }
}
