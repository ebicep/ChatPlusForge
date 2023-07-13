package com.ebicep.chatplus.events

import com.ebicep.chatplus.MODID
import com.ebicep.chatplus.config.ConfigChatSettingsGui.Companion.enabled
import com.ebicep.chatplus.config.ConfigGui
import com.ebicep.chatplus.hud.ChatManager
import com.ebicep.chatplus.hud.ChatPlusScreen
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.ChatScreen
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraftforge.client.event.RegisterClientCommandsEvent
import net.minecraftforge.client.event.RenderGuiOverlayEvent
import net.minecraftforge.client.event.ScreenEvent
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay
import net.minecraftforge.event.TickEvent
import net.minecraftforge.event.TickEvent.ClientTickEvent
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
    fun onRegisterCommands(event: RegisterClientCommandsEvent) {
        event.dispatcher.register(Commands.literal("chatplus")
            .then(
                LiteralArgumentBuilder.literal<CommandSourceStack?>("clearchat")
                    .then(Commands.literal("all")
                        .executes {
                            ChatManager.chatTabs.forEach {
                                it.messages.clear()
                                it.displayedMessages.clear()
                            }
                            1
                        })
                    .then(Commands.literal("selected")
                        .executes {
                            ChatManager.selectedTab.messages.clear()
                            ChatManager.selectedTab.displayedMessages.clear()
                            1
                        })
            )
            .executes {
                Minecraft.getInstance().setScreen(ConfigGui(null))
                1
            })

    }

    var currentTick = 0

    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START) {
            return
        }
        currentTick++

        ChatManager.chatTabs.forEach {
            if (it.resetDisplayMessageAtTick == currentTick) {
                it.refreshDisplayedMessage()
            }
        }

    }

}