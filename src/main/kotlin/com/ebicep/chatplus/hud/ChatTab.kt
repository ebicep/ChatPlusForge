package com.ebicep.chatplus.hud

import com.ebicep.chatplus.config.ConfigChatSettingsGui
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
import net.minecraftforge.client.gui.overlay.ForgeGui
import net.minecraftforge.client.gui.overlay.IGuiOverlay
import kotlin.math.min

class ChatTab(var name: String, var pattern: String) : IGuiOverlay {

    val regex = Regex(pattern)
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
        if (!regex.matches(pChatComponent.string)) {
            return
        }
        var i = Mth.floor(ConfigChatSettingsGui.chatWidth / ChatManager.getScale())
//            if (pTag?.icon() != null) {
//                i -= pTag.icon()!!.width + 4 + 2
//            }
//        ChatPlus.LOGGER.info("i: $i")
//        ChatPlus.LOGGER.info("pChatComponent: ${pChatComponent.contents}")
        val list = ComponentRenderUtils.wrapComponents(pChatComponent, i, Minecraft.getInstance().font)
        val flag = ChatManager.isChatFocused()
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
            while (this.messages.size > ConfigChatSettingsGui.maxMessages.get()) {
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
        return pX / ChatManager.getScale()
    }

    fun screenToChatY(pY: Double): Double {
        val d0: Double = ChatManager.getY() - pY
        return d0 / (ChatManager.getScale() * ChatManager.getLineHeight().toDouble())
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
        return if (ChatManager.isChatFocused() && !Minecraft.getInstance().options.hideGui) {
            if (pMouseX >= -4.0 && pMouseX <= Mth.floor(ChatManager.getWidth().toDouble() / ChatManager.getScale()).toDouble()) {
                val i = min(ChatManager.getLinesPerPage(), this.displayedMessages.size)
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
        return if (ChatManager.isChatFocused() && !Minecraft.getInstance().options.hideGui) {
            val chatlistener: ChatListener = Minecraft.getInstance().chatListener
            if (chatlistener.queueSize() == 0L) {
                false
            } else {
                val d0 = pMouseX - 2.0
                val d1: Double = ChatManager.getY() - pMouseY
                if (d0 <= Mth.floor(ChatManager.getWidth().toDouble() / ChatManager.getScale())
                        .toDouble() && d1 < 0.0 && d1 > Mth.floor(-9.0 * ChatManager.getScale())
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

    fun resetChatScroll() {
        chatScrollbarPos = 0
        this.updateChat = false
    }

    fun scrollChat(pPosInc: Int) {
        chatScrollbarPos += pPosInc
        val displayedMessagesSize: Int = this.displayedMessages.size
        if (chatScrollbarPos > displayedMessagesSize - ChatManager.getLinesPerPage()) {
            chatScrollbarPos = displayedMessagesSize - ChatManager.getLinesPerPage()
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

    override fun render(gui: ForgeGui, guiGraphics: GuiGraphics, partialTick: Float, screenWidth: Int, screenHeight: Int) {
        val mc = Minecraft.getInstance()
        val poseStack = guiGraphics.pose()
        val isSelected = this == ChatManager.selectedTab
        val backgroundOpacity = ((if (isSelected) 255 else 100) * mc.options.textBackgroundOpacity().get()).toInt() shl 24
        val textColor = if (isSelected) 0xffffff else 0x999999

        poseStack.pushPose()
        poseStack.translate(0.0f, 0.0f, 50.0f)
        guiGraphics.fill(0, 0, mc.font.width(name) + PADDING + PADDING, 9 + PADDING + PADDING, backgroundOpacity)
        poseStack.translate(0.0f, 0.0f, 50.0f)
        guiGraphics.drawString(
            Minecraft.getInstance().font,
            name,
            PADDING,
            PADDING + PADDING / 2,
            textColor
        )
        poseStack.popPose()
    }


    companion object {
        val PADDING = 2
    }

}