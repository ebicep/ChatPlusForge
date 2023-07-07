package com.ebicep.chatplus.hud

import com.ebicep.chatplus.config.ConfigGui
import net.minecraft.client.Minecraft
import net.minecraft.util.Mth
import kotlin.math.roundToInt

object ChatManager {

    val baseYOffset = 29

    val sentMessages: MutableList<String> = ArrayList()

    val chatCategories: MutableList<ChatCategory> = ArrayList()
    var selectedCategory: ChatCategory = ChatCategory("All", "(?s).*")

    init {
        chatCategories.add(selectedCategory)
        chatCategories.add(ChatCategory("Upgrades", "^Upgrades > .*"))
        chatCategories.add(ChatCategory("Test", ""))
        chatCategories.add(ChatCategory("Party", "^Party > .*"))
    }

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
        if (translatedY > ChatRenderer.categoryYOffset || translatedY < -(9 + ChatCategory.PADDING + ChatCategory.PADDING)) {
            return
        }
        chatCategories.forEach {
            val categoryLength = font.width(it.name) + ChatCategory.PADDING + ChatCategory.PADDING
            if (x > xOff && x < xOff + categoryLength) {
                selectedCategory = it
            }
            xOff += categoryLength + ChatRenderer.categoryXBetween
        }
    }

    fun isChatFocused(): Boolean {
        return Minecraft.getInstance().screen is ChatPlusScreen
    }

    fun getScale(): Float {
        return ConfigGui.scale.get().toFloat()
    }

    fun getWidth(): Int {
        val guiScaledWidth = Minecraft.getInstance().window.guiScaledWidth
        if (ConfigGui.chatWidth < 0) {
            ConfigGui.chatWidth = 200
        } else if (getX() + ConfigGui.chatWidth / getScale() > guiScaledWidth) {
            ConfigGui.chatWidth = (Mth.clamp(
                (ConfigGui.chatWidth / getScale()).toInt(),
                getX() + 160,
                guiScaledWidth
            ) * getScale()).roundToInt() - 1
        }
        return ConfigGui.chatWidth
    }

    fun getBackgroundWidth(): Int {
        return (getWidth() / getScale()).toInt()
    }

    fun getHeight(): Int {
        if (getY() - ConfigGui.chatHeight < 0) {
            ConfigGui.chatHeight = getY()
        }
        return if (isChatFocused()) {
            return ConfigGui.chatHeight
        } else {
            return (ConfigGui.chatHeight * .5).toInt()
        }
    }

    fun getX(): Int {
        val clamp = Mth.clamp(ConfigGui.x, 0, Minecraft.getInstance().window.guiScaledWidth - (ConfigGui.chatWidth / getScale()).toInt())
        if (clamp < 0) {
            ConfigGui.x = 0
            return 0
        }
        return clamp
    }

    fun getY(): Int {
        var y = ConfigGui.y
        if (y < 0) {
            y += Minecraft.getInstance().window.guiScaledHeight
        }
        return Mth.clamp(y, ConfigGui.chatHeight, Minecraft.getInstance().window.guiScaledHeight - baseYOffset)
    }


    fun getLinesPerPage(): Int {
        return getHeight() / getLineHeight()
    }

    fun getLineHeight(): Int {
        return 9
    }

}