package cy.jdkdigital.productivemetalworks.registry;

import cy.jdkdigital.productivelib.util.ImmutableFluidStack;
import cy.jdkdigital.productivemetalworks.ProductiveMetalworks;
import cy.jdkdigital.productivemetalworks.common.block.*;
import cy.jdkdigital.productivemetalworks.common.block.entity.*;
import cy.jdkdigital.productivemetalworks.common.datamap.EntityMeltingMap;
import cy.jdkdigital.productivemetalworks.common.datamap.FuelMap;
import cy.jdkdigital.productivemetalworks.common.datamap.UnitMap;
import cy.jdkdigital.productivemetalworks.common.menu.FoundryControllerContainer;
import cy.jdkdigital.productivemetalworks.recipe.*;
import cy.jdkdigital.productivemetalworks.util.CoilType;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.food.Foods;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.datamaps.DataMapType;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class MetalworksRegistrator
{
    public static void register() {}

    public static final DataMapType<Fluid, FuelMap> FUEL_MAP = DataMapType.builder(Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, "fuel_map"), Registries.FLUID, FuelMap.CODEC).synced(FuelMap.CODEC, false).build();
    public static final DataMapType<Block, FuelMap> POWER_COIL_MAP = DataMapType.builder(Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, "power_coil_map"), Registries.BLOCK, FuelMap.CODEC).synced(FuelMap.CODEC, false).build();
    public static final DataMapType<EntityType<?>, EntityMeltingMap> ENTITY_MELTING_MAP = DataMapType.builder(Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, "entity_melting"), Registries.ENTITY_TYPE, EntityMeltingMap.CODEC).synced(EntityMeltingMap.CODEC, false).build();
    public static final DataMapType<Fluid, UnitMap> UNIT_MAP = DataMapType.builder(Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, "unit_map"), Registries.FLUID, UnitMap.CODEC).synced(UnitMap.CODEC, false).build();
    public static final Supplier<DataComponentType<ImmutableFluidStack>> FLUID_STACK = ProductiveMetalworks.DATA_COMPONENTS.register("fluid_stack", () -> DataComponentType.<ImmutableFluidStack>builder().persistent(ImmutableFluidStack.CODEC).networkSynchronized(ImmutableFluidStack.STREAM_CODEC).build());
    public static final ResourceKey<DamageType> FOUNDRY_DAMAGE = ResourceKey.create(Registries.DAMAGE_TYPE, Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, "foundry_damage"));

    public static Map<String, Integer> FLUID_COLORS = new HashMap<>();
    public static FluidType.Properties MOLTEN_FLUID_TYPE_PROPERTIES = FluidType.Properties.create()
            .canSwim(false)
            .canDrown(false)
            .canConvertToSource(false)
            .lightLevel(15)
            .density(3000)
            .viscosity(6000)
            .temperature(1300)
            .motionScale(0.002335)
            .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL_LAVA)
            .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY_LAVA)
            .sound(SoundActions.FLUID_VAPORIZE, SoundEvents.LAVA_EXTINGUISH);

    // Blocks
    public static final Map<DyeColor, DeferredHolder<Block, Block>> FOUNDRY_CONTROLLERS = registerDyedBlocks("foundry_controller", FoundryControllerBlock::new, () -> BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BRICKS).noOcclusion().lightLevel(state -> state.getValue(BlockStateProperties.ATTACHED) ? 8 : 0).sound(SoundType.NETHER_BRICKS));
    public static final Map<DyeColor, DeferredHolder<Block, Block>> FOUNDRY_DRAINS = registerDyedBlocks("foundry_drain", FoundryDrainBlock::new, () -> BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BRICKS).sound(SoundType.NETHER_BRICKS));
    public static final Map<DyeColor, DeferredHolder<Block, Block>> FOUNDRY_TANKS = registerDyedBlocks("foundry_tank", FoundryTankBlock::new, () -> BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BRICKS).noOcclusion().sound(SoundType.NETHER_BRICKS));
    public static final Map<DyeColor, DeferredHolder<Block, Block>> FOUNDRY_CAPACITORS = registerDyedBlocks("foundry_capacitor", FoundryCapacitorBlock::new, () -> BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BRICKS).noOcclusion().sound(SoundType.NETHER_BRICKS));
    public static final Map<DyeColor, DeferredHolder<Block, Block>> FOUNDRY_WINDOWS = registerDyedBlocks("foundry_window", FoundryWindowBlock::new, () -> BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS));
    public static final Map<DyeColor, DeferredHolder<Block, Block>> FIRE_BRICKS = registerDyedBlocks("fire_bricks", FireBricksBlock::new, () -> BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BRICKS).sound(SoundType.NETHER_BRICKS));

    public static final DeferredHolder<Block, Block> FOUNDRY_TAP = registerBlock("foundry_tap", FoundryTapBlock::new, () -> BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BRICKS), true);
    public static final DeferredHolder<Block, Block> CASTING_BASIN = registerBlock("casting_basin", CastingBasinBlock::new, () -> BlockBehaviour.Properties.ofFullCopy(Blocks.CAULDRON), true);
    public static final DeferredHolder<Block, Block> CASTING_TABLE = registerBlock("casting_table", CastingTableBlock::new, () -> BlockBehaviour.Properties.ofFullCopy(Blocks.CAULDRON), true);
    public static final DeferredHolder<Block, Block> LIQUID_HEATING_COIL = registerBlock("liquid_heating_coil", p -> new HeatingCoilBlock(p, CoilType.FLUID), () -> BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).lightLevel(state -> state.getValue(BlockStateProperties.ATTACHED) ? 7 : 0), true);
    public static final DeferredHolder<Block, Block> POWERED_HEATING_COIL = registerBlock("powered_heating_coil", p -> new HeatingCoilBlock(p, CoilType.ENERGY), () -> BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).lightLevel(state -> state.getValue(BlockStateProperties.ATTACHED) ? 7 : 0), true);
    public static final DeferredHolder<Block, Block> HIGH_POWERED_HEATING_COIL = registerBlock("high_powered_heating_coil", p -> new HeatingCoilBlock(p, CoilType.ENERGY), () -> BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).lightLevel(state -> state.getValue(BlockStateProperties.ATTACHED) ? 7 : 0), true);
    public static final DeferredHolder<Block, Block> FIRE_CLAY = registerBlock("fire_clay", Block::new, () -> BlockBehaviour.Properties.ofFullCopy(Blocks.CLAY), true);
    public static final DeferredHolder<Block, Block> MEAT_BLOCK = registerBlock("meat_block", MeatBlock::new, () -> BlockBehaviour.Properties.ofFullCopy(Blocks.DIRT).noOcclusion().sound(SoundType.SLIME_BLOCK), new Item.Properties().craftRemainder(Items.BONE));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FoundryControllerBlockEntity>> FOUNDRY_CONTROLLER_BLOCK_ENTITY = ProductiveMetalworks.BLOCK_ENTITIES.register("foundry_controller", () -> new BlockEntityType<>(FoundryControllerBlockEntity::new, FOUNDRY_CONTROLLERS.values().stream().map(DeferredHolder::get).toList().toArray(new Block[0])));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FoundryDrainBlockEntity>> FOUNDRY_DRAIN_BLOCK_ENTITY = ProductiveMetalworks.BLOCK_ENTITIES.register("foundry_drain", () -> new BlockEntityType<>(FoundryDrainBlockEntity::new, FOUNDRY_DRAINS.values().stream().map(DeferredHolder::get).toList().toArray(new Block[0])));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FoundryTankBlockEntity>> FOUNDRY_TANK_BLOCK_ENTITY = ProductiveMetalworks.BLOCK_ENTITIES.register("foundry_tank", () -> new BlockEntityType<>(FoundryTankBlockEntity::new, FOUNDRY_TANKS.values().stream().map(DeferredHolder::get).toList().toArray(new Block[0])));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FoundryCapacitorBlockEntity>> FOUNDRY_CAPACITOR_BLOCK_ENTITY = ProductiveMetalworks.BLOCK_ENTITIES.register("foundry_capacitor", () -> new BlockEntityType<>(FoundryCapacitorBlockEntity::new, FOUNDRY_CAPACITORS.values().stream().map(DeferredHolder::get).toList().toArray(new Block[0])));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FoundryTapBlockEntity>> FOUNDRY_TAP_BLOCK_ENTITY = ProductiveMetalworks.BLOCK_ENTITIES.register("foundry_tap", () -> new BlockEntityType<>(FoundryTapBlockEntity::new, FOUNDRY_TAP.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CastingBlockEntity>> CASTING_BLOCK_ENTITY = ProductiveMetalworks.BLOCK_ENTITIES.register("casting", () -> new BlockEntityType<>(CastingBlockEntity::new, CASTING_TABLE.get(), CASTING_BASIN.get()));

    // Items
    public static DeferredHolder<Item, Item> FIRE_BRICK = registerItem("fire_brick");
    public static DeferredHolder<Item, Item> MEAT_NUGGET = registerItem("meat_nugget", Item::new, () -> new Item.Properties().food(Foods.DRIED_KELP));
    public static DeferredHolder<Item, Item> MEAT_INGOT = registerItem("meat_ingot", Item::new, () -> new Item.Properties().food(Foods.PUMPKIN_PIE));
    public static DeferredHolder<Item, Item> SHINY_MEAT_INGOT = registerItem("shiny_meat_ingot", Item::new, () -> new Item.Properties().food(Foods.GOLDEN_CARROT));

    // Casts
    public static DeferredHolder<Item, Item> CAST_INGOT = registerItem("ingot_cast");
    public static DeferredHolder<Item, Item> CAST_NUGGET = registerItem("nugget_cast");
    public static DeferredHolder<Item, Item> CAST_GEM = registerItem("gem_cast");
    public static DeferredHolder<Item, Item> CAST_GEAR = registerItem("gear_cast");
    public static DeferredHolder<Item, Item> CAST_ROD = registerItem("rod_cast");
    public static DeferredHolder<Item, Item> CAST_PLATE = registerItem("plate_cast");

    // Fluids
    public static DeferredHolder<Fluid, BaseFlowingFluid.Source> MOLTEN_HEAVY_CORE = registerFluid("molten_heavy_core", 0xff636776);
    public static DeferredHolder<Fluid, BaseFlowingFluid.Source> MOLTEN_AMETHYST = registerFluid("molten_amethyst", 0xffcfa0f3);
    public static DeferredHolder<Fluid, BaseFlowingFluid.Source> MOLTEN_GLOWSTONE = registerFluid("molten_glowstone", 0xfffbda74);
    public static DeferredHolder<Fluid, BaseFlowingFluid.Source> MOLTEN_REDSTONE = registerFluid("molten_redstone", 0xffa41808);
    public static DeferredHolder<Fluid, BaseFlowingFluid.Source> MOLTEN_OBSIDIAN = registerFluid("molten_obsidian", 0xff100c1c);
    public static DeferredHolder<Fluid, BaseFlowingFluid.Source> MOLTEN_GLASS = registerFluid("molten_glass", 0xffd0eae9);
    public static DeferredHolder<Fluid, BaseFlowingFluid.Source> MOLTEN_EMERALD = registerFluid("molten_emerald", 0xff17dd62);
    public static DeferredHolder<Fluid, BaseFlowingFluid.Source> MOLTEN_DIAMOND = registerFluid("molten_diamond", 0xff4bede6);
    public static DeferredHolder<Fluid, BaseFlowingFluid.Source> MOLTEN_LAPIS = registerFluid("molten_lapis", 0xff1c3890);
    public static DeferredHolder<Fluid, BaseFlowingFluid.Source> MOLTEN_QUARTZ = registerFluid("molten_quartz", 0xffeee6de);
    public static DeferredHolder<Fluid, BaseFlowingFluid.Source> MOLTEN_CARBON = registerFluid("molten_carbon", 0xff0c0001);
    public static DeferredHolder<Fluid, BaseFlowingFluid.Source> MOLTEN_ENDER = registerFluid("molten_ender", 0xff105e51);
    public static DeferredHolder<Fluid, BaseFlowingFluid.Source> MOLTEN_ANCIENT_DEBRIS = registerFluid("molten_ancient_debris", 0xff4a2c23);
    public static DeferredHolder<Fluid, BaseFlowingFluid.Source> MOLTEN_SHULKER_SHELL = registerFluid("molten_shulker_shell", 0xff956895);
    public static DeferredHolder<Fluid, BaseFlowingFluid.Source> MOLTEN_BLAZE = registerFluid("molten_blaze", 0xfffcc900);
    public static DeferredHolder<Fluid, BaseFlowingFluid.Source> MOLTEN_SLIME = registerFluid("molten_slime", 0xff568f4e);
    public static DeferredHolder<Fluid, BaseFlowingFluid.Source> MOLTEN_MAGMA_CREAM = registerFluid("molten_magma_cream", 0xffe97823);
    public static DeferredHolder<Fluid, BaseFlowingFluid.Source> MOLTEN_WAX = registerFluid("molten_wax", 0xffffb808);
    public static DeferredHolder<Fluid, BaseFlowingFluid.Source> LIQUID_MEAT = registerFluid("meat", 0xfffd4e67);

    public static DeferredHolder<Fluid, BaseFlowingFluid.Source> MOLTEN_IRON = registerFluid("molten_iron", 0xffc49c6d);
    public static DeferredHolder<Fluid, BaseFlowingFluid.Source> MOLTEN_COPPER = registerFluid("molten_copper", 0xffc66740);
    public static DeferredHolder<Fluid, BaseFlowingFluid.Source> MOLTEN_GOLD = registerFluid("molten_gold", 0xffe2b928);
    public static DeferredHolder<Fluid, BaseFlowingFluid.Source> MOLTEN_NETHERITE = registerFluid("molten_netherite", 0xff262626);

    public static DeferredHolder<Fluid, BaseFlowingFluid.Source> MOLTEN_ALUMINUM = registerFluid("molten_aluminum", 0xffe3e3e3);
    public static DeferredHolder<Fluid, BaseFlowingFluid.Source> MOLTEN_LEAD = registerFluid("molten_lead", 0xff7c8cc6);
    public static DeferredHolder<Fluid, BaseFlowingFluid.Source> MOLTEN_NICKEL = registerFluid("molten_nickel", 0xffa9a984);
    public static DeferredHolder<Fluid, BaseFlowingFluid.Source> MOLTEN_OSMIUM = registerFluid("molten_osmium", 0xffc0c9dd);
    public static DeferredHolder<Fluid, BaseFlowingFluid.Source> MOLTEN_PLATINUM = registerFluid("molten_platinum", 0xffb5b5ff);
    public static DeferredHolder<Fluid, BaseFlowingFluid.Source> MOLTEN_SILVER = registerFluid("molten_silver", 0xffa4e0e7);
    public static DeferredHolder<Fluid, BaseFlowingFluid.Source> MOLTEN_TIN = registerFluid("molten_tin", 0xff787878);
    public static DeferredHolder<Fluid, BaseFlowingFluid.Source> MOLTEN_URANIUM = registerFluid("molten_uranium", 0xff7ee778);
    public static DeferredHolder<Fluid, BaseFlowingFluid.Source> MOLTEN_ZINC = registerFluid("molten_zinc", 0xffb5b5b5);
    public static DeferredHolder<Fluid, BaseFlowingFluid.Source> MOLTEN_IRIDIUM = registerFluid("molten_iridium", 0xffc0c0c0);

    public static DeferredHolder<Fluid, BaseFlowingFluid.Source> MOLTEN_STEEL = registerFluid("molten_steel", 0xff696969);
    public static DeferredHolder<Fluid, BaseFlowingFluid.Source> MOLTEN_INVAR = registerFluid("molten_invar", 0xffcfcfcf);
    public static DeferredHolder<Fluid, BaseFlowingFluid.Source> MOLTEN_ELECTRUM = registerFluid("molten_electrum", 0xffefe5b2);
    public static DeferredHolder<Fluid, BaseFlowingFluid.Source> MOLTEN_BRONZE = registerFluid("molten_bronze", 0xffd98a3d);
    public static DeferredHolder<Fluid, BaseFlowingFluid.Source> MOLTEN_BRASS = registerFluid("molten_brass", 0xfff4ba45);
    public static DeferredHolder<Fluid, BaseFlowingFluid.Source> MOLTEN_ENDERIUM = registerFluid("molten_enderium", 0xff0e6464);
    public static DeferredHolder<Fluid, BaseFlowingFluid.Source> MOLTEN_LUMIUM = registerFluid("molten_lumium", 0xffffda7e);
    public static DeferredHolder<Fluid, BaseFlowingFluid.Source> MOLTEN_SIGNALUM = registerFluid("molten_signalum", 0xffdb7f15);
    public static DeferredHolder<Fluid, BaseFlowingFluid.Source> MOLTEN_CONSTANTAN = registerFluid("molten_constantan", 0xffdab38e);
    public static DeferredHolder<Fluid, BaseFlowingFluid.Source> MOLTEN_REFINED_GLOWSTONE = registerFluid("molten_refined_glowstone", 0xffb1aa56);
    public static DeferredHolder<Fluid, BaseFlowingFluid.Source> MOLTEN_REFINED_OBSIDIAN = registerFluid("molten_refined_obsidian", 0xff654c89);

    // tungsten, monazite

    // Recipes
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<FluidAlloyingRecipe>> FLUID_ALLOYING = ProductiveMetalworks.RECIPE_SERIALIZERS.register("fluid_alloying", () -> FluidAlloyingRecipe.SERIALIZER);
    public static final DeferredHolder<RecipeType<?>, RecipeType<FluidAlloyingRecipe>> FLUID_ALLOYING_TYPE = ProductiveMetalworks.RECIPE_TYPES.register("fluid_alloying", () -> new RecipeType<>() {});
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ItemMeltingRecipe>> ITEM_MELTING = ProductiveMetalworks.RECIPE_SERIALIZERS.register("item_melting", () -> ItemMeltingRecipe.SERIALIZER);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<EntityMeltingRecipe>> ENTITY_MELTING = ProductiveMetalworks.RECIPE_SERIALIZERS.register("entity_melting", () -> EntityMeltingRecipe.SERIALIZER);
    public static final DeferredHolder<RecipeType<?>, RecipeType<ItemMeltingRecipe>> ITEM_MELTING_TYPE = ProductiveMetalworks.RECIPE_TYPES.register("item_melting", () -> new RecipeType<>() {});
    public static final DeferredHolder<RecipeType<?>, RecipeType<EntityMeltingRecipe>> ENTITY_MELTING_TYPE = ProductiveMetalworks.RECIPE_TYPES.register("entity_melting", () -> new RecipeType<>() {});
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<BlockCastingRecipe>> BLOCK_CASTING = ProductiveMetalworks.RECIPE_SERIALIZERS.register("block_casting", () -> BlockCastingRecipe.SERIALIZER);
    public static final DeferredHolder<RecipeType<?>, RecipeType<BlockCastingRecipe>> BLOCK_CASTING_TYPE = ProductiveMetalworks.RECIPE_TYPES.register("block_casting", () -> new RecipeType<>() {});
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ItemCastingRecipe>> ITEM_CASTING = ProductiveMetalworks.RECIPE_SERIALIZERS.register("item_casting", () -> ItemCastingRecipe.SERIALIZER);
    public static final DeferredHolder<RecipeType<?>, RecipeType<ItemCastingRecipe>> ITEM_CASTING_TYPE = ProductiveMetalworks.RECIPE_TYPES.register("item_casting", () -> new RecipeType<>() {});

    public static final ResourceKey<CreativeModeTab> TAB_KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB, Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, ProductiveMetalworks.MODID));
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = ProductiveMetalworks.CREATIVE_MODE_TABS.register(ProductiveMetalworks.MODID, () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup." + ProductiveMetalworks.MODID))
            .icon(() -> FOUNDRY_CONTROLLERS.get(DyeColor.BLACK).get().asItem().getDefaultInstance()).build());

    public static final DeferredHolder<MenuType<?>, MenuType<FoundryControllerContainer>> FOUNDRY_CONTROLLER_CONTAINER = ProductiveMetalworks.CONTAINER_TYPES.register("foundry_controller", () ->
            IMenuTypeExtension.create(FoundryControllerContainer::new)
    );

    // 26.1: blocks/items must have their registry id baked into Properties at construction, so registration goes
    // through DeferredRegister.Blocks#registerBlock / DeferredRegister.Items#registerItem (factory + Properties)
    // instead of the old register(name, Supplier) form (which left Properties.id unset → "Block id not set").
    public static DeferredHolder<Item, Item> registerItem(String name) {
        return ProductiveMetalworks.ITEMS.registerItem(name, Item::new);
    }

    public static DeferredHolder<Item, Item> registerItem(String name, Function<Item.Properties, Item> factory, Supplier<Item.Properties> properties) {
        return ProductiveMetalworks.ITEMS.registerItem(name, factory, properties);
    }

    public static DeferredHolder<Block, Block> registerBlock(String name, Function<BlockBehaviour.Properties, Block> factory, Supplier<BlockBehaviour.Properties> properties, boolean hasItem) {
        return registerBlock(name, factory, properties, hasItem ? new Item.Properties() : null);
    }

    public static DeferredHolder<Block, Block> registerBlock(String name, Function<BlockBehaviour.Properties, Block> factory, Supplier<BlockBehaviour.Properties> properties, Item.Properties itemProperties) {
        var block = ProductiveMetalworks.BLOCKS.registerBlock(name, factory, properties);
        if (itemProperties != null) {
            // useBlockDescriptionPrefix() makes the BlockItem resolve its name as block.<modid>.<name> (matching the
            // generated lang keys); without it the item falls back to item.<modid>.<name> and shows the raw key in-game.
            ProductiveMetalworks.ITEMS.registerItem(name, p -> new BlockItem(block.get(), p), () -> itemProperties.useBlockDescriptionPrefix());
        }
        return block;
    }

    public static Map<DyeColor, DeferredHolder<Block, Block>> registerDyedBlocks(String name, Function<BlockBehaviour.Properties, Block> factory, Supplier<BlockBehaviour.Properties> properties) {
        Map<DyeColor, DeferredHolder<Block, Block>> blocks = new HashMap<>();
        for (DyeColor color : DyeColor.values()) {
            var block = ProductiveMetalworks.BLOCKS.registerBlock(color.getSerializedName() + "_" + name, factory, properties);
            ProductiveMetalworks.ITEMS.registerItem(color.getSerializedName() + "_" + name, p -> new BlockItem(block.get(), p), () -> new Item.Properties().useBlockDescriptionPrefix());
            blocks.put(color, block);
        }
        return blocks;
    }

    public static DeferredHolder<Fluid, BaseFlowingFluid.Source> registerFluid(String name, int color) {
        FLUID_COLORS.put(name, color);
        // fluid type
        var TYPE = ProductiveMetalworks.FLUID_TYPES.register(name, () -> new FluidType(MOLTEN_FLUID_TYPE_PROPERTIES));
        // fluid
        var MOLTEN = ProductiveMetalworks.FLUIDS.register(name, () -> new BaseFlowingFluid.Source(makeMoltenProperties(TYPE, name)));
        // flowing fluid
        ProductiveMetalworks.FLUIDS.register(String.format("flowing_%s", name), () -> new BaseFlowingFluid.Flowing(makeMoltenProperties(TYPE, name)));
        // fluid bucket
        registerItem(String.format("%s_bucket", name), p -> new BucketItem(MOLTEN.get(), p), () -> new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1));
        // fluid block
        registerBlock(name, p -> new HotLiquidBlock(MOLTEN.get(), p), () -> BlockBehaviour.Properties.of()
                .strength(100.0F)
                .speedFactor(0.7F)
                .noCollision()
                .liquid()
                .replaceable()
                .lightLevel(value -> 15)
        , false);

        return MOLTEN;
    }

    static private BaseFlowingFluid.Properties makeMoltenProperties(Supplier<? extends FluidType> fluidType, String name) {
        return new BaseFlowingFluid.Properties(
                fluidType,
                DeferredHolder.create(Registries.FLUID, Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, name)),
                DeferredHolder.create(Registries.FLUID, Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, String.format("flowing_%s", name)))
        )
                .bucket(DeferredHolder.create(Registries.ITEM, Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, String.format("%s_bucket", name))))
                .block(DeferredHolder.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, name)))
                .tickRate(30)
                .slopeFindDistance(4)
                .levelDecreasePerBlock(2);
    }
}
