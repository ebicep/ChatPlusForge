package com.ebicep.chatplus.config

import com.ebicep.chatplus.hud.ChatManager
import net.minecraft.client.OptionInstance
import net.minecraft.client.Options
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.OptionsList
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component

class ConfigChatSettingsGui(private val lastScreen: Screen?) : Screen(Component.translatable("chatPlus.chatSettings")) {

    companion object {
        val enabled: OptionInstance<Boolean> = OptionInstance.createBoolean(
            "chatPlus.chatSettings.toggle",
            OptionInstance.cachedConstantTooltip(Component.translatable("chatPlus.chatSettings.toggle.tooltip")),
            Config.enabled.get()
        ) { Config.delayedUpdates[Config.enabled] = { Config.enabled.set(it) } }
        var x: Int = Config.x.get()
        var y: Int = Config.y.get()
        var chatWidth: Int = Config.width.get()
            set(width) {
                Config.delayedUpdates[Config.width] = { Config.width.set(width) }
                field = width
                ChatManager.selectedTab.rescaleChat()
            }
        var chatHeight: Int = Config.height.get()
            set(height) {
                Config.delayedUpdates[Config.height] = { Config.height.set(height) }
                field = height
                ChatManager.selectedTab.rescaleChat()
            }
        val scale: OptionInstance<Double> = OptionInstance(
            "chatPlus.chatSettings.chatTextSize",
            OptionInstance.cachedConstantTooltip(Component.translatable("chatPlus.chatSettings.chatTextSize.tooltip")),
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
                rescaleChat = true
                Config.delayedUpdates[Config.scale] = { Config.scale.set(it) }
            }
        )
        val maxMessages: OptionInstance<Int> = OptionInstance(
            "chatPlus.chatSettings.maxMessages",
            OptionInstance.cachedConstantTooltip(Component.translatable("chatPlus.chatSettings.maxMessages.tooltip")),
            { component: Component, value: Int ->
                if (value == 0) {
                    CommonComponents.optionStatus(component, false)
                } else {
                    genericValueLabel(component, value)
                }
            },
            OptionInstance.IntRange(Config.minMaxMessages, Config.maxMaxMessages),
            Config.maxMessages.get(),
            { Config.delayedUpdates[Config.maxMessages] = { Config.maxMessages.set(it) } }
        )
        val textOpacity = OptionInstance(
            "chatPlus.chatSettings.textOpacity",
            OptionInstance.cachedConstantTooltip(Component.translatable("chatPlus.chatSettings.textOpacity.tooltip")),
            { component: Component, value: Double -> percentValueLabel(component, value * 0.9 + 0.1) },
            OptionInstance.UnitDouble.INSTANCE,
            Config.textOpacity.get(),
            { Config.delayedUpdates[Config.textOpacity] = { Config.textOpacity.set(it) } }
        )
        val backgroundOpacity = OptionInstance(
            "chatPlus.chatSettings.backgroundOpacity",
            OptionInstance.cachedConstantTooltip(Component.translatable("chatPlus.chatSettings.backgroundOpacity.tooltip")),
            { component: Component, value: Double -> percentValueLabel(component, value) },
            OptionInstance.UnitDouble.INSTANCE,
            Config.backgroundOpacity.get(),
            { Config.delayedUpdates[Config.backgroundOpacity] = { Config.backgroundOpacity.set(it) } }
        )
        val lineSpacing = OptionInstance(
            "chatPlus.chatSettings.lineSpacing",
            OptionInstance.cachedConstantTooltip(Component.translatable("chatPlus.chatSettings.lineSpacing.tooltip")),
            { component: Component, value: Double -> percentValueLabel(component, value) },
            OptionInstance.UnitDouble.INSTANCE,
            0.0,
            { Config.delayedUpdates[Config.lineSpacing] = { Config.lineSpacing.set(it) } }
        )

        private var rescaleChat = false

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
                maxMessages,
                scale,
                textOpacity,
                backgroundOpacity,
                lineSpacing
            )
        )
        this.addWidget(this.list!!)
        this.addRenderableWidget(
            Button.builder(CommonComponents.GUI_DONE) {
                minecraft!!.options.save()
                minecraft!!.setScreen(lastScreen)
            }.bounds(this.width / 2 - 100, this.height - 29, 200, 20).build()
        )
    }

    override fun render(pGuiGraphics: GuiGraphics, pMouseX: Int, pMouseY: Int, pPartialTick: Float) {
        renderBackground(pGuiGraphics)
        list!!.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick)
        pGuiGraphics.drawCenteredString(font, title, width / 2, 20, 16777215)
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick)
    }

    override fun onClose() {
        if (rescaleChat) {
            ChatManager.selectedTab.rescaleChat()
        }
        minecraft!!.setScreen(lastScreen)
    }

    fun ChatOptionsScreen(pLastScreen: Screen?, pOptions: Options) {
        arrayOf<OptionInstance<*>>(
            pOptions.chatVisibility(),
            pOptions.chatColors(),
            pOptions.chatLinks(),
            pOptions.chatLinksPrompt(),
            pOptions.chatOpacity(),
            pOptions.textBackgroundOpacity(),
            pOptions.chatScale(),
            pOptions.chatLineSpacing(),
            pOptions.chatDelay(),
            pOptions.chatWidth(),
            pOptions.chatHeightFocused(),
            pOptions.chatHeightUnfocused(),
            pOptions.narrator(),
            pOptions.autoSuggestions(),
            pOptions.hideMatchedNames(),
            pOptions.reducedDebugInfo(),
            pOptions.onlyShowSecureChat()
        )
    }
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