package cy.jdkdigital.productivemetalworks.integration.ponder;

import net.neoforged.neoforge.registries.DeferredHolder;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;

import cy.jdkdigital.productivemetalworks.integration.ponder.scenes.FoundryScenes;
import cy.jdkdigital.productivemetalworks.registry.MetalworksRegistrator;

import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public class MetalworksPonderScenes {

    public static void register(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        PonderSceneRegistrationHelper<DeferredHolder<Block, Block>> HELPER = helper.withKeyFunction(DeferredHolder::getId);

        MetalworksRegistrator.FOUNDRY_CONTROLLERS.forEach((key, value) -> {
            addBuilding(HELPER, value);
            addFueling(HELPER, value);
            addSmelting(HELPER, value);
            addCasting(HELPER, value);
        });

        // Building
        MetalworksRegistrator.FIRE_BRICKS.forEach((key, value) -> {
            addBuilding(HELPER, value);
        });

        MetalworksRegistrator.FOUNDRY_WINDOWS.forEach((key, value) -> {
            addBuilding(HELPER, value);
        });

        // Fueling
        MetalworksRegistrator.FOUNDRY_TANKS.forEach((key, value) -> {
            addFueling(HELPER, value);
        });

        MetalworksRegistrator.FOUNDRY_CAPACITORS.forEach((key, value) -> {
            addFueling(HELPER, value);
        });

        // Casting
        MetalworksRegistrator.FOUNDRY_DRAINS.forEach((key, value) -> {
            addCasting(HELPER, value);
        });

        addCasting(HELPER, MetalworksRegistrator.FOUNDRY_TAP);
        addCasting(HELPER, MetalworksRegistrator.CASTING_BASIN);
        addCasting(HELPER, MetalworksRegistrator.CASTING_TABLE);
    }

    private static void addBuilding(PonderSceneRegistrationHelper<DeferredHolder<Block, Block>> HELPER, DeferredHolder<Block, Block> value) {
        HELPER.forComponents(value).addStoryBoard(
                "foundry_base",
                FoundryScenes::building,
                MetalworksPonderTags.FOUNDRY_CONTROLLER_BLOCKS,
                MetalworksPonderTags.FOUNDRY_BUILDING_BLOCKS
        );
    }

    private static void addFueling(PonderSceneRegistrationHelper<DeferredHolder<Block, Block>> HELPER, DeferredHolder<Block, Block> value) {
        HELPER.forComponents(value).addStoryBoard(
                "foundry_built",
                FoundryScenes::fueling,
                MetalworksPonderTags.FOUNDRY_CONTROLLER_BLOCKS,
                MetalworksPonderTags.FOUNDRY_TANK_BLOCKS
        );
    }

    private static void addSmelting(PonderSceneRegistrationHelper<DeferredHolder<Block, Block>> HELPER, DeferredHolder<Block, Block> value) {
        HELPER.forComponents(value).addStoryBoard(
                "foundry_built",
                FoundryScenes::smelting,
                MetalworksPonderTags.FOUNDRY_CONTROLLER_BLOCKS
        );
    }

    private static void addCasting(PonderSceneRegistrationHelper<DeferredHolder<Block, Block>> HELPER, DeferredHolder<Block, Block> value) {
        HELPER.forComponents(value).addStoryBoard(
                "foundry_molten",
                FoundryScenes::casting,
                MetalworksPonderTags.FOUNDRY_CONTROLLER_BLOCKS,
                MetalworksPonderTags.FOUNDRY_CASTING_BLOCKS
        );
    }
}
