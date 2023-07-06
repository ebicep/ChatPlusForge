package com.ebicep.chatplus.hud

import com.ebicep.chatplus.config.ChatPlusKeyBindings
import com.mojang.blaze3d.platform.InputConstants
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.CommandSuggestions
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.narration.NarratedElementType
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.Style
import net.minecraft.util.Mth
import net.minecraft.util.StringUtil
import org.apache.commons.lang3.StringUtils

class ChatPlusScreen(pInitial: String) : Screen(Component.translatable("chat_plus_screen.title")) {

    val MOUSE_SCROLL_SPEED = 7.0
    private val USAGE_TEXT: Component = Component.translatable("chat_plus_screen.usage")
    private val TOOLTIP_MAX_WIDTH = 210
    private var historyBuffer = ""

    /**
     * keeps position of which chat message you will select when you press up, (does not increase for duplicated messages
     * sent immediately after each other)
     */
    private var historyPos = -1

    /** Chat entry field  */
    private var input: EditBox? = null

    /** is the text that appears when you press the chat key and the input box appears pre-filled  */
    private var initial: String = pInitial
    var commandSuggestions: CommandSuggestions? = null

    override fun init() {
        historyPos = ChatManager.sentMessages.size
        input = object : EditBox(minecraft!!.fontFilterFishy, 4, height - 12, width - 4, 12, Component.translatable("chat_plus.editBox")) {
            override fun createNarrationMessage(): MutableComponent {
                return super.createNarrationMessage().append(commandSuggestions!!.narrationMessage)
            }
        }
        val editBox = input as EditBox
        editBox.setMaxLength(256)
        editBox.setBordered(false)
        editBox.value = initial
        editBox.setResponder { p_95611_: String -> onEdited(p_95611_) }
        editBox.setCanLoseFocus(false)
        addWidget(editBox)
        commandSuggestions = CommandSuggestions(minecraft!!, this, editBox, font, false, false, 1, 10, true, -805306368)
        commandSuggestions!!.updateCommandInfo()
        setInitialFocus(editBox)
    }

    override fun resize(pMinecraft: Minecraft, pWidth: Int, pHeight: Int) {
        val s = input!!.value
        this.init(pMinecraft, pWidth, pHeight)
        setChatLine(s)
        commandSuggestions!!.updateCommandInfo()
    }

    override fun removed() {
        ChatManager.resetChatScroll()
    }

    override fun tick() {
        input!!.tick()
    }

    private fun onEdited(p_95611_: String) {
        val s = input!!.value
        commandSuggestions!!.setAllowSuggestions(s != initial)
        commandSuggestions!!.updateCommandInfo()
    }

    override fun keyPressed(pKeyCode: Int, pScanCode: Int, pModifiers: Int): Boolean {
        return if (commandSuggestions!!.keyPressed(pKeyCode, pScanCode, pModifiers)) {
            true
        } else if (super.keyPressed(pKeyCode, pScanCode, pModifiers)) {
            true
        } else if (pKeyCode == 256) { // escape
            minecraft!!.setScreen(null as Screen?)
            true
        } else if (pKeyCode != 257 && pKeyCode != 335) { // enter
            when (pKeyCode) {
                // cycle through own sent messages
                265 -> { // up arrow
                    moveInHistory(-1)
                    true
                }

                264 -> { // down arrow
                    moveInHistory(1)
                    true
                }
                // cycle through displayed chat messages
                266 -> { // page up
                    ChatManager.scrollChat(ChatManager.getLinesPerPage() - 1)
                    true
                }

                267 -> { // page down
                    ChatManager.scrollChat(-ChatManager.getLinesPerPage() + 1)
                    true
                }

                else -> {
                    false
                }
            }
        } else {
            if (handleChatInput(input!!.value, true)) {
                minecraft!!.setScreen(null as Screen?)
            }
            true
        }
    }

    override fun mouseScrolled(pMouseX: Double, pMouseY: Double, pDelta: Double): Boolean {
        var delta = Mth.clamp(pDelta, -1.0, 1.0)
        return if (commandSuggestions!!.mouseScrolled(delta)) {
            true
        } else {
            // control = no scroll
            // shift = fine scroll
            // alt = triple scroll
            val window = Minecraft.getInstance().window.window
            if (InputConstants.isKeyDown(window, ChatPlusKeyBindings.NO_SCOLL.key.value)) {
                return true
            }
            if (InputConstants.isKeyDown(window, ChatPlusKeyBindings.LARGE_SCROLL.key.value)) {
                delta *= 21.0
            } else if (!InputConstants.isKeyDown(window, ChatPlusKeyBindings.FINE_SCROLL.key.value)) {
                delta *= 7.0
            }
            ChatManager.scrollChat(delta.toInt())
            true
        }
    }

    override fun mouseClicked(pMouseX: Double, pMouseY: Double, pButton: Int): Boolean {
        return if (commandSuggestions!!.mouseClicked(pMouseX.toInt().toDouble(), pMouseY.toInt().toDouble(), pButton)) {
            true
        } else {
            if (pButton == 0) {
                if (ChatManager.handleChatQueueClicked(pMouseX, pMouseY)) {
                    return true
                }
                val style = getComponentStyleAt(pMouseX, pMouseY)
                if (style != null && handleComponentClicked(style)) {
                    initial = input!!.value
                    return true
                }
            }
            if (input!!.mouseClicked(pMouseX, pMouseY, pButton)) true else super.mouseClicked(pMouseX, pMouseY, pButton)
        }
    }

    override fun insertText(pText: String, pOverwrite: Boolean) {
        if (pOverwrite) {
            input!!.value = pText
        } else {
            input!!.insertText(pText)
        }
    }

    /**
     * Input is relative and is applied directly to the sentHistoryCursor so -1 is the previous message, 1 is the next
     * message from the current cursor position.
     */
    fun moveInHistory(pMsgPos: Int) {
        var i = historyPos + pMsgPos
        val j = ChatManager.sentMessages.size
        i = Mth.clamp(i, 0, j)
        if (i != historyPos) {
            if (i == j) {
                historyPos = j
                input!!.value = historyBuffer
            } else {
                if (historyPos == j) {
                    historyBuffer = input!!.value
                }
                input!!.value = ChatManager.sentMessages[i]
                commandSuggestions!!.setAllowSuggestions(false)
                historyPos = i
            }
        }
    }

    override fun render(guiGraphics: GuiGraphics, pMouseX: Int, pMouseY: Int, pPartialTick: Float) {
        // input box
        guiGraphics.fill(0, height - 14, width, height, minecraft!!.options.getBackgroundColor(Int.MIN_VALUE))
        guiGraphics.pose().pushPose()
        guiGraphics.pose().translate(-2.0, 2.0, 0.0)
        input!!.render(guiGraphics, pMouseX, pMouseY, pPartialTick)
        guiGraphics.pose().popPose()

        super.render(guiGraphics, pMouseX, pMouseY, pPartialTick)

        // brigadier
        commandSuggestions!!.render(guiGraphics, pMouseX, pMouseY)

        // hoverables
        val guiMessageTag = ChatManager.getMessageTagAt(pMouseX.toDouble(), pMouseY.toDouble())
        if (guiMessageTag?.text() != null) {
            guiGraphics.renderTooltip(font, font.split(guiMessageTag.text()!!, 210), pMouseX, pMouseY)
        } else {
            val style = getComponentStyleAt(pMouseX.toDouble(), pMouseY.toDouble())
            if (style != null && style.hoverEvent != null) {
                guiGraphics.renderComponentHoverEffect(font, style, pMouseX, pMouseY)
            }
        }


    }

    override fun isPauseScreen(): Boolean {
        return false
    }

    private fun setChatLine(pChatLine: String) {
        input!!.value = pChatLine
    }

    override fun updateNarrationState(pOutput: NarrationElementOutput) {
        pOutput.add(NarratedElementType.TITLE, getTitle())
        pOutput.add(NarratedElementType.USAGE, USAGE_TEXT)
        val s = input!!.value
        if (s.isNotEmpty()) {
            pOutput.nest().add(NarratedElementType.TITLE, Component.translatable("chat_plus_screen.message", s))
        }
    }

    private fun getComponentStyleAt(pMouseX: Double, pMouseY: Double): Style? {
        return ChatManager.getClickedComponentStyleAt(pMouseX, pMouseY)
    }

    fun handleChatInput(pInput: String, pAddToRecentChat: Boolean): Boolean {
        val input = normalizeChatMessage(pInput)
        return if (input.isEmpty()) {
            true
        } else {
            if (pAddToRecentChat) {
                ChatManager.addSentMessage(input)
            }
            if (input.startsWith("/")) {
                minecraft!!.player!!.connection.sendCommand(input.substring(1))
            } else {
                minecraft!!.player!!.connection.sendChat(input)
            }
            minecraft!!.screen === this // FORGE: Prevent closing the screen if another screen has been opened.
        }
    }

    fun normalizeChatMessage(pMessage: String): String {
        return StringUtil.trimChatMessage(StringUtils.normalizeSpace(pMessage.trim { it <= ' ' }))
    }


}