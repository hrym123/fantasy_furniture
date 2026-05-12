package org.lanye.fantasy_furniture.core.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.HitResult;
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlate6CrosshairPick;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftBedPlate6CrosshairMixin {

    @Inject(method = "tick", at = @At("RETURN"))
    private void fantasy_furniture$recordCrosshairForBedPick(CallbackInfo ci) {
        Minecraft self = (Minecraft) (Object) this;
        HitResult hit = self.hitResult;
        BedPlate6CrosshairPick.setClientCrosshairHit(hit);
    }
}
