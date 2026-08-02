package cy.jdkdigital.productivemetalworks.integration.ponder;

import cy.jdkdigital.productivemetalworks.ProductiveMetalworks;
import cy.jdkdigital.productivemetalworks.registry.MetalworksRegistrator;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.ItemLike;

public class MetalworksPonderTags {

    public static final Identifier FOUNDRY_CONTROLLER_BLOCKS = Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, "foundry_controller_blocks");
    public static final Identifier FOUNDRY_BUILDING_BLOCKS = Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, "foundry_building_blocks");
    public static final Identifier FOUNDRY_TANK_BLOCKS = Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, "foundry_tank_blocks");
    public static final Identifier FOUNDRY_CASTING_BLOCKS = Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, "foundry_casting_blocks");

    public static void register(PonderTagRegistrationHelper<Identifier> helper) {
        PonderTagRegistrationHelper<ItemLike> HELPER = helper.withKeyFunction(item -> BuiltInRegistries.ITEM.getKey(item.asItem()));

        helper.registerTag(FOUNDRY_CONTROLLER_BLOCKS)
                .addToIndex()
                .item(MetalworksRegistrator.FOUNDRY_CONTROLLERS.get(DyeColor.BLACK).get().asItem(), true, false)
                .title("Foundry Controllers")
                .description("Controller blocks of the foundry")
                .register();

        helper.registerTag(FOUNDRY_BUILDING_BLOCKS)
                .addToIndex()
                .item(MetalworksRegistrator.FIRE_BRICKS.get(DyeColor.BLACK).get().asItem(), true, false)
                .title("Foundry Building Blocks")
                .description("Base Blocks used to build the foundry")
                .register();

        helper.registerTag(FOUNDRY_TANK_BLOCKS)
                .addToIndex()
                .item(MetalworksRegistrator.FOUNDRY_CONTROLLERS.get(DyeColor.BLACK).get().asItem(), true, false)
                .title("Foundry Tank Blocks")
                .description("Blocks used to store fuel for the foundry")
                .register();

        helper.registerTag(FOUNDRY_CASTING_BLOCKS)
                .addToIndex()
                .item(MetalworksRegistrator.FOUNDRY_CONTROLLERS.get(DyeColor.BLACK).get().asItem(), true, false)
                .title("Foundry Casting Blocks")
                .description("Blocks used in the casting process")
                .register();


        MetalworksRegistrator.FOUNDRY_CONTROLLERS.forEach((dye, holder) -> HELPER.addToTag(FOUNDRY_CONTROLLER_BLOCKS).add(holder.get().asItem()));

        MetalworksRegistrator.FIRE_BRICKS.forEach((dye, holder) -> HELPER.addToTag(FOUNDRY_BUILDING_BLOCKS).add(holder.get().asItem()));
        MetalworksRegistrator.FOUNDRY_WINDOWS.forEach((dye, holder) -> HELPER.addToTag(FOUNDRY_BUILDING_BLOCKS).add(holder.get().asItem()));

        MetalworksRegistrator.FOUNDRY_DRAINS.forEach((dye, holder) -> HELPER.addToTag(FOUNDRY_CASTING_BLOCKS).add(holder.get().asItem()));
        HELPER.addToTag(FOUNDRY_CASTING_BLOCKS).add(MetalworksRegistrator.FOUNDRY_TAP.get().asItem());
        HELPER.addToTag(FOUNDRY_CASTING_BLOCKS).add(MetalworksRegistrator.CASTING_BASIN.get().asItem());
        HELPER.addToTag(FOUNDRY_CASTING_BLOCKS).add(MetalworksRegistrator.CASTING_TABLE.get().asItem());

        MetalworksRegistrator.FOUNDRY_TANKS.forEach((dye, holder) -> HELPER.addToTag(FOUNDRY_TANK_BLOCKS).add(holder.get().asItem()));
        MetalworksRegistrator.FOUNDRY_CAPACITORS.forEach((dye, holder) -> HELPER.addToTag(FOUNDRY_TANK_BLOCKS).add(holder.get().asItem()));
    }
}
