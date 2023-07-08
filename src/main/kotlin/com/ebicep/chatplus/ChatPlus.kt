package com.ebicep.chatplus

import com.ebicep.chatplus.config.Config
import com.ebicep.chatplus.config.ConfigGui
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen
import net.minecraftforge.client.ConfigScreenHandler
import net.minecraftforge.fml.ModLoadingContext
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.config.ModConfig
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import thedarkcolour.kotlinforforge.forge.MOD_BUS

const val MODID = "chatplus"

@Mod(MODID)
object ChatPlus {

    val LOGGER: Logger = LogManager.getLogger(MODID)

    init {
        LOGGER.log(Level.INFO, "$MODID has started!")
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.GENERAL_SPEC, "ChatPlus.toml");
        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory::class.java) {
            ConfigScreenHandler.ConfigScreenFactory { _: Minecraft, screen: Screen ->
                ConfigGui(screen)
            }
        }
        MOD_BUS.addListener(this::onClientSetup)
    }

    //FMLCommonSetupEvent -> FMLClientSetupEvent -> InterModEnqueueEvent -> InterModProcessEvent
    //@SubscribeEvent
    private fun onClientSetup(event: FMLClientSetupEvent) {
        LOGGER.log(Level.INFO, "Initializing client...")
        Config.init()
        LOGGER.log(Level.INFO, "Done initializing client")
    }

}