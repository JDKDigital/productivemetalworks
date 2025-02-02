package cy.jdkdigital.productivemetalworks.registry;

import cy.jdkdigital.productivemetalworks.ProductiveMetalworks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

public class ModTags
{

    public static class Blocks {
        public static final TagKey<Block> FOUNDRY_CONTROLLERS = BlockTags.create(ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "foundry_controllers"));
        public static final TagKey<Block> FOUNDRY_DRAINS = BlockTags.create(ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "foundry_drains"));
        public static final TagKey<Block> FOUNDRY_TANKS = BlockTags.create(ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "foundry_tanks"));
        public static final TagKey<Block> FOUNDRY_WINDOWS = BlockTags.create(ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "foundry_windows"));
        public static final TagKey<Block> FIRE_BRICKS = BlockTags.create(ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "fire_bricks"));
        public static final TagKey<Block> FOUNDRY_WALL_BLOCKS = BlockTags.create(ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "foundry_wall_blocks"));
        public static final TagKey<Block> HEATING_COILS = BlockTags.create(ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "heating_coils"));
        public static final TagKey<Block> FOUNDRY_BOTTOM_BLOCKS = BlockTags.create(ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "foundry_bottom_blocks"));
    }

    public static class Items
    {
        public static final TagKey<Item> FOUNDRY_CONTROLLERS = ItemTags.create(ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "foundry_controllers"));
        public static final TagKey<Item> FOUNDRY_DRAINS = ItemTags.create(ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "foundry_drains"));
        public static final TagKey<Item> FOUNDRY_TANKS = ItemTags.create(ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "foundry_tanks"));
        public static final TagKey<Item> FOUNDRY_WINDOWS = ItemTags.create(ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "foundry_windows"));
        public static final TagKey<Item> FIRE_BRICKS = ItemTags.create(ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "fire_bricks"));
        public static final TagKey<Item> CASTS = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "casts"));
        public static final TagKey<Item> GEARS = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "gears"));
        public static final TagKey<Item> RODS = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "rods"));
        public static final TagKey<Item> PLATES = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "plates"));
        public static final TagKey<Item> WAXES = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "waxes"));
        public static final TagKey<Item> STORAGE_BLOCK_WAXES = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "storage_blocks/wax"));
        public static final TagKey<Item> SG_BLUEPRINTS = ItemTags.create(ResourceLocation.fromNamespaceAndPath("silentgear", "blueprints"));
    }

    // Molten metals and stuff

    public static class Fluids
    {
        public static final TagKey<Fluid> MEAT = FluidTags.create(ResourceLocation.fromNamespaceAndPath("c", "meat"));
        public static final TagKey<Fluid> HONEY = FluidTags.create(ResourceLocation.fromNamespaceAndPath("c", "honey"));
        public static final TagKey<Fluid> MOLTEN_WAX = FluidTags.create(ResourceLocation.fromNamespaceAndPath("c", "wax"));

        public static final TagKey<Fluid> MOLTEN_AMETHYST = FluidTags.create(ResourceLocation.fromNamespaceAndPath("c", "molten_amethyst"));
        public static final TagKey<Fluid> MOLTEN_GLOWSTONE = FluidTags.create(ResourceLocation.fromNamespaceAndPath("c", "molten_glowstone"));
        public static final TagKey<Fluid> MOLTEN_REDSTONE = FluidTags.create(ResourceLocation.fromNamespaceAndPath("c", "molten_redstone"));
        public static final TagKey<Fluid> MOLTEN_OBSIDIAN = FluidTags.create(ResourceLocation.fromNamespaceAndPath("c", "molten_obsidian"));
        public static final TagKey<Fluid> MOLTEN_GLASS = FluidTags.create(ResourceLocation.fromNamespaceAndPath("c", "molten_glass"));
        public static final TagKey<Fluid> MOLTEN_EMERALD = FluidTags.create(ResourceLocation.fromNamespaceAndPath("c", "dmolten_emerald"));
        public static final TagKey<Fluid> MOLTEN_DIAMOND = FluidTags.create(ResourceLocation.fromNamespaceAndPath("c", "molten_diamond"));
        public static final TagKey<Fluid> MOLTEN_LAPIS = FluidTags.create(ResourceLocation.fromNamespaceAndPath("c", "molten_lapis"));
        public static final TagKey<Fluid> MOLTEN_QUARTZ = FluidTags.create(ResourceLocation.fromNamespaceAndPath("c", "molten_quartz"));
        public static final TagKey<Fluid> MOLTEN_CARBON = FluidTags.create(ResourceLocation.fromNamespaceAndPath("c", "molten_carbon"));
        public static final TagKey<Fluid> MOLTEN_ENDER = FluidTags.create(ResourceLocation.fromNamespaceAndPath("c", "molten_ender"));
        public static final TagKey<Fluid> MOLTEN_ANCIENT_DEBRIS = FluidTags.create(ResourceLocation.fromNamespaceAndPath("c", "molten_ancient_debris"));

        public static final TagKey<Fluid> MOLTEN_IRON = FluidTags.create(ResourceLocation.fromNamespaceAndPath("c", "molten_iron"));
        public static final TagKey<Fluid> MOLTEN_COPPER = FluidTags.create(ResourceLocation.fromNamespaceAndPath("c", "molten_copper"));
        public static final TagKey<Fluid> MOLTEN_GOLD = FluidTags.create(ResourceLocation.fromNamespaceAndPath("c", "molten_gold"));
        public static final TagKey<Fluid> MOLTEN_NETHERITE = FluidTags.create(ResourceLocation.fromNamespaceAndPath("c", "molten_netherite"));
        public static final TagKey<Fluid> MOLTEN_ALUMINUM = FluidTags.create(ResourceLocation.fromNamespaceAndPath("c", "molten_aluminum"));
        public static final TagKey<Fluid> MOLTEN_SILVER = FluidTags.create(ResourceLocation.fromNamespaceAndPath("c", "molten_silver"));
        public static final TagKey<Fluid> MOLTEN_TIN = FluidTags.create(ResourceLocation.fromNamespaceAndPath("c", "molten_tin"));
        public static final TagKey<Fluid> MOLTEN_ZINC = FluidTags.create(ResourceLocation.fromNamespaceAndPath("c", "molten_zinc"));
        public static final TagKey<Fluid> MOLTEN_NICKEL = FluidTags.create(ResourceLocation.fromNamespaceAndPath("c", "molten_nickel"));
        public static final TagKey<Fluid> MOLTEN_OSMIUM = FluidTags.create(ResourceLocation.fromNamespaceAndPath("c", "molten_osmium"));
        public static final TagKey<Fluid> MOLTEN_LEAD = FluidTags.create(ResourceLocation.fromNamespaceAndPath("c", "molten_lead"));
        public static final TagKey<Fluid> MOLTEN_PLATINUM = FluidTags.create(ResourceLocation.fromNamespaceAndPath("c", "molten_platinum"));
        public static final TagKey<Fluid> MOLTEN_URANIUM = FluidTags.create(ResourceLocation.fromNamespaceAndPath("c", "molten_uranium"));
        public static final TagKey<Fluid> MOLTEN_IRIDIUM = FluidTags.create(ResourceLocation.fromNamespaceAndPath("c", "molten_iridium"));
        public static final TagKey<Fluid> MOLTEN_STEEL = FluidTags.create(ResourceLocation.fromNamespaceAndPath("c", "molten_steel"));
        public static final TagKey<Fluid> MOLTEN_INVAR = FluidTags.create(ResourceLocation.fromNamespaceAndPath("c", "molten_invar"));
        public static final TagKey<Fluid> MOLTEN_ELECTRUM = FluidTags.create(ResourceLocation.fromNamespaceAndPath("c", "molten_electrum"));
        public static final TagKey<Fluid> MOLTEN_BRONZE = FluidTags.create(ResourceLocation.fromNamespaceAndPath("c", "molten_bronze"));
        public static final TagKey<Fluid> MOLTEN_BRASS = FluidTags.create(ResourceLocation.fromNamespaceAndPath("c", "molten_brass"));
        public static final TagKey<Fluid> MOLTEN_ENDERIUM = FluidTags.create(ResourceLocation.fromNamespaceAndPath("c", "molten_enderium"));
        public static final TagKey<Fluid> MOLTEN_LUMIUM = FluidTags.create(ResourceLocation.fromNamespaceAndPath("c", "molten_lumium"));
        public static final TagKey<Fluid> MOLTEN_SIGNALUM = FluidTags.create(ResourceLocation.fromNamespaceAndPath("c", "molten_signalum"));
        public static final TagKey<Fluid> MOLTEN_CONSTANTAN = FluidTags.create(ResourceLocation.fromNamespaceAndPath("c", "molten_constantan"));
        public static final TagKey<Fluid> MOLTEN_REFINED_GLOWSTONE = FluidTags.create(ResourceLocation.fromNamespaceAndPath("c", "molten_refined_glowstone"));
//    public static final TagKey<Fluid> MOLTEN_REFINED_OBSIDIAN = FluidTags.create(ResourceLocation.fromNamespaceAndPath("c", "molten_refined_obsidian"));
    }
}
