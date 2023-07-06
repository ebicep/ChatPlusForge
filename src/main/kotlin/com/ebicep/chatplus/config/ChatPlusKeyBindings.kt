package com.ebicep.chatplus.config

import com.ebicep.chatplus.hud.ChatPlusScreen
import com.mojang.blaze3d.platform.InputConstants
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import net.minecraftforge.client.settings.IKeyConflictContext
import net.minecraftforge.client.settings.KeyConflictContext
import org.lwjgl.glfw.GLFW

object ChatPlusKeyBindings {

    val NO_SCOLL: ChatPlusKeyMapping = ChatPlusKeyMapping("key.noScoll", GLFW.GLFW_KEY_LEFT_CONTROL)
    val FINE_SCROLL: ChatPlusKeyMapping = ChatPlusKeyMapping("key.fineScroll", GLFW.GLFW_KEY_LEFT_SHIFT)
    val LARGE_SCROLL: ChatPlusKeyMapping = ChatPlusKeyMapping("key.largeScroll", GLFW.GLFW_KEY_LEFT_ALT)
    val KEY_BINDINGS = arrayOf(NO_SCOLL, FINE_SCROLL, LARGE_SCROLL)

    class ChatPlusKeyMapping(name: String, keyCode: Int) :
        KeyMapping(name, ChatPlusKeyConflict(), InputConstants.Type.KEYSYM, keyCode, "key.categories.chatplus") {

        override fun hasKeyModifierConflict(other: KeyMapping): Boolean {
            if (other.keyConflictContext == KeyConflictContext.UNIVERSAL) {
                return false
            }
            return super.hasKeyModifierConflict(other)
        }

        override fun same(pBinding: KeyMapping): Boolean {
            if (pBinding !is ChatPlusKeyMapping) {
                return false
            }
            return super.same(pBinding)
        }

    }

    class ChatPlusKeyConflict : IKeyConflictContext {
        override fun isActive(): Boolean {
            return Minecraft.getInstance().screen is ChatPlusScreen
        }

        override fun conflicts(other: IKeyConflictContext?): Boolean {
            return this == other
        }

    }

}