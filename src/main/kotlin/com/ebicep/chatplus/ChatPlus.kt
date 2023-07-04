package com.ebicep.chatplus

import config.Config
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import thedarkcolour.kotlinforforge.forge.MOD_BUS

const val MODID = "chatplus"

@Mod(MODID)
object ChatPlus {

    val LOGGER: Logger = LogManager.getLogger(MODID)
    val config: Config = Config

    init {
        LOGGER.log(Level.INFO, "$MODID has started!")
        MOD_BUS.addListener(this::onClientSetup)
    }

    //FMLCommonSetupEvent -> FMLClientSetupEvent -> InterModEnqueueEvent -> InterModProcessEvent
    //@SubscribeEvent
    fun onClientSetup(event: FMLClientSetupEvent) {
        LOGGER.log(Level.INFO, "Initializing client...")
        LOGGER.log(Level.INFO, "Done initializing client")
    }

}