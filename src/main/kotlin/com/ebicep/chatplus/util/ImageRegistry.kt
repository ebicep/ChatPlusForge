package com.ebicep.chatplus.util

import com.ebicep.chatplus.MODID
import net.minecraft.resources.ResourceLocation

enum class ImageRegistry(path: String) {

    UPDOWN("/updown.png"),
    LEFTRIGHT("/leftright.png"),
    MOVE("/move.png"),

    ;

    private var resourceLocation: ResourceLocation

    init {
        this.resourceLocation = ResourceLocation(MODID, path)
    }


    fun getResourceLocation(): ResourceLocation {
        return resourceLocation
    }

}