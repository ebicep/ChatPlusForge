package com.ebicep.chatplus.config

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.Tooltip
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.controls.KeyBindsScreen
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component

class ConfigGui(private val lastScreen: Screen?) : Screen(Component.translatable("chatPlus.title")) {

    override fun init() {
        val i = width / 2 - 125
        val j = i + 160
        var k = height / 6 - 12
        addRenderableWidget(Button.builder(Component.translatable("chatPlus.chatSettings")) {
            minecraft!!.setScreen(ConfigChatSettingsGui(this))
        }
            .tooltip(Tooltip.create(Component.translatable("chatPlus.chatSettings.tooltip")))
            .bounds(i, k, 250, 20)
            .build())
        k += 24
        addRenderableWidget(
            Button.builder(Component.translatable("chatPlus.chatTabs")) {
                minecraft!!.setScreen(ConfigTabsGui(this))
            }
                .tooltip(Tooltip.create(Component.translatable("chatPlus.chatTabs.tooltip")))
                .bounds(i, k, 250, 20)
                .build()
        )
        k += 24
        addRenderableWidget(
            Button.builder(Component.translatable("chatPlus.chatKeyBinds")) {
                val keyBindsScreen = KeyBindsScreen(this, minecraft!!.options)
                minecraft!!.setScreen(keyBindsScreen)
                keyBindsScreen.keyBindsList.scrollAmount = keyBindsScreen.keyBindsList.maxScroll.toDouble() + 1
            }
                .tooltip(Tooltip.create(Component.translatable("chatPlus.chatKeyBinds.tooltip")))
                .bounds(i, k, 250, 20)
                .build()
        )


        k += 24 * 3
        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE) {
            this.minecraft!!.setScreen(this.lastScreen)
        }.bounds(this.width / 2 - 100, this.height - 27, 200, 20).build());
    }

    override fun render(pGuiGraphics: GuiGraphics, pMouseX: Int, pMouseY: Int, pPartialTick: Float) {
        renderBackground(pGuiGraphics)
        pGuiGraphics.drawCenteredString(font, title, width / 2, 20, 16777215)
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick)
    }

    override fun onClose() {
        minecraft!!.setScreen(lastScreen)
    }

}