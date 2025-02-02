package cy.jdkdigital.productivemetalworks.event;

import cy.jdkdigital.productivemetalworks.ProductiveMetalworks;
import cy.jdkdigital.productivemetalworks.client.render.block.CastingBlockEntityRenderer;
import cy.jdkdigital.productivemetalworks.client.render.block.FoundryControllerBlockEntityRenderer;
import cy.jdkdigital.productivemetalworks.client.render.block.FoundryTankBlockEntityRenderer;
import cy.jdkdigital.productivemetalworks.client.render.block.FoundryTapBlockEntityRenderer;
import cy.jdkdigital.productivemetalworks.client.screen.FoundryControllerScreen;
import cy.jdkdigital.productivemetalworks.registry.MetalworksRegistrator;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.model.DynamicFluidContainerModel;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

@EventBusSubscriber(modid = ProductiveMetalworks.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEventHandler
{
    @SubscribeEvent
    public static void registerMenus(final RegisterMenuScreensEvent event) {
        event.register(MetalworksRegistrator.FOUNDRY_CONTROLLER_CONTAINER.get(), FoundryControllerScreen::new);
    }

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(MetalworksRegistrator.FOUNDRY_CONTROLLER_BLOCK_ENTITY.get(), FoundryControllerBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(MetalworksRegistrator.FOUNDRY_TANK_BLOCK_ENTITY.get(), FoundryTankBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(MetalworksRegistrator.FOUNDRY_TAP_BLOCK_ENTITY.get(), FoundryTapBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(MetalworksRegistrator.CASTING_BLOCK_ENTITY.get(), CastingBlockEntityRenderer::new);
    }

    @SubscribeEvent
    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        ProductiveMetalworks.FLUID_TYPES.getEntries().forEach(fluidHolder -> {
            event.registerFluidType(new IClientFluidTypeExtensions()
            {
                @Override
                public @NotNull ResourceLocation getStillTexture() {
                    return ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "block/fluid/molten_metal");
                }

                @Override
                public @NotNull ResourceLocation getFlowingTexture() {
                    return ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "block/fluid/molten_metal_flow");
                }

                @Override
                public ResourceLocation getOverlayTexture() {
                    return ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "block/fluid/molten_metal");
                }

                @Override
                public int getTintColor() {
                    return MetalworksRegistrator.FLUID_COLORS.get(fluidHolder.getId().getPath());
                }

                @Override
                public @NotNull Vector3f modifyFogColor(@NotNull Camera camera, float partialTick, @NotNull ClientLevel level, int renderDistance, float darkenWorldAmount, @NotNull Vector3f fluidFogColor) {
                    var fluidColor = MetalworksRegistrator.FLUID_COLORS.get(fluidHolder.getId().getPath());;
                    return new Vector3f(fluidColor >> 16 & 255, fluidColor >> 8 & 255, fluidColor & 255).div(255.0F).mul(0.2f);
                }
            }, fluidHolder.get());
        });
    }

    @SubscribeEvent
    public static void registerItemColorHandlers(RegisterColorHandlersEvent.Item event) {
        ProductiveMetalworks.ITEMS.getEntries().forEach(itemHolder -> {
            if (itemHolder.getId().getPath().contains("_bucket")) {
                event.register(new DynamicFluidContainerModel.Colors(), itemHolder.get());
            }
        });
    }
}
