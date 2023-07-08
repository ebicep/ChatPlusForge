package com.ebicep.chatplus.events

import com.ebicep.chatplus.MODID
import com.ebicep.chatplus.config.Config
import com.ebicep.chatplus.config.ConfigChatSettingsGui
import com.ebicep.chatplus.config.ConfigChatSettingsGui.Companion.enabled
import com.ebicep.chatplus.config.ConfigGui
import com.ebicep.chatplus.hud.ChatManager
import com.ebicep.chatplus.hud.ChatPlusScreen
import hud.ChatTabRecord
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.ChatScreen
import net.minecraftforge.client.event.ClientChatEvent
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
        if (!enabled.get()) {
            return
        }
        if (event.overlay == VanillaGuiOverlay.CHAT_PANEL.type()) {
            event.isCanceled = true
        }
    }

    @SubscribeEvent
    fun onScreenOpen(event: ScreenEvent.Opening) {
        if (!enabled.get()) {
            return
        }
        if (event.newScreen is ChatScreen && event.newScreen !is ChatPlusScreen) {
            event.newScreen = ChatPlusScreen(latestDefaultText)
        }
    }

    @SubscribeEvent
    fun onChat(event: ClientChatEvent) {
        val message = event.message
        when (message) {
            "OPEN" -> {
                Minecraft.getInstance().setScreen(ConfigGui(null))
                event.isCanceled = true
            }

            "RESET" -> {
                ConfigChatSettingsGui.chatWidth = 200
                event.isCanceled = true
            }

            "CLEAR" -> {
                ChatManager.selectedTab.messages.clear()
                ChatManager.selectedTab.displayedMessages.clear()
                event.isCanceled = true
            }

            "TEST" -> {
                val categoryRecords = Config.chatTabs.get()
                categoryRecords.add(ChatTabRecord("PARTY", "?D?WE?WDW"))
                Config.chatTabs.set(categoryRecords)
                event.isCanceled = true
            }
        }
    }

//    @SubscribeEvent
//    fun onConfigReload(event : ModConfigEvent.Reloading) {
//        ChatPlus.LOGGER.info("Reloading config...")
//    }

}