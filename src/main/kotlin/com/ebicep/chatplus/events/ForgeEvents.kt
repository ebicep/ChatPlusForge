package com.ebicep.chatplus.events

import com.ebicep.chatplus.MODID
import com.ebicep.chatplus.hud.ChatPlusScreen
import net.minecraft.client.gui.screens.ChatScreen
import net.minecraftforge.client.event.RenderGuiOverlayEvent
import net.minecraftforge.client.event.ScreenEvent
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod


@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
object ForgeEvents {

    var latestDefaultText = ""

    @SubscribeEvent
    fun onChatRender(event: RenderGuiOverlayEvent.Pre) {
        if (event.overlay == VanillaGuiOverlay.CHAT_PANEL.type()) {
            event.isCanceled = true
        }
    }

    @SubscribeEvent
    fun onScreenOpen(event: ScreenEvent.Opening) {
        if (event.newScreen is ChatScreen && event.newScreen !is ChatPlusScreen) {
            event.newScreen = ChatPlusScreen(latestDefaultText)
        }
    }

}