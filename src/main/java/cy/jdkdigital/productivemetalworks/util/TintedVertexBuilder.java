package cy.jdkdigital.productivemetalworks.util;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.util.FastColor.ABGR32;
import net.minecraft.util.FastColor.ARGB32;
import net.neoforged.neoforge.client.model.pipeline.VertexConsumerWrapper;

public class TintedVertexBuilder extends VertexConsumerWrapper
{
    // Probes for the API class rather than a modid, so Embeddium and other Sodium forks count too.
    private static final boolean SODIUM_API_PRESENT = isSodiumApiPresent();

    private final int packedTint;

    public TintedVertexBuilder(VertexConsumer inner, int tintRed, int tintGreen, int tintBlue, int tintAlpha) {
        super(inner);
        this.packedTint = ABGR32.color(tintAlpha, tintBlue, tintGreen, tintRed);
    }

    // Must return VertexConsumer, not TintedVertexBuilder, to prevent verifier loading the Sodium version, which would error w/o Sodium
    public static VertexConsumer create(VertexConsumer inner, int tintRed, int tintGreen, int tintBlue, int tintAlpha) {
        if (SODIUM_API_PRESENT) {
            return new SodiumTintedVertexBuilder(inner, tintRed, tintGreen, tintBlue, tintAlpha);
        }

        return new TintedVertexBuilder(inner, tintRed, tintGreen, tintBlue, tintAlpha);
    }

    private static boolean isSodiumApiPresent() {
        try {
            Class.forName("net.caffeinemc.mods.sodium.api.vertex.buffer.VertexBufferWriter", false, TintedVertexBuilder.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }

    protected int tintPackedColor(int abgr) {
        // Yes, it looks suspicious, but ARGB32.multiply is valid for ABGR32 (which has no multiply of its own)
        return ARGB32.multiply(abgr, packedTint);
    }

    @Override
    public VertexConsumer setColor(int red, int green, int blue, int alpha) {
        int tinted = tintPackedColor(ABGR32.color(alpha, blue, green, red));
        return parent.setColor(ABGR32.red(tinted), ABGR32.green(tinted), ABGR32.blue(tinted), ABGR32.alpha(tinted));
    }
}
