package hud;

import net.minecraft.client.GuiMessage;

public record ChatPlusGuiMessageLine(GuiMessage.Line line, String content) {
}
