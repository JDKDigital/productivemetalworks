package cy.jdkdigital.productivemetalworks.client.render.block;

import com.mojang.blaze3d.vertex.PoseStack;
import cy.jdkdigital.productivelib.client.FluidRenderHelper;
import cy.jdkdigital.productivemetalworks.ProductiveMetalworks;
import cy.jdkdigital.productivemetalworks.common.block.entity.FoundryCapacitorBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import javax.annotation.Nonnull;

public class FoundryCapacitorBlockEntityRenderer implements BlockEntityRenderer<FoundryCapacitorBlockEntity, FoundryCapacitorBlockEntityRenderer.CapacitorRenderState>
{
    private static final SpriteId POWER = new SpriteId(TextureAtlas.LOCATION_BLOCKS, Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, "block/power_level"));

    private final TextureAtlasSprite powerSprite;

    public FoundryCapacitorBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.powerSprite = context.sprites().get(POWER);
    }

    @Override
    public CapacitorRenderState createRenderState() {
        return new CapacitorRenderState();
    }

    @Override
    public void extractRenderState(FoundryCapacitorBlockEntity be, CapacitorRenderState state, float partialTicks, @Nonnull Vec3 cameraPos, ModelFeatureRenderer.@Nullable CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(be, state, crumbling);

        state.render = be.getLevel() != null && be.getEnergyHandler().getCapacityAsInt() > 0;
        if (state.render) {
            state.facing = be.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
            state.fill = 12f - (12f * be.getEnergyHandler().getAmountAsInt() / (float) be.getEnergyHandler().getCapacityAsInt());
        }
    }

    @Override
    public void submit(CapacitorRenderState state, @Nonnull PoseStack poseStack, @Nonnull SubmitNodeCollector collector, @Nonnull CameraRenderState cameraState) {
        if (state.render) {
            poseStack.pushPose();
            collector.submitCustomGeometry(poseStack, RenderTypes.entityCutout(TextureAtlas.LOCATION_BLOCKS), (pose, buffer) ->
                    FluidRenderHelper.renderTexturedSide(pose.pose(), buffer, state.facing, this.powerSprite, 6f, 1.99f * FluidRenderHelper.pixelFraction, (14f - state.fill) * FluidRenderHelper.pixelFraction, state.lightCoords, OverlayTexture.NO_OVERLAY));
            poseStack.popPose();
        }
    }

    public static class CapacitorRenderState extends BlockEntityRenderState
    {
        public boolean render;
        public Direction facing = Direction.NORTH;
        public float fill;
    }
}
