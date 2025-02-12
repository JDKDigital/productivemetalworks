package cy.jdkdigital.productivemetalworks.event;

import cy.jdkdigital.productivemetalworks.ProductiveMetalworks;
import cy.jdkdigital.productivemetalworks.common.block.entity.FoundryControllerBlockEntity;
import cy.jdkdigital.productivemetalworks.network.MoveFoundryFluidData;
import cy.jdkdigital.productivemetalworks.registry.MetalworksRegistrator;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;

@EventBusSubscriber(modid = ProductiveMetalworks.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ModEventHandler
{
    @SubscribeEvent
    public static void registerBlockEntityCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                MetalworksRegistrator.FOUNDRY_CONTROLLER_BLOCK_ENTITY.get(),
                (myBlockEntity, side) -> myBlockEntity.getItemHandler()
        );
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                MetalworksRegistrator.FOUNDRY_DRAIN_BLOCK_ENTITY.get(),
                (myBlockEntity, side) -> {
                    if (myBlockEntity.getLevel() instanceof Level level && myBlockEntity.getMultiblockController() != null) {
                        if (level.getBlockEntity(myBlockEntity.getMultiblockController()) instanceof FoundryControllerBlockEntity foundryController) {
                            return foundryController.getFluidHandler();
                        }
                    }
                    return null;
                }
        );
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                MetalworksRegistrator.FOUNDRY_TANK_BLOCK_ENTITY.get(),
                (myBlockEntity, side) -> myBlockEntity.getFluidHandler()
        );
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                MetalworksRegistrator.CASTING_BLOCK_ENTITY.get(),
                (myBlockEntity, side) -> myBlockEntity.isCooling() ? null : myBlockEntity.getFluidHandler()
        );
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                MetalworksRegistrator.CASTING_BLOCK_ENTITY.get(),
                (myBlockEntity, side) -> myBlockEntity.getItemHandler()
        );
    }

    @SubscribeEvent
    public static void buildContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(MetalworksRegistrator.TAB_KEY)) {
            for (DeferredHolder<Item, ? extends Item> item : ProductiveMetalworks.ITEMS.getEntries()) {
                if (!item.is(MetalworksRegistrator.POWERED_HEATING_COIL.getId())) {
                    event.accept(item.value());
                }
            }
        }
    }

    @SubscribeEvent
    private static void registerDataMapTypes(RegisterDataMapTypesEvent event) {
        event.register(MetalworksRegistrator.FUEL_MAP);
        event.register(MetalworksRegistrator.ENTITY_MELTING_MAP);
    }

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        // Sets the current network version
        final PayloadRegistrar registrar = event.registrar(ProductiveMetalworks.MODID).versioned("1");
        registrar.playToServer(
            MoveFoundryFluidData.TYPE,
            MoveFoundryFluidData.STREAM_CODEC,
            new DirectionalPayloadHandler<>(
                MoveFoundryFluidData::clientHandle,
                MoveFoundryFluidData::serverHandle
            )
        );
    }
}
