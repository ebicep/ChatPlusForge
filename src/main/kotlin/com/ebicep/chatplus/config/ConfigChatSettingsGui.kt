package com.ebicep.chatplus.config

import com.ebicep.chatplus.hud.ChatManager
import com.mojang.serialization.Codec
import net.minecraft.client.OptionInstance
import net.minecraft.client.Options
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.OptionsList
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component
import net.minecraft.util.ByIdMap
import net.minecraft.util.OptionEnum

class ConfigChatSettingsGui(private val lastScreen: Screen?) : Screen(Component.translatable("chatPlus.chatSettings")) {

    companion object {

        // shown in GUI
        lateinit var enabled: OptionInstance<Boolean>
        lateinit var scale: OptionInstance<Double>
        lateinit var maxMessages: OptionInstance<Int>
        lateinit var textOpacity: OptionInstance<Double>
        lateinit var backgroundOpacity: OptionInstance<Double>
        lateinit var lineSpacing: OptionInstance<Double>
        lateinit var chatTimestampMode: OptionInstance<TimestampMode>

        // not shown in GUI
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

        private var rescaleChat = false

        fun isEnabled(): Boolean {
            return ::enabled.isInitialized && enabled.get()
        }

        fun bake() {
            enabled = ConfigUtils.createBooleanOption("chatPlus.chatSettings.toggle", Config.enabled)
            scale = ConfigUtils.createDoubleOption("chatPlus.chatSettings.chatTextSize", Config.scale, true)
            maxMessages = ConfigUtils.createIntRangeOption(
                "chatPlus.chatSettings.maxMessages",
                Config.minMaxMessages,
                Config.maxMaxMessages,
                Config.maxMessages
            )
            textOpacity = ConfigUtils.createDoubleMinOption("chatPlus.chatSettings.textOpacity", .1, Config.textOpacity)
            backgroundOpacity = ConfigUtils.createDoubleOption("chatPlus.chatSettings.backgroundOpacity", Config.backgroundOpacity, false)
            lineSpacing = ConfigUtils.createDoubleOption("chatPlus.chatSettings.lineSpacing", Config.lineSpacing, false)
            chatTimestampMode = OptionInstance(
                "chatPlus.chatSettings.chatTimestampMode",
                OptionInstance.cachedConstantTooltip(Component.translatable("chatPlus.chatSettings.chatTimestampMode.tooltip")),
                OptionInstance.forOptionEnum(),
                OptionInstance.Enum(
                    listOf(*TimestampMode.values()),
                    Codec.INT.xmap(
                        { pId: Int -> TimestampMode.byId(pId) },
                        { obj: TimestampMode -> obj.id }
                    )
                ),
                TimestampMode.byId(Config.chatTimestampMode.get())
            ) { Config.delayedUpdates[Config.chatTimestampMode] = { Config.chatTimestampMode.set(it.id) } }

            x = Config.x.get()
            y = Config.y.get()
            chatWidth = Config.width.get()
            chatHeight = Config.height.get()
        }

        enum class TimestampMode(private val id: Int, private val key: String, val format: String) : OptionEnum {
            NONE(0, "chatPlus.chatSettings.chatTimestampMode.off", ""),
            HR_12(1, "chatPlus.chatSettings.chatTimestampMode.hr_12", "[hh:mm a]"),
            HR_12_SECOND(2, "chatPlus.chatSettings.chatTimestampMode.hr_12_second", "[hh:mm:ss a]"),
            HR_24(3, "chatPlus.chatSettings.chatTimestampMode.hr_24", "[HH:mm]"),
            HR_24_SECOND(4, "chatPlus.chatSettings.chatTimestampMode.hr_24_second", "[HH:mm:ss]"),

            ;

            override fun getId(): Int {
                return id
            }

            override fun getKey(): String {
                return key
            }

            companion object {
                private val BY_ID = ByIdMap.continuous(
                    { timestampMode: TimestampMode -> timestampMode.getId() },
                    TimestampMode.values(),
                    ByIdMap.OutOfBoundsStrategy.WRAP
                )

                fun byId(pId: Int): TimestampMode {
                    return BY_ID.apply(pId)
                }
            }
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
                lineSpacing,
                chatTimestampMode
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