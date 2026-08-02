package cy.jdkdigital.productivemetalworks.registry;

import cy.jdkdigital.productivemetalworks.ProductiveMetalworks;
import net.minecraft.resources.Identifier;
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
        public static final TagKey<Block> FOUNDRY_CONTROLLERS = BlockTags.create(Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, "foundry_controllers"));
        public static final TagKey<Block> FOUNDRY_DRAINS = BlockTags.create(Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, "foundry_drains"));
        public static final TagKey<Block> FOUNDRY_TANKS = BlockTags.create(Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, "foundry_tanks"));
        public static final TagKey<Block> FOUNDRY_CAPACITORS = BlockTags.create(Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, "foundry_capacitors"));
        public static final TagKey<Block> FOUNDRY_WINDOWS = BlockTags.create(Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, "foundry_windows"));
        public static final TagKey<Block> FIRE_BRICKS = BlockTags.create(Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, "fire_bricks"));
        public static final TagKey<Block> FOUNDRY_WALL_BLOCKS = BlockTags.create(Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, "foundry_wall_blocks"));
        public static final TagKey<Block> HEATING_COILS = BlockTags.create(Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, "heating_coils"));
        public static final TagKey<Block> FOUNDRY_BOTTOM_BLOCKS = BlockTags.create(Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, "foundry_bottom_blocks"));
    }

    public static class Items
    {
        public static final TagKey<Item> FOUNDRY_CONTROLLERS = ItemTags.create(Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, "foundry_controllers"));
        public static final TagKey<Item> FOUNDRY_DRAINS = ItemTags.create(Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, "foundry_drains"));
        public static final TagKey<Item> FOUNDRY_TANKS = ItemTags.create(Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, "foundry_tanks"));
        public static final TagKey<Item> FOUNDRY_CAPACITORS = ItemTags.create(Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, "foundry_capacitors"));
        public static final TagKey<Item> FOUNDRY_WINDOWS = ItemTags.create(Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, "foundry_windows"));
        public static final TagKey<Item> FIRE_BRICKS = ItemTags.create(Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, "fire_bricks"));
        public static final TagKey<Item> CASTS = ItemTags.create(Identifier.fromNamespaceAndPath("c", "casts"));
        public static final TagKey<Item> GEARS = ItemTags.create(Identifier.fromNamespaceAndPath("c", "gears"));
        public static final TagKey<Item> RODS = ItemTags.create(Identifier.fromNamespaceAndPath("c", "rods"));
        public static final TagKey<Item> PLATES = ItemTags.create(Identifier.fromNamespaceAndPath("c", "plates"));
        public static final TagKey<Item> WAXES = ItemTags.create(Identifier.fromNamespaceAndPath("c", "waxes"));
        public static final TagKey<Item> STORAGE_BLOCK_WAXES = ItemTags.create(Identifier.fromNamespaceAndPath("c", "storage_blocks/wax"));
        public static final TagKey<Item> MEAT_NUGGETS = ItemTags.create(Identifier.fromNamespaceAndPath("c", "nuggets/meat"));
        public static final TagKey<Item> MEAT_INGOTS = ItemTags.create(Identifier.fromNamespaceAndPath("c", "ingots/meat"));
        public static final TagKey<Item> STORAGE_BLOCKS_CHARCOAL = ItemTags.create(Identifier.fromNamespaceAndPath("c", "storage_blocks/charcoal"));

        public static final TagKey<Item> MELTABLE = ItemTags.create(Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, "meltable"));
        public static final TagKey<Item> MELTABLE_METALS = ItemTags.create(Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, "meltable/metals"));
        public static final TagKey<Item> MELTABLE_GEMS = ItemTags.create(Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, "meltable/gems"));
        public static final TagKey<Item> MELTABLE_ORGANIC = ItemTags.create(Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, "meltable/organic"));
    }

    // Molten metals and stuff

    public static class Fluids
    {
        public static final TagKey<Fluid> MEAT = FluidTags.create(Identifier.fromNamespaceAndPath("c", "meat"));
        public static final TagKey<Fluid> HONEY = FluidTags.create(Identifier.fromNamespaceAndPath("c", "honey"));
        public static final TagKey<Fluid> MOLTEN_WAX = FluidTags.create(Identifier.fromNamespaceAndPath("c", "wax"));

        public static final TagKey<Fluid> MOLTEN_HEAVY_CORE = FluidTags.create(Identifier.fromNamespaceAndPath("c", "molten_heavy_core"));
        public static final TagKey<Fluid> MOLTEN_AMETHYST = FluidTags.create(Identifier.fromNamespaceAndPath("c", "molten_amethyst"));
        public static final TagKey<Fluid> MOLTEN_GLOWSTONE = FluidTags.create(Identifier.fromNamespaceAndPath("c", "molten_glowstone"));
        public static final TagKey<Fluid> MOLTEN_REDSTONE = FluidTags.create(Identifier.fromNamespaceAndPath("c", "molten_redstone"));
        public static final TagKey<Fluid> MOLTEN_OBSIDIAN = FluidTags.create(Identifier.fromNamespaceAndPath("c", "molten_obsidian"));
        public static final TagKey<Fluid> MOLTEN_GLASS = FluidTags.create(Identifier.fromNamespaceAndPath("c", "molten_glass"));
        public static final TagKey<Fluid> MOLTEN_EMERALD = FluidTags.create(Identifier.fromNamespaceAndPath("c", "molten_emerald"));
        public static final TagKey<Fluid> MOLTEN_DIAMOND = FluidTags.create(Identifier.fromNamespaceAndPath("c", "molten_diamond"));
        public static final TagKey<Fluid> MOLTEN_LAPIS = FluidTags.create(Identifier.fromNamespaceAndPath("c", "molten_lapis"));
        public static final TagKey<Fluid> MOLTEN_QUARTZ = FluidTags.create(Identifier.fromNamespaceAndPath("c", "molten_quartz"));
        public static final TagKey<Fluid> MOLTEN_CARBON = FluidTags.create(Identifier.fromNamespaceAndPath("c", "molten_carbon"));
        public static final TagKey<Fluid> MOLTEN_ENDER = FluidTags.create(Identifier.fromNamespaceAndPath("c", "molten_ender"));
        public static final TagKey<Fluid> MOLTEN_ANCIENT_DEBRIS = FluidTags.create(Identifier.fromNamespaceAndPath("c", "molten_ancient_debris"));
        public static final TagKey<Fluid> MOLTEN_SHULKER_SHELL = FluidTags.create(Identifier.fromNamespaceAndPath("c", "molten_shulker_shell"));
        public static final TagKey<Fluid> MOLTEN_BLAZE = FluidTags.create(Identifier.fromNamespaceAndPath("c", "molten_blaze"));
        public static final TagKey<Fluid> MOLTEN_SLIME = FluidTags.create(Identifier.fromNamespaceAndPath("c", "molten_slime"));
        public static final TagKey<Fluid> MOLTEN_MAGMA_CREAM = FluidTags.create(Identifier.fromNamespaceAndPath("c", "molten_magma_cream"));

        public static final TagKey<Fluid> MOLTEN_IRON = FluidTags.create(Identifier.fromNamespaceAndPath("c", "molten_iron"));
        public static final TagKey<Fluid> MOLTEN_COPPER = FluidTags.create(Identifier.fromNamespaceAndPath("c", "molten_copper"));
        public static final TagKey<Fluid> MOLTEN_GOLD = FluidTags.create(Identifier.fromNamespaceAndPath("c", "molten_gold"));
        public static final TagKey<Fluid> MOLTEN_NETHERITE = FluidTags.create(Identifier.fromNamespaceAndPath("c", "molten_netherite"));
        public static final TagKey<Fluid> MOLTEN_ALUMINUM = FluidTags.create(Identifier.fromNamespaceAndPath("c", "molten_aluminum"));
        public static final TagKey<Fluid> MOLTEN_SILVER = FluidTags.create(Identifier.fromNamespaceAndPath("c", "molten_silver"));
        public static final TagKey<Fluid> MOLTEN_TIN = FluidTags.create(Identifier.fromNamespaceAndPath("c", "molten_tin"));
        public static final TagKey<Fluid> MOLTEN_ZINC = FluidTags.create(Identifier.fromNamespaceAndPath("c", "molten_zinc"));
        public static final TagKey<Fluid> MOLTEN_NICKEL = FluidTags.create(Identifier.fromNamespaceAndPath("c", "molten_nickel"));
        public static final TagKey<Fluid> MOLTEN_OSMIUM = FluidTags.create(Identifier.fromNamespaceAndPath("c", "molten_osmium"));
        public static final TagKey<Fluid> MOLTEN_LEAD = FluidTags.create(Identifier.fromNamespaceAndPath("c", "molten_lead"));
        public static final TagKey<Fluid> MOLTEN_PLATINUM = FluidTags.create(Identifier.fromNamespaceAndPath("c", "molten_platinum"));
        public static final TagKey<Fluid> MOLTEN_URANIUM = FluidTags.create(Identifier.fromNamespaceAndPath("c", "molten_uranium"));
        public static final TagKey<Fluid> MOLTEN_IRIDIUM = FluidTags.create(Identifier.fromNamespaceAndPath("c", "molten_iridium"));
        public static final TagKey<Fluid> MOLTEN_STEEL = FluidTags.create(Identifier.fromNamespaceAndPath("c", "molten_steel"));
        public static final TagKey<Fluid> MOLTEN_INVAR = FluidTags.create(Identifier.fromNamespaceAndPath("c", "molten_invar"));
        public static final TagKey<Fluid> MOLTEN_ELECTRUM = FluidTags.create(Identifier.fromNamespaceAndPath("c", "molten_electrum"));
        public static final TagKey<Fluid> MOLTEN_BRONZE = FluidTags.create(Identifier.fromNamespaceAndPath("c", "molten_bronze"));
        public static final TagKey<Fluid> MOLTEN_BRASS = FluidTags.create(Identifier.fromNamespaceAndPath("c", "molten_brass"));
        public static final TagKey<Fluid> MOLTEN_ENDERIUM = FluidTags.create(Identifier.fromNamespaceAndPath("c", "molten_enderium"));
        public static final TagKey<Fluid> MOLTEN_LUMIUM = FluidTags.create(Identifier.fromNamespaceAndPath("c", "molten_lumium"));
        public static final TagKey<Fluid> MOLTEN_SIGNALUM = FluidTags.create(Identifier.fromNamespaceAndPath("c", "molten_signalum"));
        public static final TagKey<Fluid> MOLTEN_CONSTANTAN = FluidTags.create(Identifier.fromNamespaceAndPath("c", "molten_constantan"));
        public static final TagKey<Fluid> MOLTEN_REFINED_GLOWSTONE = FluidTags.create(Identifier.fromNamespaceAndPath("c", "molten_refined_glowstone"));
        public static final TagKey<Fluid> MOLTEN_REFINED_OBSIDIAN = FluidTags.create(Identifier.fromNamespaceAndPath("c", "molten_refined_obsidian"));
    }
}
