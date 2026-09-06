package cy.jdkdigital.productivemetalworks.util;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.caffeinemc.mods.sodium.api.vertex.attributes.common.ColorAttribute;
import net.caffeinemc.mods.sodium.api.vertex.buffer.VertexBufferWriter;
import org.lwjgl.system.MemoryStack;

public class SodiumTintedVertexBuilder extends TintedVertexBuilder implements VertexBufferWriter
{
    private final VertexBufferWriter parentWriter;

    public SodiumTintedVertexBuilder(VertexConsumer inner, int tintRed, int tintGreen, int tintBlue, int tintAlpha) {
        super(inner, tintRed, tintGreen, tintBlue, tintAlpha);
        this.parentWriter = VertexBufferWriter.tryOf(inner);
    }

    @Override
    public boolean canUseIntrinsics() {
        return this.parentWriter != null;
    }

    @Override
    public void push(MemoryStack stack, long ptr, int count, VertexFormat format) {
        int colorOffset = format.getOffset(VertexFormatElement.COLOR);

        if (colorOffset >= 0) {
            long stride = format.getVertexSize();

            for (int vertex = 0; vertex < count; vertex++) {
                long colorPtr = ptr + (stride * vertex) + colorOffset;
                ColorAttribute.set(colorPtr, tintPackedColor(ColorAttribute.get(colorPtr)));
            }
        }

        this.parentWriter.push(stack, ptr, count, format);
    }
}
