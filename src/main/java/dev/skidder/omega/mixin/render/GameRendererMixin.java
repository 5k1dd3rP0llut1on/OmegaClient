package dev.skidder.omega.mixin.render;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.skidder.omega.event.impl.Render3DEvent;
import dev.skidder.omega.util.graphics.Render3DEngine;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/GameRenderer;renderHand:Z", opcode = Opcodes.GETFIELD, ordinal = 0), method = "renderWorld")
    void render3dHook(RenderTickCounter tickCounter, CallbackInfo ci) {
        MatrixStack matrices = new MatrixStack();

        Render3DEngine.INSTANCE.getLastProjMat().set(RenderSystem.getProjectionMatrix());
        Render3DEngine.INSTANCE.getLastModMat().set(RenderSystem.getModelViewMatrix());
        Render3DEngine.INSTANCE.getLastWorldSpaceMatrix().set(matrices.peek().getPositionMatrix());

        new Render3DEvent(matrices);

        RenderSystem.getModelViewStack().popMatrix();
        RenderSystem.applyModelViewMatrix();
    }
}