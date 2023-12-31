package com.ebicep.chatplus.config

import com.ebicep.chatplus.ChatPlus
import com.ebicep.chatplus.hud.ChatManager
import com.ebicep.chatplus.hud.ChatTab
import com.ebicep.chatplus.hud.ChatTabRecord
import net.minecraftforge.common.ForgeConfigSpec
import kotlin.concurrent.fixedRateTimer

object Config {

    val GENERAL_SPEC: ForgeConfigSpec

    lateinit var enabled: ForgeConfigSpec.BooleanValue
    lateinit var x: ForgeConfigSpec.ConfigValue<Int>
    lateinit var y: ForgeConfigSpec.ConfigValue<Int>
    lateinit var height: ForgeConfigSpec.ConfigValue<Int>
    lateinit var width: ForgeConfigSpec.ConfigValue<Int>
    lateinit var scale: ForgeConfigSpec.ConfigValue<Double>
    const val minMaxMessages = 1000
    const val maxMaxMessages = 10_000_000
    lateinit var maxMessages: ForgeConfigSpec.IntValue
    lateinit var textOpacity: ForgeConfigSpec.ConfigValue<Double>
    lateinit var backgroundOpacity: ForgeConfigSpec.ConfigValue<Double>
    lateinit var lineSpacing: ForgeConfigSpec.ConfigValue<Double>
    lateinit var chatTimestampMode: ForgeConfigSpec.ConfigValue<Int>

    private lateinit var chatTabs: ConfigHelper.ConfigObject<MutableList<ChatTabRecord>>

    // values that need to be updated, runs every 10 seconds to prevent spam saving
    val delayedUpdates: HashMap<Any, () -> Unit> = HashMap()

    init {
        val builder = ForgeConfigSpec.Builder()
        setupConfig(builder)
        GENERAL_SPEC = builder.build()
        fixedRateTimer(period = 10 * 1000) {
            delayedUpdates.forEach { it.value() }
            delayedUpdates.clear()
        }
    }

    fun init() {
        // general settings
        ChatPlus.LOGGER.info("Baking general settings.")
        ConfigChatSettingsGui.bake()
        // tabs
        val tabsToLoad = chatTabs.get()
        ChatPlus.LOGGER.info("Trying to load ${tabsToLoad.size} tabs.")
        if (tabsToLoad.size >= ConfigTabsGui.MAX_TABS) {
            chatTabs.set(tabsToLoad.subList(0, ConfigTabsGui.MAX_TABS))
        }

        val tabs = chatTabs.get()
        ChatPlus.LOGGER.info("Loaded ${tabs.size} tabs.")
        tabs.forEach {
            ChatManager.chatTabs.add(ChatTab(it.name, it.pattern))
        }
        ChatManager.selectedTab = ChatManager.chatTabs[0]
    }

    private fun setupConfig(builder: ForgeConfigSpec.Builder) {
        enabled = builder.define("enabled", true)
        x = builder.define("x", 0)
        y = builder.define("y", -ChatManager.baseYOffset)
        height = builder.define("height", 180)
        width = builder.define("width", 320)
        scale = builder.define("scale", 1.0)
        maxMessages = builder.defineInRange("maxMessages", minMaxMessages, minMaxMessages, maxMaxMessages)
        textOpacity = builder.define("textOpacity", 1.0)
        backgroundOpacity = builder.define("backgroundOpacity", 0.5)
        lineSpacing = builder.define("lineSpacing", 0.0)
        chatTimestampMode = builder.define("chatTimestampMode", 0)


        chatTabs = ConfigHelper.defineObject(
            builder,
            "tab",
            ChatTabRecord.CODEC.listOf(),
            mutableListOf(ChatTabRecord("All", "(?s).*"))
        )
    }

    fun refreshTabs() {
        val mutableList = mutableListOf<ChatTabRecord>()
        ChatManager.chatTabs.forEach { mutableList.add(ChatTabRecord(it.name, it.pattern)) }
        delayedUpdates[chatTabs] = { chatTabs.set(mutableList) }
    }

}