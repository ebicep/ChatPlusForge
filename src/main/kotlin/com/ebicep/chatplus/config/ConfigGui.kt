package com.ebicep.chatplus.config

import com.ebicep.chatplus.hud.ChatManager
import net.minecraft.client.OptionInstance
import net.minecraft.client.Options
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.OptionsList
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.controls.KeyBindsScreen
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component

class ConfigGui(private val lastScreen: Screen?) : Screen(Component.translatable("chatPlus.title")) {

    companion object {
        val enabled: OptionInstance<Boolean> = OptionInstance.createBoolean("Enabled", Config.enabled.get()) {
            Config.delayedUpdates[Config.enabled] = { Config.enabled.set(it) }
        }
        val x: Int = Config.x.get()
        val y: Int = Config.y.get()
        val chatWidth: Int = Config.width.get()
        val chatHeight: Int = Config.height.get()
        val scale: OptionInstance<Double> = OptionInstance(
            "Scale",
            OptionInstance.noTooltip(),
            { component: Component, value: Double ->
                if (value == 0.0) {
                    CommonComponents.optionStatus(component, false)
                } else {
                    percentValueLabel(component, value)
                }
            },
            OptionInstance.UnitDouble.INSTANCE,
            Config.scale.get(),
            {
                ChatManager.rescaleChat()
                Config.delayedUpdates[Config.scale] = { Config.scale.set(it) }
            }
        )
        val maxMessages: OptionInstance<Int> = OptionInstance(
            "Max Messages",
            OptionInstance.noTooltip(),
            { component: Component, value: Int ->
                if (value == 0) {
                    CommonComponents.optionStatus(component, false)
                } else {
                    genericValueLabel(component, value)
                }
            },
            OptionInstance.IntRange(Config.minMaxMessages, Config.maxMaxMessages),
            Config.maxMessages.get(),
            {
                Config.delayedUpdates[Config.maxMessages] = { Config.maxMessages.set(it) }
            }
        )

        //StringWidget
        private fun percentValueLabel(component: Component, value: Double): Component {
            return Component.translatable("options.percent_value", component, (value * 100.0).toInt())
        }

        private fun genericValueLabel(pText: Component, pValue: Int): Component {
            return Options.genericValueLabel(pText, Component.literal(pValue.toString()))
        }
    }

    private var list: OptionsList? = null

    override fun init() {
        this.list = OptionsList(
            minecraft!!, width, height,
            32,
            height - 32,
            25
        )
        this.list!!.addSmall(
            arrayOf(
                enabled,
                scale,
                maxMessages,
            )
        )
        this.addWidget(this.list!!)
        this.addRenderableWidget(
            Button.builder(Component.translatable("controls.keybinds")) {
                val keyBindsScreen = KeyBindsScreen(this, minecraft!!.options)
                minecraft!!.setScreen(keyBindsScreen)
                keyBindsScreen.keyBindsList.scrollAmount = keyBindsScreen.keyBindsList.maxScroll.toDouble() + 1
            }.bounds(this.width / 2 - 155, this.height - 29, 150, 20).build()
        )
        this.addRenderableWidget(
            Button.builder(CommonComponents.GUI_DONE) {
                minecraft!!.options.save()
                minecraft!!.setScreen(lastScreen)
            }.bounds(this.width / 2 - 155 + 160, this.height - 29, 150, 20).build()
        )
    }

    override fun render(pGuiGraphics: GuiGraphics, pMouseX: Int, pMouseY: Int, pPartialTick: Float) {
        renderBackground(pGuiGraphics)
        list!!.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick)
        pGuiGraphics.drawCenteredString(font, title, width / 2, 20, 16777215)
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick)
    }

    //    fun ChatOptionsScreen(pLastScreen: Screen?, pOptions: Options) {
    //        arrayOf<OptionInstance<*>>(
    //            pOptions.chatVisibility(),
    //            pOptions.chatColors(),
    //            pOptions.chatLinks(),
    //            pOptions.chatLinksPrompt(),
    //            pOptions.chatOpacity(),
    //            pOptions.textBackgroundOpacity(),
    //            pOptions.chatScale(),
    //            pOptions.chatLineSpacing(),
    //            pOptions.chatDelay(),
    //            pOptions.chatWidth(),
    //            pOptions.chatHeightFocused(),
    //            pOptions.chatHeightUnfocused(),
    //            pOptions.narrator(),
    //            pOptions.autoSuggestions(),
    //            pOptions.hideMatchedNames(),
    //            pOptions.reducedDebugInfo(),
    //            pOptions.onlyShowSecureChat()
    //        )
    //    }
    //
    //    private fun options(pOptions: Options): Array<OptionInstance<*>>? {
    //        return arrayOf(
    //            pOptions.graphicsMode(),
    //            pOptions.renderDistance(),
    //            pOptions.prioritizeChunkUpdates(),
    //            pOptions.simulationDistance(),
    //            pOptions.ambientOcclusion(),
    //            pOptions.framerateLimit(),
    //            pOptions.enableVsync(),
    //            pOptions.bobView(),
    //            pOptions.guiScale(),
    //            pOptions.attackIndicator(),
    //            pOptions.gamma(),
    //            pOptions.cloudStatus(),
    //            pOptions.fullscreen(),
    //            pOptions.particles(),
    //            pOptions.mipmapLevels(),
    //            pOptions.entityShadows(),
    //            pOptions.screenEffectScale(),
    //            pOptions.entityDistanceScaling(),
    //            pOptions.fovEffectScale(),
    //            pOptions.showAutosaveIndicator(),
    //            pOptions.glintSpeed(),
    //            pOptions.glintStrength()
    //        )
    //    }

}