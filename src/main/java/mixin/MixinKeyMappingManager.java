package mixin;

import com.ebicep.chatplus.config.ChatPlusKeyBindings;
import com.ebicep.chatplus.config.ConfigGui;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(KeyMapping.class)
public class MixinKeyMappingManager {

    @Inject(method = "same", at = @At("HEAD"), cancellable = true)
    public void same(KeyMapping pBinding, CallbackInfoReturnable<Boolean> cir) {
        if (!ConfigGui.Companion.getEnabled().get()) {
            return;
        }
        boolean otherIsChatPlusMapping = pBinding instanceof ChatPlusKeyBindings.ChatPlusKeyMapping;
        if (otherIsChatPlusMapping) {
            cir.setReturnValue(false);
        }
    }


}
