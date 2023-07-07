package com.ebicep.chatplus.hud

import com.ebicep.chatplus.config.ConfigGui
import net.minecraft.client.Minecraft
import net.minecraft.util.Mth

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
        val translatedY = Minecraft.getInstance().window.guiScaledHeight.toDouble() - y - baseYOffset
        var xOff = 0.0
        val font = Minecraft.getInstance().font
        if (translatedY > -ChatRenderer.categoryYOffset || translatedY < -(9 + ChatCategory.PADDING + ChatCategory.PADDING)) {
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
        return ConfigGui.chatWidth
    }

    fun getHeight(): Int {
        return if (isChatFocused()) {
            getHeight(0f) //TODO
        } else {
            getHeight(0f)
        }
    }

    fun getHeight(heightPercent: Float): Int {
        return ConfigGui.chatHeight
        //return Mth.floor(Minecraft.getInstance().window.guiScaledHeight * (heightPercent - .1))
    }

    fun getX(): Int {
        return Mth.clamp(ConfigGui.x, 0, Minecraft.getInstance().window.guiScaledWidth - getWidth())
    }

    fun getY(): Int {
        var y = ConfigGui.y
        if (y < 0) {
            y += Minecraft.getInstance().window.guiScaledHeight
        }
        return Mth.clamp(y, baseYOffset, Minecraft.getInstance().window.guiScaledHeight - getHeight())
    }


    fun getLinesPerPage(): Int {
        return getHeight() / getLineHeight()
    }

    fun getLineHeight(): Int {
        return 9
    }

}