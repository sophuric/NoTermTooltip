package me.sophur.notermtooltip.mixin;

import me.sophur.notermtooltip.NoTermTooltipClient;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiGraphicsExtractor.class)
public class TooltipMixin {
    @Shadow
    private @Nullable Runnable deferredTooltip;

    @Inject(method = "extractDeferredElements", at = @At("HEAD"))
    private void injectExtractDeferredElements(int mouseX, int mouseY, float a, CallbackInfo ci) {
        if (NoTermTooltipClient.shouldHide()) {
            this.deferredTooltip = null;
        }
    }
}
