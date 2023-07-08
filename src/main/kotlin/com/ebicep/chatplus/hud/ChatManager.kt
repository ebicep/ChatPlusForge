package com.ebicep.chatplus.hud

import com.ebicep.chatplus.config.ConfigChatSettingsGui
import net.minecraft.client.Minecraft
import net.minecraft.util.Mth
import kotlin.math.roundToInt

object ChatManager {

    val baseYOffset = 29

    val sentMessages: MutableList<String> = ArrayList()

    val chatTabs: MutableList<ChatTab> = ArrayList()
    var selectedTab: ChatTab = ChatTab("All", "(?s).*")

    /**
     * Gets the list of messages previously sent through the chat GUI
     */
    fun getRecentChat(): List<String?> {
        return this.sentMessages
    }

    /**
     * Adds this string to the list of sent messages, for recall using the up/down arrow keys
     */
    fun addSentMessage(pMessage: String) {
        if (this.sentMessages.isEmpty() || this.sentMessages[this.sentMessages.size - 1] != pMessage) {
            this.sentMessages.add(pMessage)
        }
    }

    fun handleClickedCategory(x: Double, y: Double) {
        val translatedY = getY() - y
        var xOff = 0.0
        val font = Minecraft.getInstance().font
        //ChatPlus.LOGGER.debug("x: $x, translatedY: $translatedY")
        if (translatedY > ChatRenderer.tabYOffset || translatedY < -(9 + ChatTab.PADDING + ChatTab.PADDING)) {
            return
        }
        chatTabs.forEach {
            val categoryLength = font.width(it.name) + ChatTab.PADDING + ChatTab.PADDING
            if (x > xOff && x < xOff + categoryLength) {
                selectedTab = it
            }
            xOff += categoryLength + ChatRenderer.tabXBetween
        }
    }

    fun isChatFocused(): Boolean {
        return Minecraft.getInstance().screen is ChatPlusScreen
    }

    fun getScale(): Float {
        return ConfigChatSettingsGui.scale.get().toFloat()
    }

    fun getWidth(): Int {
        val guiScaledWidth = Minecraft.getInstance().window.guiScaledWidth
        if (ConfigChatSettingsGui.chatWidth < 0) {
            ConfigChatSettingsGui.chatWidth = 200
        } else if (getX() + ConfigChatSettingsGui.chatWidth / getScale() > guiScaledWidth) {
            ConfigChatSettingsGui.chatWidth = (Mth.clamp(
                (ConfigChatSettingsGui.chatWidth / getScale()).toInt(),
                getX() + 160,
                guiScaledWidth
            ) * getScale()).roundToInt() - 1
        }
        return ConfigChatSettingsGui.chatWidth
    }

    fun getBackgroundWidth(): Int {
        return (getWidth() / getScale()).toInt()
    }

    fun getHeight(): Int {
        if (getY() - ConfigChatSettingsGui.chatHeight < 0) {
            ConfigChatSettingsGui.chatHeight = getY()
        }
        return if (isChatFocused()) {
            return ConfigChatSettingsGui.chatHeight
        } else {
            return (ConfigChatSettingsGui.chatHeight * .5).toInt()
        }
    }

    fun getX(): Int {
        val clamp = Mth.clamp(
            ConfigChatSettingsGui.x,
            0,
            Minecraft.getInstance().window.guiScaledWidth - (ConfigChatSettingsGui.chatWidth / getScale()).toInt()
        )
        if (clamp < 0) {
            ConfigChatSettingsGui.x = 0
            return 0
        }
        return clamp
    }

    fun getY(): Int {
        var y = ConfigChatSettingsGui.y
        if (y < 0) {
            y += Minecraft.getInstance().window.guiScaledHeight
        }
        return Mth.clamp(y, ConfigChatSettingsGui.chatHeight, Minecraft.getInstance().window.guiScaledHeight - baseYOffset)
    }


    fun getLinesPerPage(): Int {
        return getHeight() / getLineHeight()
    }

    fun getLineHeight(): Int {
        return 9
    }

}