package com.ebicep.chatplus.events

import com.ebicep.chatplus.ChatPlus
import com.ebicep.chatplus.MODID
import com.ebicep.chatplus.hud.ChatRenderer
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod

@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
object ModEvents {

    @SubscribeEvent
    fun onGuiRegister(event: RegisterGuiOverlaysEvent) {
        event.registerAbove(VanillaGuiOverlay.CHAT_PANEL.id(), "chat_plus", ChatRenderer)
        ChatPlus.LOGGER.info("Registered ChatPlus")
    }

}