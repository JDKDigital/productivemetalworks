package cy.jdkdigital.productivemetalworks.client.render.block;

import com.mojang.blaze3d.vertex.PoseStack;
import cy.jdkdigital.productivelib.client.FluidRenderHelper;
import cy.jdkdigital.productivemetalworks.common.block.entity.FoundryTankBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jspecify.annotations.Nullable;

import javax.annotation.Nonnull;

public class FoundryTankBlockEntityRenderer implements BlockEntityRenderer<FoundryTankBlockEntity, FoundryTankBlockEntityRenderer.TankRenderState>
{
    private static final int[] SINGLE = new int[]{0};

    public FoundryTankBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public TankRenderState createRenderState() {
        return new TankRenderState();
    }

    @Override
    public void extractRenderState(FoundryTankBlockEntity be, TankRenderState state, float partialTicks, @Nonnull Vec3 cameraPos, ModelFeatureRenderer.@Nullable CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(be, state, crumbling);

        FluidStack fluidStack = be.getFluidHandler().getFluid();
        state.hasFluid = be.getLevel() != null && !fluidStack.isEmpty();
        if (state.hasFluid) {
            state.fluidStack = fluidStack.copy();
            float fillProgress = (float) fluidStack.getAmount() / (float) be.getFluidHandler().getCapacity();
            state.fluidY = FluidRenderHelper.halfPixelFraction + (fillProgress * 15.01f) * FluidRenderHelper.pixelFraction;
        }
    }

    @Override
    public void submit(TankRenderState state, @Nonnull PoseStack poseStack, @Nonnull SubmitNodeCollector collector, @Nonnull CameraRenderState cameraState) {
        if (state.hasFluid) {
            poseStack.pushPose();
            collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucent(TextureAtlas.LOCATION_BLOCKS), (pose, buffer) ->
                    FluidRenderHelper.renderFullFluidLayer(pose.pose(), buffer, 0, state.fluidY, SINGLE, SINGLE, state.fluidStack, state.lightCoords, OverlayTexture.NO_OVERLAY, 1.0f, 0.01f));
            poseStack.popPose();
        }
    }

    public static class TankRenderState extends BlockEntityRenderState
    {
        public boolean hasFluid;
        public FluidStack fluidStack = FluidStack.EMPTY;
        public float fluidY;
    }
}
