package cy.jdkdigital.productivemetalworks.registry;

import cy.jdkdigital.productivelib.util.ImmutableFluidStack;
import cy.jdkdigital.productivemetalworks.ProductiveMetalworks;
import cy.jdkdigital.productivemetalworks.common.block.*;
import cy.jdkdigital.productivemetalworks.common.block.entity.*;
import cy.jdkdigital.productivemetalworks.common.datamap.EntityMeltingMap;
import cy.jdkdigital.productivemetalworks.common.datamap.FuelMap;
import cy.jdkdigital.productivemetalworks.common.menu.FoundryControllerContainer;
import cy.jdkdigital.productivemetalworks.recipe.*;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
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
import net.minecraft.world.level.material.Fluid;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.datamaps.DataMapType;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class MetalworksRegistrator
{
    public static void register() {}

    public static final DataMapType<Fluid, FuelMap> FUEL_MAP = DataMapType.builder(ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "fuel_map"), Registries.FLUID, FuelMap.CODEC).synced(FuelMap.CODEC, false).build();
    public static final DataMapType<EntityType<?>, EntityMeltingMap> ENTITY_MELTING_MAP = DataMapType.builder(ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "entity_melting"), Registries.ENTITY_TYPE, EntityMeltingMap.CODEC).synced(EntityMeltingMap.CODEC, false).build();
    public static final Supplier<DataComponentType<ImmutableFluidStack>> FLUID_STACK = ProductiveMetalworks.DATA_COMPONENTS.register("fluid_stack", () -> DataComponentType.<ImmutableFluidStack>builder().persistent(ImmutableFluidStack.CODEC).networkSynchronized(ImmutableFluidStack.STREAM_CODEC).build());

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
    public static final Map<DyeColor, DeferredHolder<Block, Block>> FOUNDRY_CONTROLLERS = registerDyedBlocks("foundry_controller", () -> new FoundryControllerBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BRICKS).noOcclusion()));
    public static final Map<DyeColor, DeferredHolder<Block, Block>> FOUNDRY_DRAINS = registerDyedBlocks("foundry_drain", () -> new FoundryDrainBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BRICKS)));
    public static final Map<DyeColor, DeferredHolder<Block, Block>> FOUNDRY_TANKS = registerDyedBlocks("foundry_tank", () -> new FoundryTankBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BRICKS).noOcclusion()));
    public static final Map<DyeColor, DeferredHolder<Block, Block>> FOUNDRY_WINDOWS = registerDyedBlocks("foundry_window", () -> new FireBricksBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS)));
    public static final Map<DyeColor, DeferredHolder<Block, Block>> FIRE_BRICKS = registerDyedBlocks("fire_bricks", () -> new FireBricksBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BRICKS)));

    public static final DeferredHolder<Block, Block> FOUNDRY_TAP = registerBlock("foundry_tap", () -> new FoundryTapBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BRICKS)), true);
    public static final DeferredHolder<Block, Block> CASTING_BASIN = registerBlock("casting_basin", () -> new CastingBasinBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CAULDRON)), true);
    public static final DeferredHolder<Block, Block> CASTING_TABLE = registerBlock("casting_table", () -> new CastingTableBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CAULDRON)), true);
    public static final DeferredHolder<Block, Block> LIQUID_HEATING_COIL = registerBlock("liquid_heating_coil", () -> new AttachedBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)), true);
    public static final DeferredHolder<Block, Block> POWERED_HEATING_COIL = registerBlock("powered_heating_coil", () -> new AttachedBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)), true);
    public static final DeferredHolder<Block, Block> FIRE_CLAY = registerBlock("fire_clay", () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.CLAY)), true);
    public static final DeferredHolder<Block, Block> MEAT_BLOCK = registerBlock("meat_block", () -> new MeatBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.DIRT).noOcclusion().sound(SoundType.SLIME_BLOCK)), new Item.Properties().craftRemainder(Items.BONE));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FoundryControllerBlockEntity>> FOUNDRY_CONTROLLER_BLOCK_ENTITY = ProductiveMetalworks.BLOCK_ENTITIES.register("foundry_controller", () -> BlockEntityType.Builder.of(FoundryControllerBlockEntity::new, FOUNDRY_CONTROLLERS.values().stream().map(DeferredHolder::get).toList().toArray(new Block[0])).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FoundryDrainBlockEntity>> FOUNDRY_DRAIN_BLOCK_ENTITY = ProductiveMetalworks.BLOCK_ENTITIES.register("foundry_drain", () -> BlockEntityType.Builder.of(FoundryDrainBlockEntity::new, FOUNDRY_DRAINS.values().stream().map(DeferredHolder::get).toList().toArray(new Block[0])).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FoundryTankBlockEntity>> FOUNDRY_TANK_BLOCK_ENTITY = ProductiveMetalworks.BLOCK_ENTITIES.register("foundry_tank", () -> BlockEntityType.Builder.of(FoundryTankBlockEntity::new, FOUNDRY_TANKS.values().stream().map(DeferredHolder::get).toList().toArray(new Block[0])).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FoundryTapBlockEntity>> FOUNDRY_TAP_BLOCK_ENTITY = ProductiveMetalworks.BLOCK_ENTITIES.register("foundry_tap", () -> BlockEntityType.Builder.of(FoundryTapBlockEntity::new, FOUNDRY_TAP.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CastingBlockEntity>> CASTING_BLOCK_ENTITY = ProductiveMetalworks.BLOCK_ENTITIES.register("casting", () -> BlockEntityType.Builder.of(CastingBlockEntity::new, CASTING_TABLE.get(), CASTING_BASIN.get()).build(null));

    // Items
    public static DeferredHolder<Item, Item> FIRE_BRICK = registerItem("fire_brick");
    public static DeferredHolder<Item, Item> MEAT_INGOT = registerItem("meat_ingot", () -> new Item(new Item.Properties().food(Foods.RABBIT_STEW)));
    public static DeferredHolder<Item, Item> SHINY_MEAT_INGOT = registerItem("shiny_meat_ingot", () -> new Item(new Item.Properties().food(Foods.GOLDEN_CARROT)));

    // Casts
    public static DeferredHolder<Item, Item> CAST_INGOT = registerItem("ingot_cast");
    public static DeferredHolder<Item, Item> CAST_NUGGET = registerItem("nugget_cast");
    public static DeferredHolder<Item, Item> CAST_GEM = registerItem("gem_cast");
    public static DeferredHolder<Item, Item> CAST_GEAR = registerItem("gear_cast");
    public static DeferredHolder<Item, Item> CAST_ROD = registerItem("rod_cast");
    public static DeferredHolder<Item, Item> CAST_PLATE = registerItem("plate_cast");

    // Fluids
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
//    public static DeferredHolder<Fluid, BaseFlowingFluid.Source> MOLTEN_REFINED_OBSIDIAN = registerFluid("molten_refined_obsidian", 0xff654c89);

    // Recipes
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<?>> FLUID_ALLOYING = ProductiveMetalworks.RECIPE_SERIALIZERS.register("fluid_alloying", FluidAlloyingRecipe.Serializer::new);
    public static final DeferredHolder<RecipeType<?>, RecipeType<FluidAlloyingRecipe>> FLUID_ALLOYING_TYPE = ProductiveMetalworks.RECIPE_TYPES.register("fluid_alloying", () -> new RecipeType<>() {});
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<?>> ITEM_MELTING = ProductiveMetalworks.RECIPE_SERIALIZERS.register("item_melting", ItemMeltingRecipe.Serializer::new);
    public static final DeferredHolder<RecipeType<?>, RecipeType<ItemMeltingRecipe>> ITEM_MELTING_TYPE = ProductiveMetalworks.RECIPE_TYPES.register("item_melting", () -> new RecipeType<>() {});
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<?>> BLOCK_CASTING = ProductiveMetalworks.RECIPE_SERIALIZERS.register("block_casting", BlockCastingRecipe.Serializer::new);
    public static final DeferredHolder<RecipeType<?>, RecipeType<BlockCastingRecipe>> BLOCK_CASTING_TYPE = ProductiveMetalworks.RECIPE_TYPES.register("block_casting", () -> new RecipeType<>() {});
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<?>> ITEM_CASTING = ProductiveMetalworks.RECIPE_SERIALIZERS.register("item_casting", ItemCastingRecipe.Serializer::new);
    public static final DeferredHolder<RecipeType<?>, RecipeType<ItemCastingRecipe>> ITEM_CASTING_TYPE = ProductiveMetalworks.RECIPE_TYPES.register("item_casting", () -> new RecipeType<>() {});

    public static final ResourceKey<CreativeModeTab> TAB_KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, ProductiveMetalworks.MODID));
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = ProductiveMetalworks.CREATIVE_MODE_TABS.register(ProductiveMetalworks.MODID, () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup." + ProductiveMetalworks.MODID))
            .icon(() -> FOUNDRY_CONTROLLERS.get(DyeColor.BLACK).get().asItem().getDefaultInstance()).build());

    public static final DeferredHolder<MenuType<?>, MenuType<FoundryControllerContainer>> FOUNDRY_CONTROLLER_CONTAINER = ProductiveMetalworks.CONTAINER_TYPES.register("foundry_controller", () ->
            IMenuTypeExtension.create(FoundryControllerContainer::new)
    );

    public static DeferredHolder<Item, Item> registerItem(String name) {
        return registerItem(name, () -> new Item(new Item.Properties()));
    }

    public static DeferredHolder<Item, Item> registerItem(String name, Supplier<Item> supplier) {
        return ProductiveMetalworks.ITEMS.register(name, supplier);
    }

    public static DeferredHolder<Block, Block> registerBlock(String name, Supplier<Block> supplier, boolean hasItem) {
        return registerBlock(name, supplier, hasItem ? new Item.Properties() : null);
    }

    public static DeferredHolder<Block, Block> registerBlock(String name, Supplier<Block> supplier, Item.Properties properties) {
        var block = ProductiveMetalworks.BLOCKS.register(name, supplier);
        if (properties != null) {
            registerItem(name, () -> new BlockItem(block.get(), properties));
        }
        return block;
    }

    public static Map<DyeColor, DeferredHolder<Block, Block>> registerDyedBlocks(String name, Supplier<Block> supplier) {
        Map<DyeColor, DeferredHolder<Block, Block>> blocks = new HashMap<>();
        for (DyeColor color : DyeColor.values()) {
            var block = ProductiveMetalworks.BLOCKS.register(color.getSerializedName() + "_" + name, supplier);
            registerItem(color.getSerializedName() + "_" + name, () -> new BlockItem(block.get(), new Item.Properties()));
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
        registerItem(String.format("%s_bucket", name), () -> new BucketItem(MOLTEN.get(), new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));
        // fluid block
        registerBlock(name, () -> new HotLiquidBlock(MOLTEN.get(), Block.Properties.of()
                .strength(100.0F)
                .speedFactor(0.7F)
                .noCollission()
                .liquid()
                .replaceable()
        ), false);

        return MOLTEN;
    }

    static private BaseFlowingFluid.Properties makeMoltenProperties(Supplier<? extends FluidType> fluidType, String name) {
        return new BaseFlowingFluid.Properties(
                fluidType,
                DeferredHolder.create(Registries.FLUID, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, name)),
                DeferredHolder.create(Registries.FLUID, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, String.format("flowing_%s", name)))
        )
                .bucket(DeferredHolder.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, String.format("%s_bucket", name))))
                .block(DeferredHolder.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, name)))
                .tickRate(30)
                .slopeFindDistance(4)
                .levelDecreasePerBlock(2);
    }
}
