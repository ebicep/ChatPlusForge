package com.ebicep.chatplus.hud

import com.ebicep.chatplus.config.ConfigGui
import com.ebicep.chatplus.hud.ChatRenderer.padding
import net.minecraft.client.GuiMessage
import net.minecraft.client.GuiMessageTag
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.ComponentRenderUtils
import net.minecraft.client.multiplayer.chat.ChatListener
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MessageSignature
import net.minecraft.network.chat.Style
import net.minecraft.util.Mth
import kotlin.math.min

object ChatManager {

    val sentMessages: MutableList<String> = ArrayList()
    val messages: MutableList<GuiMessage> = ArrayList()
    val displayedMessages: MutableList<GuiMessage.Line> = ArrayList()
    var chatScrollbarPos: Int = 0
    var updateChat = false

    fun addMessage(
        pChatComponent: Component,
        pHeaderSignature: MessageSignature?,
        pAddedTime: Int,
        pTag: GuiMessageTag?,
        pOnlyTrim: Boolean
    ) {
        var i = Mth.floor(this.getWidth().toDouble() / this.getScale())
        if (pTag?.icon() != null) {
            i -= pTag.icon()!!.width + 4 + 2
        }
//        ChatPlus.LOGGER.info("i: $i")
//        ChatPlus.LOGGER.info("pChatComponent: ${pChatComponent.contents}")
        val list = ComponentRenderUtils.wrapComponents(pChatComponent, i, Minecraft.getInstance().font)
        val flag = isChatFocused()
        for (j in list.indices) {
            val formattedCharSequence = list[j]
            if (flag && chatScrollbarPos > 0) {
                updateChat = true
                scrollChat(1)
            }
            val flag1 = j == list.size - 1
            this.displayedMessages.add(0, GuiMessage.Line(pAddedTime, formattedCharSequence, pTag, flag1))
        }
        while (this.displayedMessages.size > 2000) {
            this.displayedMessages.removeAt(this.displayedMessages.size - 1)
        }
        if (!pOnlyTrim) {
            this.messages.add(0, GuiMessage(pAddedTime, pChatComponent, pHeaderSignature, pTag))
            while (this.messages.size > ConfigGui.maxMessages.get()) {
                this.messages.removeAt(this.messages.size - 1)
            }
        }
    }

    fun getMessageTagAt(pMouseX: Double, pMouseY: Double): GuiMessageTag? {
        val d0: Double = this.screenToChatX(pMouseX)
        val d1: Double = this.screenToChatY(pMouseY)
        val i: Int = this.getMessageEndIndexAt(d0, d1)
        if (i >= 0 && i < this.displayedMessages.size) {
            val guiMessageLine: GuiMessage.Line = this.displayedMessages[i]
            val guiMessageTag = guiMessageLine.tag()
            if (guiMessageTag != null && this.hasSelectedMessageTag(d0, guiMessageLine, guiMessageTag)) {
                return guiMessageTag
            }
        }
        return null
    }

    fun screenToChatX(pX: Double): Double {
        return pX / getScale() - 4.0
    }

    fun screenToChatY(pY: Double): Double {
        val d0: Double = Minecraft.getInstance().window.guiScaledHeight.toDouble() - pY - 40.0
        return d0 / (getScale() * getLineHeight().toDouble())
    }

    fun getMessageEndIndexAt(pMouseX: Double, pMouseY: Double): Int {
        var i: Int = this.getMessageLineIndexAt(pMouseX, pMouseY)
        return if (i == -1) {
            -1
        } else {
            while (i >= 0) {
                if (this.displayedMessages[i].endOfEntry()) {
                    return i
                }
                --i
            }
            i
        }
    }

    private fun getMessageLineIndexAt(pMouseX: Double, pMouseY: Double): Int {
        return if (isChatFocused() && !Minecraft.getInstance().options.hideGui) {
            if (pMouseX >= -4.0 && pMouseX <= Mth.floor(this.getWidth().toDouble() / getScale()).toDouble()) {
                val i = min(getLinesPerPage(), this.displayedMessages.size)
                if (pMouseY >= 0.0 && pMouseY < i.toDouble()) {
                    val j = Mth.floor(pMouseY + chatScrollbarPos.toDouble())
                    if (j >= 0 && j < this.displayedMessages.size) {
                        return j
                    }
                }
                -1
            } else {
                -1
            }
        } else {
            -1
        }
    }

    private fun hasSelectedMessageTag(p_240619_: Double, pLine: GuiMessage.Line, pTag: GuiMessageTag): Boolean {
        return if (p_240619_ < 0.0) {
            true
        } else {
            val guiMessageTagIcon = pTag.icon()
            if (guiMessageTagIcon == null) {
                false
            } else {
                val i: Int = this.getTagIconLeft(pLine)
                val j = i + guiMessageTagIcon.width
                p_240619_ >= i.toDouble() && p_240619_ <= j.toDouble()
            }
        }
    }

    fun getTagIconLeft(pLine: GuiMessage.Line): Int {
        return Minecraft.getInstance().font.width(pLine.content()) + 4
    }

    fun handleChatQueueClicked(pMouseX: Double, pMouseY: Double): Boolean {
        return if (isChatFocused() && !Minecraft.getInstance().options.hideGui) {
            val chatlistener: ChatListener = Minecraft.getInstance().chatListener
            if (chatlistener.queueSize() == 0L) {
                false
            } else {
                val d0 = pMouseX - 2.0
                val d1: Double = Minecraft.getInstance().window.guiScaledHeight.toDouble() - pMouseY - 40.0
                if (d0 <= Mth.floor(this.getWidth().toDouble() / getScale()).toDouble() && d1 < 0.0 && d1 > Mth.floor(-9.0 * getScale())
                        .toDouble()
                ) {
                    chatlistener.acceptNextDelayedMessage()
                    true
                } else {
                    false
                }
            }
        } else {
            false
        }
    }

    fun drawTagIcon(pGuiGraphics: GuiGraphics, pLeft: Int, pBottom: Int, pTagIcon: GuiMessageTag.Icon) {
        val i = pBottom - pTagIcon.height - 1
        pTagIcon.draw(pGuiGraphics, pLeft, i)
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

    fun getClickedComponentStyleAt(pMouseX: Double, pMouseY: Double): Style? {
        val d0 = screenToChatX(pMouseX)
        val d1 = screenToChatY(pMouseY)
        val i = getMessageLineIndexAt(d0, d1)
        return if (i >= 0 && i < this.displayedMessages.size) {
            val guiMessageLine: GuiMessage.Line = this.displayedMessages[i]
            Minecraft.getInstance().font.splitter.componentStyleAtWidth(guiMessageLine.content(), Mth.floor(d0))
        } else {
            null
        }
    }

    fun resetChatScroll() {
        chatScrollbarPos = 0
        this.updateChat = false
    }

    fun scrollChat(pPosInc: Int) {
        chatScrollbarPos += pPosInc
        val displayedMessagesSize: Int = this.displayedMessages.size
        if (chatScrollbarPos > displayedMessagesSize - getLinesPerPage()) {
            chatScrollbarPos = displayedMessagesSize - getLinesPerPage()
        }
        if (chatScrollbarPos <= 0) {
            chatScrollbarPos = 0
            this.updateChat = false
        }
    }

    fun rescaleChat() {
        resetChatScroll()
        refreshTrimmedMessage()
    }

    private fun refreshTrimmedMessage() {
        this.displayedMessages.clear()
        for (i in this.messages.indices.reversed()) {
            val guiMessage: GuiMessage = this.messages[i]
            addMessage(guiMessage.content(), guiMessage.signature(), guiMessage.addedTime(), guiMessage.tag(), true)
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
        return Mth.clamp(ConfigGui.x, padding, Minecraft.getInstance().window.guiScaledWidth - 50)
    }

    fun getY(): Int {
        var y = ConfigGui.y
        if (y < 0) {
            y += Minecraft.getInstance().window.guiScaledHeight
        }
        return Mth.clamp(y, 30, Minecraft.getInstance().window.guiScaledHeight - 30)
    }


    fun getLinesPerPage(): Int {
        return getHeight() / getLineHeight()
    }

    fun getLineHeight(): Int {
        return 9
    }

}