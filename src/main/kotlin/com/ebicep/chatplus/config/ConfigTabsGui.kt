package com.ebicep.chatplus.config

import com.ebicep.chatplus.hud.ChatManager
import com.ebicep.chatplus.hud.ChatTab
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.*
import net.minecraft.client.gui.layouts.FrameLayout
import net.minecraft.client.gui.layouts.GridLayout
import net.minecraft.client.gui.layouts.SpacerElement
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component
import java.util.function.Consumer

class ConfigTabsGui(private val lastScreen: Screen?) : Screen(Component.translatable("chatPlus.chatTabs")) {

    companion object {
        const val MAX_TABS = 8
    }

    data class TabWidgets(
        val nameEditBox: EditBox,
        val patternEditBox: EditBox,
    )

    private val tabTickableWidgets: MutableMap<ChatTab, TabWidgets> = HashMap()
    private var list: OptionsList? = null

    override fun init() {
        // list only for auto renderTopAndBottom
        this.list = OptionsList(
            minecraft!!, width, height,
            32,
            height - 32,
            25
        )

        val gridlayout = GridLayout()
        gridlayout.defaultCellSetting()
            .paddingHorizontal(4)
            .paddingBottom(4)
            .alignHorizontallyCenter()
        val rowHelper = gridlayout.createRowHelper(7)
        ChatManager.chatTabs.forEach { category ->
            val upButton = Button.builder(Component.translatable("chatPlus.chatTabs.up")) {
                ChatManager.chatTabs.moveUp(category)
                refresh()
            }
                .width(45)
                .tooltip(Tooltip.create(Component.translatable("chatPlus.chatTabs.up.tooltip")))
                .build()
            val downButton = Button.builder(Component.translatable("chatPlus.chatTabs.down")) {
                ChatManager.chatTabs.moveDown(category)
                refresh()
            }
                .width(45)
                .tooltip(Tooltip.create(Component.translatable("chatPlus.chatTabs.down.tooltip")))
                .build()
            val nameEditBox = CategoryEditBox(
                75,
                "chatPlus.chatTabs.name",
                category.name,
                10
            ) { category.name = it }
            nameEditBox.setHint(Component.translatable("chatPlus.chatTabs.name"))
            val patternEditBox = CategoryEditBox(
                140,
                "chatPlus.chatTabs.pattern",
                category.pattern,
                100
            ) { category.pattern = it }
            patternEditBox.setHint(Component.translatable("chatPlus.chatTabs.pattern"))

            rowHelper.addChild(upButton)
            rowHelper.addChild(downButton)
            rowHelper.addChild(SpacerElement.width(3))
            rowHelper.addChild(nameEditBox)
            rowHelper.addChild(patternEditBox)
            rowHelper.addChild(SpacerElement.width(6))

            if (ChatManager.selectedTab != category) {
                val deleteButton = Button.builder(Component.translatable("chatPlus.chatTabs.delete")) {
                    ChatManager.chatTabs.remove(category)
                    refresh()
                }
                    .width(80)
                    .tooltip(Tooltip.create(Component.translatable("chatPlus.chatTabs.delete.tooltip")))
                    .build()
                rowHelper.addChild(deleteButton)
            } else {
                rowHelper.addChild(SpacerElement.width(6))
            }

            tabTickableWidgets[category] = TabWidgets(nameEditBox, patternEditBox)
        }

        gridlayout.arrangeElements()
        FrameLayout.alignInRectangle(gridlayout, 0, 40, width, height, 0.5f, 0.0f)
        gridlayout.visitWidgets { widget: AbstractWidget -> addRenderableWidget(widget) }

        if (ChatManager.chatTabs.size < MAX_TABS) {
            this.addRenderableWidget(
                Button.builder(Component.translatable("chatPlus.chatTabs.create")) {
                    ChatManager.chatTabs.add(ChatTab("", ""))
                    refresh()
                }
                    .bounds(this.width / 2 - 155, this.height - 29, 150, 20)
                    .tooltip(Tooltip.create(Component.translatable("chatPlus.chatTabs.create.tooltip")))
                    .build()
            )
        }
        this.addRenderableWidget(
            Button.builder(CommonComponents.GUI_DONE) {
                minecraft!!.options.save()
                minecraft!!.setScreen(lastScreen)
            }.bounds(this.width / 2 - 155 + 160, this.height - 29, 150, 20).build()
        )
    }

    override fun render(pGuiGraphics: GuiGraphics, pMouseX: Int, pMouseY: Int, pPartialTick: Float) {
        renderDirtBackground(pGuiGraphics)
        list!!.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick)
        pGuiGraphics.drawCenteredString(font, title, width / 2, 20, 16777215)
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick)
    }

    override fun tick() {
        tabTickableWidgets.forEach { (_, widgets) ->
            widgets.nameEditBox.tick()
            widgets.patternEditBox.tick()
        }
    }

    override fun onClose() {
        minecraft!!.setScreen(lastScreen)
    }

    fun refresh() {
        minecraft!!.setScreen(ConfigTabsGui(lastScreen))
        Config.refreshTabs()
    }

    fun createStringWidget(px: Int, py: Int, message: String, font: Font): StringWidget {
        val translatable = Component.translatable(message)
        return StringWidget(px, py, font.width(translatable.visualOrderText) + 4, 20, translatable, font)
    }

    inner class CategoryEditBox(pWidth: Int, translatable: String, name: String, maxLength: Int, responder: Consumer<String>) :
        EditBox(font, 0, 50, pWidth, 20, Component.translatable(translatable)) {

        init {
            this.value = name
            this.setMaxLength(maxLength)
            this.setCanLoseFocus(true)
            this.setResponder {
                responder.accept(it)
                Config.refreshTabs()
            }
        }

    }

    //kotlin function to move element in list up, cycle to last element if at first element
    fun <T> MutableList<T>.moveUp(element: T) {
        val index = this.indexOf(element)
        if (index > 0) {
            this[index] = this[index - 1].also { this[index - 1] = this[index] }
        } else {
            this[index] = this[this.size - 1].also { this[this.size - 1] = this[index] }
        }
    }

    //kotlin function to move element in list down cycle to first element if at last element
    fun <T> MutableList<T>.moveDown(element: T) {
        val index = this.indexOf(element)
        if (index < this.size - 1) {
            this[index] = this[index + 1].also { this[index + 1] = this[index] }
        } else {
            this[index] = this[0].also { this[0] = this[index] }
        }
    }


}
