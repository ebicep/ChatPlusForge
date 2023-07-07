package com.ebicep.chatplus.hud

import com.ebicep.chatplus.config.ChatPlusKeyBindings
import com.ebicep.chatplus.config.ConfigGui
import com.ebicep.chatplus.hud.ChatManager.selectedCategory
import com.mojang.blaze3d.platform.InputConstants
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.GuiMessage
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.util.Mth
import net.minecraftforge.client.gui.overlay.ForgeGui
import net.minecraftforge.client.gui.overlay.IGuiOverlay
import kotlin.math.roundToInt

object ChatRenderer : IGuiOverlay {

    val categoryYOffset = 1 // offset from text box
    val categoryXBetween = 1 // space between categories
    val renderingMovingSize = 3 // width/length of box when rendering moving chat

    override fun render(gui: ForgeGui, guiGraphics: GuiGraphics, partialTick: Float, screenWidth: Int, screenHeight: Int) {
        val x: Int = ChatManager.getX()
        val y: Int = ChatManager.getY()
        val height: Int = ChatManager.getHeight()
        val backgroundWidth: Int = ChatManager.getBackgroundWidth()

        val mc = Minecraft.getInstance()
        val poseStack = guiGraphics.pose()
        val linesPerPage: Int = ChatManager.getLinesPerPage()
        val chatFocused: Boolean = ChatManager.isChatFocused()
        val scale: Float = ConfigGui.scale.get().toFloat()
        val startingYOffset = Mth.floor(y / scale)

        val textOpacity: Double = mc.options.chatOpacity().get() * 0.9 + 0.1
        val backGroundOpacity: Double = mc.options.textBackgroundOpacity().get()
        val lineSpacing: Double = mc.options.chatLineSpacing().get()
        val lineHeight: Int = ChatManager.getLineHeight()
        val l1 = (-8.0 * (lineSpacing + 1.0) + 4.0 * lineSpacing).roundToInt()
//        var i2 = 0

        // categories
        if (chatFocused) {
            poseStack.pushPose()
            poseStack.translate(x.toFloat(), y.toFloat() + categoryYOffset, 0f)
            ChatManager.chatCategories.forEach {
                it.render(gui, guiGraphics, partialTick, screenWidth, screenHeight)
                poseStack.translate(
                    mc.font.width(it.name).toFloat() + ChatCategory.PADDING + ChatCategory.PADDING + categoryXBetween,
                    0f,
                    0f
                )
            }
            poseStack.popPose()
        }

        if (selectedCategory.displayedMessages.size <= 0) {
            if (InputConstants.isKeyDown(mc.window.window, ChatPlusKeyBindings.MOVE_CHAT.key.value)) {
                guiGraphics.fill(
                    x,
                    y - height,
                    x + backgroundWidth,
                    y,
                    (255 * backGroundOpacity).toInt() shl 24
                )
            }
            renderMoving(
                poseStack,
                guiGraphics,
                x,
                y,
                height,
                backgroundWidth
            )
            return
        }

        val backgroundWidthRescaled = (backgroundWidth / scale).roundToInt()

        poseStack.pushPose()
        poseStack.scale(scale, scale, 1.0f)


        var displayMessageIndex = 0
        while (displayMessageIndex + selectedCategory.chatScrollbarPos < selectedCategory.displayedMessages.size && displayMessageIndex < linesPerPage) {
            val line: GuiMessage.Line = selectedCategory.displayedMessages[displayMessageIndex + selectedCategory.chatScrollbarPos]
            val ticksLived: Int = gui.guiTicks - line.addedTime()
            if (ticksLived >= 200 && !chatFocused) {
                ++displayMessageIndex
                continue
            }
            val fadeOpacity = if (chatFocused) 1.0 else getTimeFactor(ticksLived)
            val textColor = (255.0 * fadeOpacity * textOpacity).toInt()
            val backgroundColor = (255.0 * fadeOpacity * backGroundOpacity).toInt()
            //                ++i2
            if (textColor <= 3) {
                ++displayMessageIndex
                continue
            }
            val verticalChatOffset = startingYOffset - displayMessageIndex * lineHeight
            //how high chat is from input bar, if changed need to change queue offset
            val verticalTextOffset = verticalChatOffset + l1 //align text with background
            poseStack.pushPose()
            poseStack.translate(0.0f, 0.0f, 50.0f)
            //background
            guiGraphics.fill(
                (x / scale).roundToInt(),
                verticalChatOffset - lineHeight,
                (x / scale).roundToInt() + backgroundWidthRescaled,
                verticalChatOffset,
                backgroundColor shl 24
            )

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

            poseStack.translate(0f, 0f, 50f)
            guiGraphics.drawString(
                Minecraft.getInstance().font,
                line.content(),
                (x / scale).roundToInt(),
                verticalTextOffset,
                16777215 + (textColor shl 24)
            )
            poseStack.popPose()
            ++displayMessageIndex
        }

        if (InputConstants.isKeyDown(mc.window.window, ChatPlusKeyBindings.MOVE_CHAT.key.value)) {
            guiGraphics.fill(
                (x / scale).roundToInt(),
                (startingYOffset - height / scale).roundToInt(),
                (x / scale).roundToInt() + backgroundWidthRescaled,
                startingYOffset - displayMessageIndex * lineHeight,
                (255 * backGroundOpacity).toInt() shl 24
            )
            renderMoving(
                poseStack,
                guiGraphics,
                (x / scale).roundToInt(),
                startingYOffset,
                (height / scale).roundToInt(),
                backgroundWidthRescaled
            )
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

//        val mouseX = Mth.floor(mc.mouseHandler.xpos() * window.guiScaledWidth / window.screenWidth)
//        val mouseY = Mth.floor(mc.mouseHandler.ypos() * window.guiScaledHeight / window.screenHeight)
    }

    private fun renderMoving(
        poseStack: PoseStack,
        guiGraphics: GuiGraphics,
        x: Int,
        y: Int,
        height: Int,
        backgroundWidth: Int
    ) {
        poseStack.pushPose()
        poseStack.translate(0f, 0f, 200f)
        if (ChatPlusScreen.movingChatX) {
            guiGraphics.fill(
                x + backgroundWidth - renderingMovingSize,
                y - height,
                x + backgroundWidth,
                y,
                0xFFFFFFFF.toInt()
            )
        }
        if (ChatPlusScreen.movingChatY) {
            guiGraphics.fill(
                x,
                y - height,
                x + backgroundWidth,
                y - height + renderingMovingSize,
                0xFFFFFFFF.toInt()
            )
        }
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