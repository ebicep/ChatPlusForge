package com.ebicep.chatplus.hud

import com.ebicep.chatplus.config.ConfigChatSettingsGui
import net.minecraft.client.Minecraft
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

    /**
     * Width of chat window, raw value not scaled
     */
    fun getWidth(): Int {
        val guiWidth = Minecraft.getInstance().window.guiScaledWidth
        if (ConfigChatSettingsGui.chatWidth <= 0) {
            ConfigChatSettingsGui.chatWidth = 200.coerceAtMost(guiWidth - getX() - 1)
        }
        if (getX() + ConfigChatSettingsGui.chatWidth >= guiWidth) {
            ConfigChatSettingsGui.chatWidth = guiWidth - getX() - 1
        }
        return ConfigChatSettingsGui.chatWidth
    }

    fun getBackgroundWidth(): Float {
        return getWidth() / getScale()
    }


    /**
     * Height of chat window, raw value not scaled
     */
    fun getHeight(): Int {
        if (getY() - ConfigChatSettingsGui.chatHeight <= 0) {
            ConfigChatSettingsGui.chatHeight = getY() - 1
        }
        if (ConfigChatSettingsGui.chatHeight >= getY()) {
            ConfigChatSettingsGui.chatHeight = getY() - 1
        }
        return if (isChatFocused()) {
            return ConfigChatSettingsGui.chatHeight
        } else {
            return (ConfigChatSettingsGui.chatHeight * .5).roundToInt()
        }
    }

    fun getX(): Int {
        var x = ConfigChatSettingsGui.x
        if (x < 0) {
            x = 0
            ConfigChatSettingsGui.x = x
        }
        if (x >= Minecraft.getInstance().window.guiScaledWidth) {
            x = Minecraft.getInstance().window.guiScaledWidth - 1
            ConfigChatSettingsGui.x = x
        }
        return x
    }

    fun getY(): Int {
        var y = ConfigChatSettingsGui.y
        if (y < 0) {
            y += Minecraft.getInstance().window.guiScaledHeight
            ConfigChatSettingsGui.y = y
        }
        if (y >= Minecraft.getInstance().window.guiScaledHeight) {
            y = Minecraft.getInstance().window.guiScaledHeight - baseYOffset
            ConfigChatSettingsGui.y = y
        }
        return y
    }


    fun getLinesPerPage(): Int {
        return getHeight() / getLineHeight()
    }

    fun getLinesPerPageScaled(): Int {
        return (getLinesPerPage() / getScale()).toInt()
    }

    fun getLineHeight(): Int {
        return (9.0 * (getLineSpacing() + 1.0)).toInt()
    }

    fun getTextOpacity(): Double {
        return ConfigChatSettingsGui.textOpacity.get()
    }

    fun getBackgroundOpacity(): Double {
        return ConfigChatSettingsGui.backgroundOpacity.get()
    }

    fun getLineSpacing(): Double {
        return ConfigChatSettingsGui.lineSpacing.get()
    }

}