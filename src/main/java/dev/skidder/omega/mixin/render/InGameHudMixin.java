package dev.skidder.omega.mixin.render;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.skidder.omega.event.impl.Render2DEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {

    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method = "render", at = @At("HEAD"))
    private void render(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        client.getProfiler().push("m7thh4ck_render_2d");
        Render2DEvent event = new Render2DEvent(context);
        event.post();
        RenderSystem.applyModelViewMatrix();
        client.getProfiler().pop();
    }

}
