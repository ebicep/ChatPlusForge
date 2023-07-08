package hud;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

// dont ask
public record ChatTabRecord(String name, String pattern) {

    public static Codec<ChatTabRecord> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.STRING.fieldOf("name").forGetter(ChatTabRecord::name),
                    Codec.STRING.fieldOf("pattern").forGetter(ChatTabRecord::pattern)
            ).apply(instance, ChatTabRecord::new)
    );


}
