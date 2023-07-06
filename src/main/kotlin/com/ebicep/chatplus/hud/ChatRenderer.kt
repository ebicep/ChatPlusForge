package com.ebicep.chatplus.hud

import com.ebicep.chatplus.MODID
import com.ebicep.chatplus.config.ConfigGui
import com.ebicep.chatplus.hud.ChatManager.chatScrollbarPos
import com.ebicep.chatplus.hud.ChatManager.displayedMessages
import net.minecraft.client.GuiMessage
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.util.Mth
import net.minecraftforge.client.gui.overlay.ForgeGui
import net.minecraftforge.client.gui.overlay.IGuiOverlay
import net.minecraftforge.fml.common.Mod
import kotlin.math.roundToInt

@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
object ChatRenderer : IGuiOverlay {

    val padding: Int = 0

    override fun render(gui: ForgeGui, guiGraphics: GuiGraphics, partialTick: Float, screenWidth: Int, screenHeight: Int) {
        if (displayedMessages.size <= 0) {
            return
        }
        val x: Int = ChatManager.getX()
        val y: Int = ChatManager.getY()
        val height: Int = ChatManager.getHeight()
        val width: Int = ChatManager.getWidth()

        val mc = Minecraft.getInstance()
        val poseStack = guiGraphics.pose()
        val linesPerPage: Int = ChatManager.getLinesPerPage()
        val chatFocused: Boolean = ChatManager.isChatFocused()
        val scale: Float = ConfigGui.scale.get().toFloat()
        val backgroundWidth = (width / scale).toInt()
        val startingYOffset = Mth.floor(y / scale)

        poseStack.pushPose()
        poseStack.scale(scale, scale, 1.0f)

        val textOpacity: Double = mc.options.chatOpacity().get() * 0.9 + 0.1
        val backGroundOpacity: Double = mc.options.textBackgroundOpacity().get()
        val lineSpacing: Double = mc.options.chatLineSpacing().get()
        val lineHeight: Int = ChatManager.getLineHeight()
        val l1 = (-8.0 * (lineSpacing + 1.0) + 4.0 * lineSpacing).roundToInt()
//        var i2 = 0
        var displayMessageIndex = 0
        while (displayMessageIndex + chatScrollbarPos < displayedMessages.size && displayMessageIndex < linesPerPage) {
            val line: GuiMessage.Line = displayedMessages[displayMessageIndex + chatScrollbarPos]
            val ticksLived: Int = gui.guiTicks - line.addedTime()
            if (ticksLived >= 200 && !chatFocused) {
                ++displayMessageIndex
                continue
            }
            val fadeOpacity = if (chatFocused) 1.0 else getTimeFactor(ticksLived)
            val backgroundColor = (255.0 * fadeOpacity * textOpacity).toInt()
            val textColor = (255.0 * fadeOpacity * backGroundOpacity).toInt()
            //                ++i2
            if (backgroundColor <= 3) {
                ++displayMessageIndex
                continue
            }
            val verticalChatOffset = startingYOffset - displayMessageIndex * lineHeight
            //how high chat is from input bar, if changed need to change queue offset
            val verticalTextOffset = verticalChatOffset + l1 //align text with background
            poseStack.pushPose()
            poseStack.translate(0.0f, 0.0f, 50.0f)
            //background
            guiGraphics.fill(-x, verticalChatOffset - lineHeight, backgroundWidth, verticalChatOffset, textColor shl 24)

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
            guiGraphics.drawString(
                Minecraft.getInstance().font,
                line.content(),
                0,
                verticalTextOffset,
                16777215 + (backgroundColor shl 24)
            )
            poseStack.popPose()
            ++displayMessageIndex
        }

        //rendering queued messages
//        val j5: Long = mc.chatListener.queueSize()
//        if (j5 > 0L) {
//            val k5 = (128.0 * textOpacity).toInt()
//            val i6 = (255.0 * backGroundOpacity).toInt()
//            guiGraphics.pose().pushPose()
//            guiGraphics.pose().translate(0.0f, scaledHeight.toFloat(), 50.0f)
//            guiGraphics.fill(-2, 0, k + 4, 9, i6 shl 24)
//            guiGraphics.pose().translate(0.0f, 0.0f, 50.0f)
//            guiGraphics.drawString(mc.font, Component.translatable("chat.queue", j5), 0, 1, 16777215 + (k5 shl 24))
//            guiGraphics.pose().popPose()
//        }

        // rendering scroll bar
//        if (chatFocused) {
//            val l5: Int = lineHeight
//            val j6 = j * l5
//            val k6 = i2 * l5
//            val i3: Int = chatScrollbarPos * k6 / j - scaledHeight
//            val l6 = k6 * k6 / j6
//            if (j6 != k6) {
//                val i7 = if (i3 > 0) 170 else 96
//                val j7 = if (newMessageSinceScroll) 13382451 else 3355562
//                val k7 = k + 4
//                guiGraphics.fill(k7, -i3, k7 + 2, -i3 - l6, j7 + (i7 shl 24))
//                guiGraphics.fill(k7 + 2, -i3, k7 + 1, -i3 - l6, 13421772 + (i7 shl 24))
//            }
//        }

        poseStack.popPose()
    }

    private fun getTimeFactor(ticksLived: Int): Double {
        var d0 = ticksLived.toDouble() / 200.0
        d0 = 1.0 - d0
        d0 *= 10.0
        d0 = Mth.clamp(d0, 0.0, 1.0)
        return d0 * d0
    }

}