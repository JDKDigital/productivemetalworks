package cy.jdkdigital.productivemetalworks.event;

import cy.jdkdigital.productivemetalworks.ProductiveMetalworks;
import cy.jdkdigital.productivemetalworks.client.render.block.*;
import cy.jdkdigital.productivemetalworks.client.screen.FoundryControllerScreen;
import cy.jdkdigital.productivemetalworks.registry.MetalworksRegistrator;
import net.minecraft.client.Camera;
import net.minecraft.client.color.block.BlockTintSources;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterFluidModelsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector4f;

@EventBusSubscriber(modid = ProductiveMetalworks.MODID, value = Dist.CLIENT)
public class ClientEventHandler
{
    // The shared molten-metal still/flowing/overlay sprites; per-fluid colour is supplied as the fluid model's tint source.
    private static final Material MOLTEN_STILL = new Material(Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, "block/fluid/molten_metal"));
    private static final Material MOLTEN_FLOWING = new Material(Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, "block/fluid/molten_metal_flow"));
    private static final Material MOLTEN_OVERLAY = new Material(Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, "block/fluid/molten_metal"));

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
        event.registerBlockEntityRenderer(MetalworksRegistrator.FOUNDRY_CAPACITOR_BLOCK_ENTITY.get(), FoundryCapacitorBlockEntityRenderer::new);
    }

    // 26.1: still/flowing textures + tint moved off IClientFluidTypeExtensions to the data-driven fluid model.
    // One model per molten fluid (still + flowing variants), each tinted with the metal's colour. This also drives
    // the bucket tint (the neoforge:fluid_container item model reads the fluid model's tint source).
    @SubscribeEvent
    public static void registerFluidModels(RegisterFluidModelsEvent event) {
        ProductiveMetalworks.FLUIDS.getEntries().forEach(fluidHolder -> {
            String path = fluidHolder.getId().getPath();
            String metal = path.startsWith("flowing_") ? path.substring("flowing_".length()) : path;
            Integer color = MetalworksRegistrator.FLUID_COLORS.get(metal);
            if (color != null) {
                event.register(new FluidModel.Unbaked(MOLTEN_STILL, MOLTEN_FLOWING, MOLTEN_OVERLAY, BlockTintSources.constant(0xFF000000 | color)), fluidHolder.get());
            }
        });
    }

    // Only fog colour remains on IClientFluidTypeExtensions in 26.1 (textures/tint are data-driven now).
    @SubscribeEvent
    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        ProductiveMetalworks.FLUID_TYPES.getEntries().forEach(fluidHolder -> {
            event.registerFluidType(new IClientFluidTypeExtensions()
            {
                @Override
                public void modifyFogColor(@NotNull Camera camera, float partialTick, @NotNull ClientLevel level, int renderDistance, float darkenWorldAmount, @NotNull Vector4f fluidFogColor) {
                    var fluidColor = MetalworksRegistrator.FLUID_COLORS.get(fluidHolder.getId().getPath());
                    if (fluidColor != null) {
                        fluidFogColor.x = (fluidColor >> 16 & 255) / 255.0F * 0.2f;
                        fluidFogColor.y = (fluidColor >> 8 & 255) / 255.0F * 0.2f;
                        fluidFogColor.z = (fluidColor & 255) / 255.0F * 0.2f;
                    }
                }
            }, fluidHolder.get());
        });
    }
}
