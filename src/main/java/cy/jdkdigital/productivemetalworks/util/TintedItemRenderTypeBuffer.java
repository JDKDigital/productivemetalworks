package cy.jdkdigital.productivemetalworks.util;

import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;

import java.util.Set;

public class TintedItemRenderTypeBuffer implements MultiBufferSource
{
    private static final Set<String> MAKE_TRANSPARENT = ImmutableSet.of("entity_solid", "entity_cutout", "entity_cutout_no_cull", "entity_translucent", "entity_no_outline");

    private final MultiBufferSource inner;
    private final int red;
    private final int green;
    private final int blue;
    private final int alpha;

    /**
     * Creates a new instance of this class
     * @param inner        Base render type buffer
     * @param alpha        Opacity of the item from 0 to 255. 255 is the end of the animation.
     * @param temperature  Temperature of the item from 0 to 255. 0 is the end of the animation when the item is "cool"/untinted
     */
    public TintedItemRenderTypeBuffer(MultiBufferSource inner, int alpha, int temperature) {
        this.inner = inner;
        // alpha is a direct fade from 0 to 255
        this.alpha = Mth.clamp(alpha, 0, 0xFF);
        // RGB based on temperature, fades from 0xB06020 tint to 0xFFFFFF
        temperature = Mth.clamp(temperature, 0, 0xFF);
        this.red   = 0xFF - (temperature * (0xFF - 0xB0) / 0xFF);
        this.green = 0xFF - (temperature * (0xFF - 0x60) / 0xFF);
        this.blue  = 0xFF - (temperature * (0xFF - 0x20) / 0xFF);
    }

    @Override
    public VertexConsumer getBuffer(RenderType renderType) {
        if (alpha < 255 && MAKE_TRANSPARENT.contains(renderType.name) && renderType instanceof RenderType.CompositeRenderType composite && composite.state.textureState instanceof RenderStateShard.TextureStateShard textureState) {
            ResourceLocation texture = textureState.texture.orElse(InventoryMenu.BLOCK_ATLAS);
            renderType = RenderType.entityTranslucentCull(texture);
        }

        return new TintedVertexBuilder(inner.getBuffer(renderType), red, green, blue, alpha);
    }
}