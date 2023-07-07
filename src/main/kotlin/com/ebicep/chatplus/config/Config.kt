package com.ebicep.chatplus.config

import com.ebicep.chatplus.ChatPlus
import com.ebicep.chatplus.hud.ChatManager
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

    // values that need to be updated, runs every 10 seconds to prevent spam saving
    val delayedUpdates: HashMap<Any, () -> Unit> = HashMap()

    init {
        val builder = ForgeConfigSpec.Builder()
        setupConfig(builder)
        GENERAL_SPEC = builder.build()
        ChatPlus.LOGGER.info("Initialized config.")
        fixedRateTimer(period = 10 * 1000) {
            delayedUpdates.forEach { it.value() }
            delayedUpdates.clear()
        }
    }

    private fun setupConfig(builder: ForgeConfigSpec.Builder) {
        enabled = builder.define("enabled", true)
        x = builder.define("x", 0)
        y = builder.define("y", -ChatManager.baseYOffset)
        height = builder.define("height", 180)
        width = builder.define("width", 320)
        scale = builder.define("scale", 1.0)
        maxMessages = builder.defineInRange("maxMessages", minMaxMessages, minMaxMessages, maxMaxMessages)
    }

}