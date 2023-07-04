package com.ebicep.chatplus.util

import com.ebicep.chatplus.MODID
import net.minecraft.resources.ResourceLocation

enum class ImageRegistry(path: String) {

    ;

    private var resourceLocation: ResourceLocation? = null

    init {
        this.resourceLocation = ResourceLocation(MODID, path)
    }


    fun getResourceLocation(): ResourceLocation? {
        return resourceLocation
    }

}