package mixin;

import com.ebicep.chatplus.events.ForgeEvents;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MixinMinecraftManager {

    @Inject(method = "openChatScreen", at = @At("HEAD"))
    public void openChatScreen(String pDefaultText, CallbackInfo ci) {
        ForgeEvents.INSTANCE.setLatestDefaultText(pDefaultText);
    }

}
