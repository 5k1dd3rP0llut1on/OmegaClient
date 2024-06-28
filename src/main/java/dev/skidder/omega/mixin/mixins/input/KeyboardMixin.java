package dev.skidder.omega.mixin.mixins.input;

import dev.skidder.omega.event.impl.KeyEvent;
import dev.skidder.omega.settings.KeyBind;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public class KeyboardMixin {

    @Inject(method = "onKey", at = @At("HEAD"))
    public void onKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        if (MinecraftClient.getInstance().currentScreen != null) return;
        if (action != GLFW.GLFW_PRESS) return;
        if (key == GLFW.GLFW_KEY_UNKNOWN) return;
        new KeyEvent(new KeyBind(KeyBind.Type.KEYBOARD, key, 1)).post();
    }

}
