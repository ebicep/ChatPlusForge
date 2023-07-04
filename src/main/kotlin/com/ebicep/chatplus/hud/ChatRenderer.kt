package com.ebicep.chatplus.hud

import com.ebicep.chatplus.MODID
import com.ebicep.chatplus.hud.ChatManager.chatScrollbarPos
import com.ebicep.chatplus.hud.ChatManager.displayedMessages
import com.ebicep.chatplus.hud.ChatManager.newMessageSinceScroll
import com.mojang.blaze3d.platform.Window
import net.minecraft.client.GuiMessage
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import net.minecraft.util.Mth
import net.minecraftforge.client.gui.overlay.ForgeGui
import net.minecraftforge.client.gui.overlay.IGuiOverlay
import net.minecraftforge.fml.common.Mod
import kotlin.math.roundToInt

@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
object ChatRenderer : IGuiOverlay {

    override fun render(gui: ForgeGui, guiGraphics: GuiGraphics, partialTick: Float, screenWidth: Int, screenHeight: Int) {
        val mc = Minecraft.getInstance()
        val poseStack = guiGraphics.pose()
        val window: Window = mc.window
//        val mouseX = Mth.floor(mc.mouseHandler.xpos() * window.guiScaledWidth / window.screenWidth)
//        val mouseY = Mth.floor(mc.mouseHandler.ypos() * window.guiScaledHeight / window.screenHeight)
//        val j1: Int = ChatManager.getMessageEndIndexAt(
//            ChatManager.screenToChatX(mouseX.toDouble()),
//            ChatManager.screenToChatY(mouseY.toDouble())
//        )
        val linesPerPage: Int = ChatManager.getLinesPerPage()
        val j: Int = displayedMessages.size
        if (j <= 0) {
            return
        }
        val chatFocused: Boolean = ChatManager.isChatFocused()
        val scale = 1f
        val k = Mth.ceil(ChatManager.getWidth() / scale)
        val guiHeight: Int = guiGraphics.guiHeight()
        poseStack.pushPose()
        poseStack.scale(scale, scale, 1.0f)
        poseStack.translate(1.0f, 0.0f, 0.0f) // text shift right a little
        val scaledHeight = Mth.floor((guiHeight - 40).toFloat() / scale)

        val textOpacity: Double = mc.options.chatOpacity().get() * 0.9 + 0.1
        val backGroundOpacity: Double = mc.options.textBackgroundOpacity().get()
        val lineSpacing: Double = mc.options.chatLineSpacing().get()
        val lineHeight: Int = ChatManager.getLineHeight()
        val l1 = (-8.0 * (lineSpacing + 1.0) + 4.0 * lineSpacing).roundToInt()
        var i2 = 0
        var j2 = 0
        while (j2 + chatScrollbarPos < displayedMessages.size && j2 < linesPerPage) {
            val k2: Int = j2 + chatScrollbarPos
            val line: GuiMessage.Line = displayedMessages[k2]
            val ticksLived: Int = gui.guiTicks - line.addedTime()
            if (ticksLived < 200 || chatFocused) {
                val d3 = if (chatFocused) 1.0 else getTimeFactor(ticksLived)
                val j3 = (255.0 * d3 * textOpacity).toInt()
                val k3 = (255.0 * d3 * backGroundOpacity).toInt()
                ++i2
                if (j3 > 3) {
                    val verticalChatOffset = scaledHeight - j2 * lineHeight //how high chat is from input bar, if changed need to change
                    // queue offset
                    val verticalTextOffset = verticalChatOffset + l1 //align text with background
                    poseStack.pushPose()
                    poseStack.translate(0.0f, 0.0f, 50.0f)
                    //background
                    guiGraphics.fill(-4, verticalChatOffset - lineHeight, k + 4 + 4, verticalChatOffset, k3 shl 24)

                    //thing on the right (server message cannot be reported)
                    //val guimessagetag: GuiMessageTag? = line.tag()
                    //                    if (guimessagetag != null) {
                    //                        val k4 = guimessagetag.indicatorColor() or (j3 shl 24)
                    //                        guiGraphics.fill(-4, i4 - lineHeight, -2, i4, k4)
                    //                        if (k2 == j1 && guimessagetag.icon() != null) {
                    //                            val l4: Int = ChatManager.getTagIconLeft(line)
                    //                            val i5 = j4 + 9
                    //                            drawTagIcon(guiGraphics, l4, i5, guimessagetag.icon()!!)
                    //                        }
                    //                    }

                    poseStack.translate(0.0f, 0.0f, 50.0f)
                    guiGraphics.drawString(Minecraft.getInstance().font, line.content(), 0, verticalTextOffset, 16777215 + (j3 shl 24))
                    poseStack.popPose()
                }
            }
            ++j2
        }

        val j5: Long = mc.chatListener.queueSize()
        if (j5 > 0L) {
            val k5 = (128.0 * textOpacity).toInt()
            val i6 = (255.0 * backGroundOpacity).toInt()
            guiGraphics.pose().pushPose()
            guiGraphics.pose().translate(0.0f, scaledHeight.toFloat(), 50.0f)
            guiGraphics.fill(-2, 0, k + 4, 9, i6 shl 24)
            guiGraphics.pose().translate(0.0f, 0.0f, 50.0f)
            guiGraphics.drawString(mc.font, Component.translatable("chat.queue", j5), 0, 1, 16777215 + (k5 shl 24))
            guiGraphics.pose().popPose()
        }

        if (chatFocused) {
            val l5: Int = lineHeight
            val j6 = j * l5
            val k6 = i2 * l5
            val i3: Int = chatScrollbarPos * k6 / j - scaledHeight
            val l6 = k6 * k6 / j6
            if (j6 != k6) {
                val i7 = if (i3 > 0) 170 else 96
                val j7 = if (newMessageSinceScroll) 13382451 else 3355562
                val k7 = k + 4
                guiGraphics.fill(k7, -i3, k7 + 2, -i3 - l6, j7 + (i7 shl 24))
                guiGraphics.fill(k7 + 2, -i3, k7 + 1, -i3 - l6, 13421772 + (i7 shl 24))
            }
        }

        poseStack.popPose()
    }

    private fun getTimeFactor(pCounter: Int): Double {
        var d0 = pCounter.toDouble() / 200.0
        d0 = 1.0 - d0
        d0 *= 10.0
        d0 = Mth.clamp(d0, 0.0, 1.0)
        return d0 * d0
    }

}