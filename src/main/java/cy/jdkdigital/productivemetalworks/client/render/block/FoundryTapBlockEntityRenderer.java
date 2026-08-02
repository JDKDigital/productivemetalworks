package cy.jdkdigital.productivemetalworks.client.render.block;

import com.mojang.blaze3d.vertex.PoseStack;
import cy.jdkdigital.productivelib.client.FluidRenderHelper;
import cy.jdkdigital.productivemetalworks.common.block.entity.FoundryTapBlockEntity;
import cy.jdkdigital.productivemetalworks.registry.MetalworksRegistrator;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jspecify.annotations.Nullable;

import javax.annotation.Nonnull;

public class FoundryTapBlockEntityRenderer implements BlockEntityRenderer<FoundryTapBlockEntity, FoundryTapBlockEntityRenderer.TapRenderState>
{
    public FoundryTapBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public TapRenderState createRenderState() {
        return new TapRenderState();
    }

    @Override
    public void extractRenderState(FoundryTapBlockEntity be, TapRenderState state, float partialTicks, @Nonnull Vec3 cameraPos, ModelFeatureRenderer.@Nullable CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(be, state, crumbling);

        state.active = be.getLevel() != null && be.isActive;
        if (state.active) {
            FluidStack fluidStack = new FluidStack(BuiltInRegistries.FLUID.byId(be.fluidId), 1);
            state.active = !fluidStack.isEmpty();
            if (state.active) {
                state.fluidStack = fluidStack;
                state.fluidY = FluidRenderHelper.halfPixelFraction + 5f * FluidRenderHelper.pixelFraction;
                state.basinBelow = be.getLevel().getBlockState(be.getBlockPos().below()).is(MetalworksRegistrator.CASTING_BASIN);
            }
        }
    }

    @Override
    public void submit(TapRenderState state, @Nonnull PoseStack poseStack, @Nonnull SubmitNodeCollector collector, @Nonnull CameraRenderState cameraState) {
        if (state.active) {
            poseStack.pushPose();
            collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucent(TextureAtlas.LOCATION_BLOCKS), (pose, buffer) ->
                    FluidRenderHelper.renderCenteredFluidColumn(pose.pose(), buffer, 3f, 0, state.fluidY, state.fluidStack, state.lightCoords, OverlayTexture.NO_OVERLAY, 1.0f));
            if (state.basinBelow) {
                collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucent(TextureAtlas.LOCATION_BLOCKS), (pose, buffer) ->
                        FluidRenderHelper.renderCenteredFluidColumn(pose.pose(), buffer, 3f, -0.99f, 0, state.fluidStack, state.lightCoords, OverlayTexture.NO_OVERLAY, 1.0f));
            }
            poseStack.popPose();
        }
    }

    public static class TapRenderState extends BlockEntityRenderState
    {
        public boolean active;
        public boolean basinBelow;
        public FluidStack fluidStack = FluidStack.EMPTY;
        public float fluidY;
    }
}
