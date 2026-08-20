package cy.jdkdigital.productivemetalworks.datagen;

import cy.jdkdigital.productivebees.common.crafting.conditions.BeeExistsCondition;
import cy.jdkdigital.productivebees.init.ModEntities;
import cy.jdkdigital.productivebees.init.ModItems;
import cy.jdkdigital.productivelib.common.condition.LazyCondition;
import cy.jdkdigital.productivelib.crafting.condition.FluidTagEmptyCondition;
import cy.jdkdigital.productivelib.registry.LibItems;
import cy.jdkdigital.productivemetalworks.ProductiveMetalworks;
import cy.jdkdigital.productivemetalworks.datagen.recipe.BlockCastingRecipeBuilder;
import cy.jdkdigital.productivemetalworks.datagen.recipe.FluidAlloyingRecipeBuilder;
import cy.jdkdigital.productivemetalworks.datagen.recipe.ItemCastingRecipeBuilder;
import cy.jdkdigital.productivemetalworks.datagen.recipe.ItemMeltingRecipeBuilder;
import cy.jdkdigital.productivemetalworks.registry.MetalworksRegistrator;
import cy.jdkdigital.productivemetalworks.registry.ModTags;
import cy.jdkdigital.productivemetalworks.util.FluidStackTemplate;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.conditions.ModLoadedCondition;
import net.neoforged.neoforge.common.conditions.NotCondition;
import net.neoforged.neoforge.common.conditions.TagEmptyCondition;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.component.DataComponentPatch;
import cy.jdkdigital.productivebees.common.crafting.ingredient.ComponentIngredient;

public class RecipeProvider extends net.minecraft.data.recipes.RecipeProvider
{
    static String[] PM_METALS = new String[]{
            "minecraft:iron", "minecraft:copper", "minecraft:gold", "minecraft:netherite",
            "productivemetalwork:aluminum", "productivemetalwork:lead", "productivemetalwork:nickel", "productivemetalwork:osmium",
            "productivemetalwork:platinum", "productivemetalwork:silver", "productivemetalwork:tin", "productivemetalwork:uranium",
            "productivemetalwork:zinc", "productivemetalwork:iridium", "productivemetalwork:steel", "productivemetalwork:invar",
            "productivemetalwork:electrum", "productivemetalwork:bronze", "productivemetalwork:brass", "productivemetalwork:enderium",
            "productivemetalwork:lumium", "productivemetalwork:signalum", "productivemetalwork:constantan",
            "productivemetalwork:refined_glowstone", "productivemetalwork:refined_obsidian"
    };
    static String[] ATO_METALS = new String[]{
            "minecraft:iron", "minecraft:copper", "minecraft:gold", "minecraft:netherite",
            "alltheores:aluminum", "alltheores:lead", "alltheores:nickel", "alltheores:osmium",
            "alltheores:platinum", "alltheores:silver", "alltheores:tin", "alltheores:uranium",
            "alltheores:zinc", "alltheores:iridium", "alltheores:steel", "alltheores:invar",
            "alltheores:electrum", "alltheores:bronze", "alltheores:brass", "alltheores:enderium",
            "alltheores:lumium", "alltheores:signalum", "alltheores:constantan"
    };
    static String[] FTB_METALS = new String[]{
            "minecraft:iron", "minecraft:copper", "minecraft:gold", "minecraft:netherite", "ftbmaterials:constantan",
            "ftbmaterials:aluminum", "ftbmaterials:lead", "ftbmaterials:nickel", "ftbmaterials:osmium",
            "ftbmaterials:platinum", "ftbmaterials:silver", "ftbmaterials:tin", "ftbmaterials:uranium",
            "ftbmaterials:zinc", "ftbmaterials:iridium", "ftbmaterials:steel", "ftbmaterials:invar",
            "ftbmaterials:electrum", "ftbmaterials:bronze", "ftbmaterials:brass", "ftbmaterials:enderium",
            "ftbmaterials:lumium", "ftbmaterials:signalum", "ftbmaterials:refined_glowstone", "ftbmaterials:obsidian"
    };
    static String[] MI_METALS = new String[]{
            "modern_industrialization:aluminum", "modern_industrialization:lead", "modern_industrialization:nickel",
            "modern_industrialization:platinum", "modern_industrialization:silver", "modern_industrialization:tin",
            "modern_industrialization:iridium", "modern_industrialization:steel", "modern_industrialization:invar",
            "modern_industrialization:electrum", "modern_industrialization:bronze", "modern_industrialization:uranium"
    };

    private final HolderGetter<Item> items;
    private final HolderGetter<Fluid> fluids;

    public RecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
        this.items = registries.lookupOrThrow(Registries.ITEM);
        this.fluids = registries.lookupOrThrow(Registries.FLUID);
    }

    // 26.1: SizedFluidIngredient.of(TagKey) was removed; build a tag-backed fluid ingredient via the lookup.
    private SizedFluidIngredient fluidTag(TagKey<Fluid> tag, int amount) {
        return new SizedFluidIngredient(FluidIngredient.of(this.fluids.getOrThrow(tag)), amount);
    }

    // 26.1 datagen: cross-mod item outputs can't be materialized as ItemStacks (components unbound + the mod
    // may be absent). Resolve to a deferred ItemStackTemplate, or null when the item isn't present on the data
    // classpath — the casting builders skip such recipes (AIR can't be templated, and an air-result recipe would
    // bake a wrong output; the dev regenerates with the compat mod present to emit them correctly).
    private static ItemStackTemplate modItem(Identifier id) {
        Item item = BuiltInRegistries.ITEM.getValue(id);
        return (item == null || item == Items.AIR) ? null : new ItemStackTemplate(item);
    }

    @Override
    protected void buildRecipes() {
        // 26.1: buildRecipes is no-arg; the RecipeOutput is held by the base class. Keep the local name so the
        // (very many) recipe definitions below read unchanged. patchouli guide-book recipe dropped (no 26.1 build).
        RecipeOutput recipeOutput = this.output;

        ShapedRecipeBuilder.shaped(this.items, RecipeCategory.MISC, LibItems.UPGRADE_TIME.get(), 1)
                .pattern("XOX").pattern("OBO").pattern("XOX")
                .define('X', MetalworksRegistrator.FIRE_BRICK.get())
                .define('O', Items.CLOCK)
                .define('B', LibItems.UPGRADE_BASE.get())
                .unlockedBy(getHasName(MetalworksRegistrator.FIRE_BRICK.get()), has(MetalworksRegistrator.FIRE_BRICK.get()))
                .unlockedBy(getHasName(Items.CLOCK), has(Items.CLOCK))
                .save(recipeOutput, "productivemetalworks:crafting/upgrade_time");

        ShapedRecipeBuilder.shaped(this.items, RecipeCategory.MISC, MetalworksRegistrator.FIRE_CLAY.get(), 4)
                .pattern("XOX").pattern("OXO").pattern("XOX")
                .define('X', Items.CLAY_BALL)
                .define('O', Tags.Items.SANDS)
                .unlockedBy(getHasName(Items.CLAY_BALL), has(Items.CLAY_BALL))
                .unlockedBy(getHasName(Items.SAND), has(Tags.Items.SANDS))
                .save(recipeOutput, "productivemetalworks:crafting/fire_clay");

        SimpleCookingRecipeBuilder.blasting(Ingredient.of(MetalworksRegistrator.FIRE_CLAY.get()), RecipeCategory.MISC, CookingBookCategory.MISC, MetalworksRegistrator.FIRE_BRICK.get(), 0.4f, 100)
                .unlockedBy(getHasName(MetalworksRegistrator.FIRE_CLAY.get()), has(MetalworksRegistrator.FIRE_CLAY.get()))
                .save(recipeOutput, "productivemetalworks:blasting/fire_brick");

        SimpleCookingRecipeBuilder.smelting(Ingredient.of(MetalworksRegistrator.FIRE_CLAY.get()), RecipeCategory.MISC, CookingBookCategory.MISC, MetalworksRegistrator.FIRE_BRICK.get(), 0.4f, 200)
                .unlockedBy(getHasName(MetalworksRegistrator.FIRE_CLAY.get()), has(MetalworksRegistrator.FIRE_CLAY.get()))
                .save(recipeOutput, "productivemetalworks:smelting/fire_brick");

        // clayworks kiln-baking compat dropped (no 26.1.2 build).

        ShapedRecipeBuilder.shaped(this.items, RecipeCategory.MISC, MetalworksRegistrator.FIRE_BRICKS.get(DyeColor.BLACK).get(), 4)
                .pattern("XX").pattern("XX")
                .define('X', MetalworksRegistrator.FIRE_BRICK.get())
                .unlockedBy(getHasName(MetalworksRegistrator.FIRE_BRICK.get()), has(MetalworksRegistrator.FIRE_BRICK.get()))
                .save(recipeOutput, "productivemetalworks:crafting/black_fire_bricks");

        MetalworksRegistrator.FIRE_BRICKS.forEach((color, holder) -> {
            ShapelessRecipeBuilder.shapeless(this.items, RecipeCategory.MISC, holder.get(), 1)
                    .requires(ModTags.Items.FIRE_BRICKS)
                    .requires(color.getTag())
                    .unlockedBy("has_fire_bricks", has(ModTags.Items.FIRE_BRICKS))
                    .save(recipeOutput, ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, "crafting/" + color.getSerializedName() + "_fire_bricks_from_dye_single")));
            ShapedRecipeBuilder.shaped(this.items, RecipeCategory.MISC, holder.get(), 8)
                    .pattern("XXX").pattern("XDX").pattern("XXX")
                    .define('X', ModTags.Items.FIRE_BRICKS)
                    .define('D', color.getTag())
                    .unlockedBy("has_fire_bricks", has(ModTags.Items.FIRE_BRICKS))
                    .save(recipeOutput, ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, "crafting/" + color.getSerializedName() + "_fire_bricks_from_dye")));
        });

        ShapedRecipeBuilder.shaped(this.items, RecipeCategory.MISC, MetalworksRegistrator.FOUNDRY_CONTROLLERS.get(DyeColor.BLACK).get(), 1)
                .pattern("XXX").pattern("XOX").pattern("XXX")
                .define('X', MetalworksRegistrator.FIRE_BRICK.get())
                .define('O', Items.BLAST_FURNACE)
                .unlockedBy(getHasName(MetalworksRegistrator.FIRE_BRICK.get()), has(MetalworksRegistrator.FIRE_BRICK.get()))
                .save(recipeOutput, "productivemetalworks:crafting/black_foundry_controller");
        MetalworksRegistrator.FOUNDRY_CONTROLLERS.forEach((color, holder) -> {
            ShapelessRecipeBuilder.shapeless(this.items, RecipeCategory.MISC, holder.get(), 1)
                    .requires(ModTags.Items.FOUNDRY_CONTROLLERS)
                    .requires(color.getTag())
                    .unlockedBy("has_foundry_controller", has(ModTags.Items.FOUNDRY_CONTROLLERS))
                    .save(recipeOutput, ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, "crafting/" + color.getSerializedName() + "_foundry_controller_from_dye")));
        });

        ShapedRecipeBuilder.shaped(this.items, RecipeCategory.MISC, MetalworksRegistrator.FOUNDRY_DRAINS.get(DyeColor.BLACK).get(), 1)
                .pattern("XXX").pattern("X X").pattern("XXX")
                .define('X', MetalworksRegistrator.FIRE_BRICK.get())
                .unlockedBy(getHasName(MetalworksRegistrator.FIRE_BRICK.get()), has(MetalworksRegistrator.FIRE_BRICK.get()))
                .save(recipeOutput, "productivemetalworks:crafting/black_foundry_drain");
        MetalworksRegistrator.FOUNDRY_DRAINS.forEach((color, holder) -> {
            ShapelessRecipeBuilder.shapeless(this.items, RecipeCategory.MISC, holder.get(), 1)
                    .requires(ModTags.Items.FOUNDRY_DRAINS)
                    .requires(color.getTag())
                    .unlockedBy("has_foundry_drain", has(ModTags.Items.FOUNDRY_DRAINS))
                    .save(recipeOutput, ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, "crafting/" + color.getSerializedName() + "_foundry_drain_from_dye")));
        });

        ShapedRecipeBuilder.shaped(this.items, RecipeCategory.MISC, MetalworksRegistrator.FOUNDRY_TANKS.get(DyeColor.BLACK).get(), 1)
                .pattern("XXX").pattern("XOX").pattern("XXX")
                .define('X', MetalworksRegistrator.FIRE_BRICK.get())
                .define('O', Tags.Items.GLASS_BLOCKS_COLORLESS)
                .unlockedBy(getHasName(MetalworksRegistrator.FIRE_BRICK.get()), has(MetalworksRegistrator.FIRE_BRICK.get()))
                .save(recipeOutput, "productivemetalworks:crafting/black_foundry_tank");
        MetalworksRegistrator.FOUNDRY_TANKS.forEach((color, holder) -> {
            ShapelessRecipeBuilder.shapeless(this.items, RecipeCategory.MISC, holder.get(), 1)
                    .requires(ModTags.Items.FOUNDRY_TANKS)
                    .requires(color.getTag())
                    .unlockedBy("has_foundry_tank", has(ModTags.Items.FOUNDRY_TANKS))
                    .save(recipeOutput, ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, "crafting/" + color.getSerializedName() + "_foundry_tank_from_dye")));
        });

        ShapedRecipeBuilder.shaped(this.items, RecipeCategory.MISC, MetalworksRegistrator.FOUNDRY_WINDOWS.get(DyeColor.BLACK).get(), 1)
                .pattern("XOX")
                .define('X', MetalworksRegistrator.FIRE_BRICK.get())
                .define('O', Tags.Items.GLASS_BLOCKS_COLORLESS)
                .unlockedBy(getHasName(MetalworksRegistrator.FIRE_BRICK.get()), has(MetalworksRegistrator.FIRE_BRICK.get()))
                .save(recipeOutput, "productivemetalworks:crafting/black_foundry_window");
        MetalworksRegistrator.FOUNDRY_WINDOWS.forEach((color, holder) -> {
            ShapelessRecipeBuilder.shapeless(this.items, RecipeCategory.MISC, holder.get(), 1)
                    .requires(ModTags.Items.FOUNDRY_WINDOWS)
                    .requires(color.getTag())
                    .unlockedBy("has_foundry_window", has(ModTags.Items.FOUNDRY_WINDOWS))
                    .save(recipeOutput, ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, "crafting/" + color.getSerializedName() + "_foundry_window_from_dye")));
        });

        MetalworksRegistrator.FOUNDRY_CAPACITORS.forEach((color, holder) -> {
            BlockCastingRecipeBuilder.of(new ItemStackTemplate(MetalworksRegistrator.FIRE_BRICKS.get(color).get().asItem()), SizedFluidIngredient.of(MetalworksRegistrator.MOLTEN_REDSTONE.get(), 1000), new ItemStackTemplate(holder.get().asItem()))
                    .save(recipeOutput, ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting/" + color.getSerializedName() + "_foundry_capacitor")));

            ShapelessRecipeBuilder.shapeless(this.items, RecipeCategory.MISC, holder.get(), 1)
                    .requires(ModTags.Items.FOUNDRY_CAPACITORS)
                    .requires(color.getTag())
                    .unlockedBy("has_foundry_capacitor", has(ModTags.Items.FOUNDRY_CAPACITORS))
                    .save(recipeOutput, ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, "crafting/" + color.getSerializedName() + "_foundry_capacitor_from_dye")));
        });

        ShapedRecipeBuilder.shaped(this.items, RecipeCategory.MISC, MetalworksRegistrator.FOUNDRY_TAP.get(), 1)
                .pattern("X X").pattern(" X ")
                .define('X', MetalworksRegistrator.FIRE_BRICK.get())
                .unlockedBy(getHasName(MetalworksRegistrator.FIRE_BRICK.get()), has(MetalworksRegistrator.FIRE_BRICK.get()))
                .save(recipeOutput, "productivemetalworks:crafting/foundry_tap");

        ShapedRecipeBuilder.shaped(this.items, RecipeCategory.MISC, MetalworksRegistrator.CASTING_TABLE.get(), 1)
                .pattern("XXX").pattern("X X").pattern("X X")
                .define('X', MetalworksRegistrator.FIRE_BRICK.get())
                .unlockedBy(getHasName(MetalworksRegistrator.FIRE_BRICK.get()), has(MetalworksRegistrator.FIRE_BRICK.get()))
                .save(recipeOutput, "productivemetalworks:crafting/casting_table");

        ShapedRecipeBuilder.shaped(this.items, RecipeCategory.MISC, MetalworksRegistrator.CASTING_BASIN.get(), 1)
                .pattern("X X").pattern("X X").pattern("XXX")
                .define('X', MetalworksRegistrator.FIRE_BRICK.get())
                .unlockedBy(getHasName(MetalworksRegistrator.FIRE_BRICK.get()), has(MetalworksRegistrator.FIRE_BRICK.get()))
                .save(recipeOutput, "productivemetalworks:crafting/casting_basin");

        ShapedRecipeBuilder.shaped(this.items, RecipeCategory.MISC, MetalworksRegistrator.LIQUID_HEATING_COIL.get(), 1)
                .pattern("CCC").pattern("CBC").pattern("XXX")
                .define('C', Tags.Items.INGOTS_COPPER)
                .define('B', Tags.Items.BUCKETS_EMPTY)
                .define('X', MetalworksRegistrator.FIRE_BRICK.get())
                .unlockedBy(getHasName(MetalworksRegistrator.FIRE_BRICK.get()), has(MetalworksRegistrator.FIRE_BRICK.get()))
                .save(recipeOutput, "productivemetalworks:crafting/liquid_heating_coil");

        ShapedRecipeBuilder.shaped(this.items, RecipeCategory.MISC, MetalworksRegistrator.POWERED_HEATING_COIL.get(), 1)
                .pattern("CCC").pattern("CAC").pattern("XXX")
                .define('C', Tags.Items.INGOTS_COPPER)
                .define('A', Items.AMETHYST_BLOCK)
                .define('X', MetalworksRegistrator.FIRE_BRICK.get())
                .unlockedBy(getHasName(MetalworksRegistrator.FIRE_BRICK.get()), has(MetalworksRegistrator.FIRE_BRICK.get()))
                .save(recipeOutput, "productivemetalworks:crafting/powered_heating_coil");

        BlockCastingRecipeBuilder.of(new ItemStackTemplate(MetalworksRegistrator.POWERED_HEATING_COIL.get().asItem()), SizedFluidIngredient.of(MetalworksRegistrator.MOLTEN_SHULKER_SHELL.get(), 1000), new ItemStackTemplate(MetalworksRegistrator.HIGH_POWERED_HEATING_COIL.get().asItem()))
                .save(recipeOutput.withConditions(new NotCondition(new ModLoadedCondition("allthemodium"))), "productivemetalworks:casting/high_powered_heating_coil");

        ShapelessRecipeBuilder.shapeless(this.items, RecipeCategory.MISC, MetalworksRegistrator.MEAT_INGOT.get(), 8)
                .requires(MetalworksRegistrator.MEAT_BLOCK.get())
                .unlockedBy(getHasName(MetalworksRegistrator.MEAT_BLOCK.get()), has(MetalworksRegistrator.MEAT_BLOCK.get()))
                .save(recipeOutput, "productivemetalworks:crafting/meat_ingot_from_block");

        ShapelessRecipeBuilder.shapeless(this.items, RecipeCategory.MISC, MetalworksRegistrator.MEAT_NUGGET.get(), 9)
                .requires(MetalworksRegistrator.MEAT_INGOT.get())
                .unlockedBy(getHasName(MetalworksRegistrator.MEAT_INGOT.get()), has(MetalworksRegistrator.MEAT_INGOT.get()))
                .save(recipeOutput, "productivemetalworks:crafting/meat_nugget_from_ingot");

        ShapedRecipeBuilder.shaped(this.items, RecipeCategory.MISC, MetalworksRegistrator.MEAT_INGOT.get(), 1)
                .pattern("TTT").pattern("TMT").pattern("TTT")
                .define('T', ModTags.Items.MEAT_NUGGETS)
                .define('M', MetalworksRegistrator.MEAT_NUGGET.get())
                .unlockedBy(getHasName(MetalworksRegistrator.MEAT_NUGGET.get()), has(MetalworksRegistrator.MEAT_NUGGET.get()))
                .save(recipeOutput, "productivemetalworks:crafting/meat_ingots_from_nugget");

        // reset tanks
        MetalworksRegistrator.FOUNDRY_TANKS.forEach((dyeColor, tankBlock) -> {
            ShapelessRecipeBuilder.shapeless(this.items, RecipeCategory.MISC, tankBlock.get(), 1)
                    .requires(tankBlock.get())
                    .unlockedBy(getHasName(tankBlock.get()), has(tankBlock.get()))
                    .save(recipeOutput, ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, "crafting/reset_" + dyeColor.getSerializedName() + "_foundry_tank")));
        });

        // Melting
        ItemMeltingRecipeBuilder.of(Ingredient.of(this.items.getOrThrow(Tags.Items.FOODS_RAW_MEAT)), new FluidStackTemplate(MetalworksRegistrator.LIQUID_MEAT.get(), 30), 1000, 0)
                .save(recipeOutput, "productivemetalworks:melting/meat");
        ItemMeltingRecipeBuilder.of(Ingredient.of(this.items.getOrThrow(Tags.Items.GLASS_BLOCKS_CHEAP)), new FluidStackTemplate(MetalworksRegistrator.MOLTEN_GLASS.get(), 1000), 1400, 0)
                .save(recipeOutput, "productivemetalworks:melting/glass");
        ItemMeltingRecipeBuilder.of(Ingredient.of(this.items.getOrThrow(Tags.Items.GLASS_PANES)), new FluidStackTemplate(MetalworksRegistrator.MOLTEN_GLASS.get(), 375), 1400, 0)
                .save(recipeOutput, "productivemetalworks:melting/glass_pane");
        ItemMeltingRecipeBuilder.of(Ingredient.of(this.items.getOrThrow(Tags.Items.SANDS)), new FluidStackTemplate(MetalworksRegistrator.MOLTEN_GLASS.get(), 1000), 1400, 0)
                .save(recipeOutput, "productivemetalworks:melting/sand");
        ItemMeltingRecipeBuilder.of(Ingredient.of(this.items.getOrThrow(Tags.Items.OBSIDIANS)), new FluidStackTemplate(MetalworksRegistrator.MOLTEN_OBSIDIAN.get(), 1000), 1300, 0)
                .save(recipeOutput, "productivemetalworks:melting/obsidian_block");
        itemMeltingRecipe(ItemTags.create(Identifier.fromNamespaceAndPath("c", "dusts/obsidian")), new FluidStackTemplate(MetalworksRegistrator.MOLTEN_OBSIDIAN.get(), 250), 1400, 0, recipeOutput);
        ItemMeltingRecipeBuilder.of(Ingredient.of(this.items.getOrThrow(Tags.Items.GEMS_DIAMOND)), new FluidStackTemplate(MetalworksRegistrator.MOLTEN_DIAMOND.get(), 100))
                .save(recipeOutput, "productivemetalworks:melting/gems/diamond");
        ItemMeltingRecipeBuilder.of(Ingredient.of(this.items.getOrThrow(Tags.Items.ORES_DIAMOND)), new FluidStackTemplate(MetalworksRegistrator.MOLTEN_DIAMOND.get(), 300))
                .save(recipeOutput, "productivemetalworks:melting/ores/diamond");
        ItemMeltingRecipeBuilder.of(Ingredient.of(this.items.getOrThrow(Tags.Items.STORAGE_BLOCKS_DIAMOND)), new FluidStackTemplate(MetalworksRegistrator.MOLTEN_DIAMOND.get(), 900))
                .save(recipeOutput, "productivemetalworks:melting/storage_blocks/diamond");
        itemMeltingRecipe(ItemTags.create(Identifier.fromNamespaceAndPath("c", "dusts/diamond")), new FluidStackTemplate(MetalworksRegistrator.MOLTEN_DIAMOND.get(), 100), recipeOutput);
        ItemMeltingRecipeBuilder.of(Ingredient.of(this.items.getOrThrow(Tags.Items.GEMS_EMERALD)), new FluidStackTemplate(MetalworksRegistrator.MOLTEN_EMERALD.get(), 100))
                .save(recipeOutput, "productivemetalworks:melting/gems/emerald");
        ItemMeltingRecipeBuilder.of(Ingredient.of(this.items.getOrThrow(Tags.Items.ORES_EMERALD)), new FluidStackTemplate(MetalworksRegistrator.MOLTEN_EMERALD.get(), 300))
                .save(recipeOutput, "productivemetalworks:melting/ores/emerald");
        ItemMeltingRecipeBuilder.of(Ingredient.of(this.items.getOrThrow(Tags.Items.STORAGE_BLOCKS_EMERALD)), new FluidStackTemplate(MetalworksRegistrator.MOLTEN_EMERALD.get(), 900))
                .save(recipeOutput, "productivemetalworks:melting/storage_blocks/emerald");
        ItemMeltingRecipeBuilder.of(Ingredient.of(this.items.getOrThrow(Tags.Items.DUSTS_REDSTONE)), new FluidStackTemplate(MetalworksRegistrator.MOLTEN_REDSTONE.get(), 100))
                .save(recipeOutput, "productivemetalworks:melting/dusts/redstone");
        ItemMeltingRecipeBuilder.of(Ingredient.of(this.items.getOrThrow(Tags.Items.STORAGE_BLOCKS_REDSTONE)), new FluidStackTemplate(MetalworksRegistrator.MOLTEN_REDSTONE.get(), 900))
                .save(recipeOutput, "productivemetalworks:melting/storage_blocks/redstone");
        ItemMeltingRecipeBuilder.of(Ingredient.of(this.items.getOrThrow(Tags.Items.ORES_REDSTONE)), new FluidStackTemplate(MetalworksRegistrator.MOLTEN_REDSTONE.get(), 500))
                .save(recipeOutput, "productivemetalworks:melting/ores/redstone");
        ItemMeltingRecipeBuilder.of(Ingredient.of(this.items.getOrThrow(Tags.Items.GEMS_LAPIS)), new FluidStackTemplate(MetalworksRegistrator.MOLTEN_LAPIS.get(), 100))
                .save(recipeOutput, "productivemetalworks:melting/gems/lapis");
        ItemMeltingRecipeBuilder.of(Ingredient.of(this.items.getOrThrow(Tags.Items.STORAGE_BLOCKS_LAPIS)), new FluidStackTemplate(MetalworksRegistrator.MOLTEN_LAPIS.get(), 900))
                .save(recipeOutput, "productivemetalworks:melting/storage_blocks/lapis");
        ItemMeltingRecipeBuilder.of(Ingredient.of(this.items.getOrThrow(Tags.Items.ORES_LAPIS)), new FluidStackTemplate(MetalworksRegistrator.MOLTEN_LAPIS.get(), 500))
                .save(recipeOutput, "productivemetalworks:melting/ores/lapis");
        ItemMeltingRecipeBuilder.of(Ingredient.of(this.items.getOrThrow(ItemTags.COALS)), new FluidStackTemplate(MetalworksRegistrator.MOLTEN_CARBON.get(), 100))
                .save(recipeOutput, "productivemetalworks:melting/coals");
        ItemMeltingRecipeBuilder.of(Ingredient.of(this.items.getOrThrow(Tags.Items.STORAGE_BLOCKS_COAL)), new FluidStackTemplate(MetalworksRegistrator.MOLTEN_CARBON.get(), 900))
                .save(recipeOutput, "productivemetalworks:melting/storage_blocks/coals");
        ItemMeltingRecipeBuilder.of(Ingredient.of(this.items.getOrThrow(ModTags.Items.STORAGE_BLOCKS_CHARCOAL)), new FluidStackTemplate(MetalworksRegistrator.MOLTEN_CARBON.get(), 900))
                .save(recipeOutput.withConditions(new NotCondition(new TagEmptyCondition(ModTags.Items.STORAGE_BLOCKS_CHARCOAL))), "productivemetalworks:melting/storage_blocks/charcoals");
        ItemMeltingRecipeBuilder.of(Ingredient.of(this.items.getOrThrow(Tags.Items.ORES_COAL)), new FluidStackTemplate(MetalworksRegistrator.MOLTEN_CARBON.get(), 300))
                .save(recipeOutput, "productivemetalworks:melting/ores/coals");
        ItemMeltingRecipeBuilder.of(Ingredient.of(this.items.getOrThrow(Tags.Items.DUSTS_GLOWSTONE)), new FluidStackTemplate(MetalworksRegistrator.MOLTEN_GLOWSTONE.get(), 100))
                .save(recipeOutput, "productivemetalworks:melting/dusts/glowstone");
        ItemMeltingRecipeBuilder.of(Ingredient.of(Items.GLOWSTONE), new FluidStackTemplate(MetalworksRegistrator.MOLTEN_GLOWSTONE.get(), 400))
                .save(recipeOutput, "productivemetalworks:melting/storage_blocks/glowstone");
        ItemMeltingRecipeBuilder.of(Ingredient.of(this.items.getOrThrow(Tags.Items.ENDER_PEARLS)), new FluidStackTemplate(MetalworksRegistrator.MOLTEN_ENDER.get(), 100))
                .save(recipeOutput, "productivemetalworks:melting/ender_pearl");
        ItemMeltingRecipeBuilder.of(Ingredient.of(Items.ENDER_EYE), new FluidStackTemplate(MetalworksRegistrator.MOLTEN_ENDER.get(), 100))
                .save(recipeOutput, "productivemetalworks:melting/ender_eye");
        ItemMeltingRecipeBuilder.of(Ingredient.of(this.items.getOrThrow(Tags.Items.GEMS_AMETHYST)), new FluidStackTemplate(MetalworksRegistrator.MOLTEN_AMETHYST.get(), 100))
                .save(recipeOutput, "productivemetalworks:melting/gems/amethyst");
        ItemMeltingRecipeBuilder.of(Ingredient.of(Items.AMETHYST_BLOCK), new FluidStackTemplate(MetalworksRegistrator.MOLTEN_AMETHYST.get(), 400))
                .save(recipeOutput, "productivemetalworks:melting/storage_blocks/amethyst");
        ItemMeltingRecipeBuilder.of(Ingredient.of(this.items.getOrThrow(Tags.Items.GEMS_QUARTZ)), new FluidStackTemplate(MetalworksRegistrator.MOLTEN_QUARTZ.get(), 100))
                .save(recipeOutput, "productivemetalworks:melting/gems/quarts");
        ItemMeltingRecipeBuilder.of(Ingredient.of(this.items.getOrThrow(Tags.Items.ORES_QUARTZ)), new FluidStackTemplate(MetalworksRegistrator.MOLTEN_QUARTZ.get(), 300))
                .save(recipeOutput, "productivemetalworks:melting/ores/quarts");
        ItemMeltingRecipeBuilder.of(Ingredient.of(Items.QUARTZ_BLOCK, Items.QUARTZ_BRICKS, Items.QUARTZ_PILLAR), new FluidStackTemplate(MetalworksRegistrator.MOLTEN_QUARTZ.get(), 400))
                .save(recipeOutput, "productivemetalworks:melting/storage_blocks/quarts");
        itemMeltingRecipe(ItemTags.create(Identifier.fromNamespaceAndPath("c", "dusts/quartz")), new FluidStackTemplate(MetalworksRegistrator.MOLTEN_QUARTZ.get(), 100), recipeOutput);
        ItemMeltingRecipeBuilder.of(Ingredient.of(Items.NETHERITE_SCRAP), new FluidStackTemplate(MetalworksRegistrator.MOLTEN_ANCIENT_DEBRIS.get(), 100))
                .save(recipeOutput, "productivemetalworks:melting/netherite_scrap");
        ItemMeltingRecipeBuilder.of(Ingredient.of(Items.ANCIENT_DEBRIS), new FluidStackTemplate(MetalworksRegistrator.MOLTEN_ANCIENT_DEBRIS.get(), 200))
                .save(recipeOutput, "productivemetalworks:melting/ancient_debris");
        ItemMeltingRecipeBuilder.of(Ingredient.of(Items.SHULKER_SHELL), new FluidStackTemplate(MetalworksRegistrator.MOLTEN_SHULKER_SHELL.get(), 100))
                .save(recipeOutput, "productivemetalworks:melting/shulker_shell");
        ItemMeltingRecipeBuilder.of(Ingredient.of(this.items.getOrThrow(ModTags.Items.MEAT_NUGGETS)), new FluidStackTemplate(MetalworksRegistrator.LIQUID_MEAT.get(), 10))
                .save(recipeOutput, "productivemetalworks:melting/meat_nugget");
        ItemMeltingRecipeBuilder.of(Ingredient.of(this.items.getOrThrow(ModTags.Items.MEAT_INGOTS)), new FluidStackTemplate(MetalworksRegistrator.LIQUID_MEAT.get(), 90))
                .save(recipeOutput, "productivemetalworks:melting/meat_ingot");
        ItemMeltingRecipeBuilder.of(Ingredient.of(MetalworksRegistrator.SHINY_MEAT_INGOT.get()), List.of(new FluidStackTemplate(MetalworksRegistrator.LIQUID_MEAT.get(), 90), new FluidStackTemplate(MetalworksRegistrator.MOLTEN_GOLD.get(), 80)))
                .save(recipeOutput, "productivemetalworks:melting/shiny_meat_ingot");
        ItemMeltingRecipeBuilder.of(Ingredient.of(Items.COMPASS), List.of(new FluidStackTemplate(MetalworksRegistrator.MOLTEN_IRON.get(), 360), new FluidStackTemplate(MetalworksRegistrator.MOLTEN_REDSTONE.get(), 100)))
                .save(recipeOutput, "productivemetalworks:melting/compass");
        ItemMeltingRecipeBuilder.of(Ingredient.of(Items.CLOCK), List.of(new FluidStackTemplate(MetalworksRegistrator.MOLTEN_GOLD.get(), 360), new FluidStackTemplate(MetalworksRegistrator.MOLTEN_REDSTONE.get(), 100)))
                .save(recipeOutput, "productivemetalworks:melting/clock");
        ItemMeltingRecipeBuilder.of(Ingredient.of(Items.DIAMOND_HORSE_ARMOR), List.of(new FluidStackTemplate(MetalworksRegistrator.MOLTEN_DIAMOND.get(), 600)))
                .save(recipeOutput, "productivemetalworks:melting/diamond_horse_armor");
        ItemMeltingRecipeBuilder.of(Ingredient.of(Items.GOLDEN_HORSE_ARMOR), List.of(new FluidStackTemplate(MetalworksRegistrator.MOLTEN_GOLD.get(), 540)))
                .save(recipeOutput, "productivemetalworks:melting/golden_horse_armor");
        ItemMeltingRecipeBuilder.of(Ingredient.of(Items.IRON_HORSE_ARMOR), List.of(new FluidStackTemplate(MetalworksRegistrator.MOLTEN_IRON.get(), 540)))
                .save(recipeOutput, "productivemetalworks:melting/iron_horse_armor");
        ItemMeltingRecipeBuilder.of(Ingredient.of(Items.BLAZE_POWDER), List.of(new FluidStackTemplate(MetalworksRegistrator.MOLTEN_BLAZE.get(), 100)))
                .save(recipeOutput, "productivemetalworks:melting/blaze_powder");
        ItemMeltingRecipeBuilder.of(Ingredient.of(Items.BLAZE_ROD), List.of(new FluidStackTemplate(MetalworksRegistrator.MOLTEN_BLAZE.get(), 400)))
                .save(recipeOutput, "productivemetalworks:melting/blaze_rod");
        ItemMeltingRecipeBuilder.of(Ingredient.of(Items.SLIME_BALL), List.of(new FluidStackTemplate(MetalworksRegistrator.MOLTEN_SLIME.get(), 100)))
                .save(recipeOutput, "productivemetalworks:melting/slime_ball");
        ItemMeltingRecipeBuilder.of(Ingredient.of(Items.SLIME_BLOCK), List.of(new FluidStackTemplate(MetalworksRegistrator.MOLTEN_SLIME.get(), 900)))
                .save(recipeOutput, "productivemetalworks:melting/slime_block");
        ItemMeltingRecipeBuilder.of(Ingredient.of(Items.MAGMA_CREAM), List.of(new FluidStackTemplate(MetalworksRegistrator.MOLTEN_MAGMA_CREAM.get(), 100)))
                .save(recipeOutput, "productivemetalworks:melting/magma_cream");
        ItemMeltingRecipeBuilder.of(Ingredient.of(Items.MAGMA_BLOCK), List.of(new FluidStackTemplate(MetalworksRegistrator.MOLTEN_MAGMA_CREAM.get(), 400)))
                .save(recipeOutput, "productivemetalworks:melting/magma_block");
        ItemMeltingRecipeBuilder.of(Ingredient.of(this.items.getOrThrow(Tags.Items.SHULKER_BOXES)), new FluidStackTemplate(MetalworksRegistrator.MOLTEN_SHULKER_SHELL.get(), 200))
                .save(recipeOutput, "productivemetalworks:melting/shulker_box");
        ItemMeltingRecipeBuilder.of(Ingredient.of(Items.HEAVY_CORE), new FluidStackTemplate(MetalworksRegistrator.MOLTEN_HEAVY_CORE.get(), 810), 3000, 30000)
                .save(recipeOutput, "productivemetalworks:melting/heavy_core");

        // Casting
        BlockCastingRecipeBuilder.of(SizedFluidIngredient.of(MetalworksRegistrator.MOLTEN_GLASS.get(), 1000), new ItemStackTemplate(Items.GLASS))
                .save(recipeOutput, "productivemetalworks:casting/misc/glass_block");
        ItemCastingRecipeBuilder.of(SizedFluidIngredient.of(MetalworksRegistrator.MOLTEN_GLASS.get(), 375), new ItemStackTemplate(Items.GLASS_PANE))
                .save(recipeOutput, "productivemetalworks:casting/misc/glass_pane");
        BlockCastingRecipeBuilder.of(SizedFluidIngredient.of(MetalworksRegistrator.MOLTEN_OBSIDIAN.get(), 1000), new ItemStackTemplate(Items.OBSIDIAN))
                .save(recipeOutput, "productivemetalworks:casting/misc/obsidian_block");
        BlockCastingRecipeBuilder.of(SizedFluidIngredient.of(MetalworksRegistrator.MOLTEN_SLIME.get(), 900), new ItemStackTemplate(Items.SLIME_BLOCK))
                .save(recipeOutput, "productivemetalworks:casting/misc/slime_block");
        BlockCastingRecipeBuilder.of(SizedFluidIngredient.of(MetalworksRegistrator.MOLTEN_MAGMA_CREAM.get(), 400), new ItemStackTemplate(Items.MAGMA_BLOCK))
                .save(recipeOutput, "productivemetalworks:casting/misc/magma_block");
        ItemCastingRecipeBuilder.of(new ItemStackTemplate(MetalworksRegistrator.CAST_GEM.get()), SizedFluidIngredient.of(MetalworksRegistrator.MOLTEN_DIAMOND.get(), 100), new ItemStackTemplate(Items.DIAMOND))
                .save(recipeOutput, "productivemetalworks:casting/gems/diamond");
        BlockCastingRecipeBuilder.of(SizedFluidIngredient.of(MetalworksRegistrator.MOLTEN_DIAMOND.get(), 900), new ItemStackTemplate(Items.DIAMOND_BLOCK))
                .save(recipeOutput, "productivemetalworks:casting/storage_blocks/diamond");
        ItemCastingRecipeBuilder.of(new ItemStackTemplate(MetalworksRegistrator.CAST_GEM.get()), SizedFluidIngredient.of(MetalworksRegistrator.MOLTEN_EMERALD.get(), 100), new ItemStackTemplate(Items.EMERALD))
                .save(recipeOutput, "productivemetalworks:casting/gems/emerald");
        BlockCastingRecipeBuilder.of(SizedFluidIngredient.of(MetalworksRegistrator.MOLTEN_EMERALD.get(), 900), new ItemStackTemplate(Items.EMERALD_BLOCK))
                .save(recipeOutput, "productivemetalworks:casting/storage_blocks/emerald");
        BlockCastingRecipeBuilder.of(SizedFluidIngredient.of(MetalworksRegistrator.MOLTEN_REDSTONE.get(), 900), new ItemStackTemplate(Items.REDSTONE_BLOCK))
                .save(recipeOutput, "productivemetalworks:casting/storage_blocks/redstone");
        ItemCastingRecipeBuilder.of(new ItemStackTemplate(MetalworksRegistrator.CAST_GEM.get()), SizedFluidIngredient.of(MetalworksRegistrator.MOLTEN_LAPIS.get(), 100), new ItemStackTemplate(Items.LAPIS_LAZULI))
                .save(recipeOutput, "productivemetalworks:casting/gems/lapis");
        BlockCastingRecipeBuilder.of(SizedFluidIngredient.of(MetalworksRegistrator.MOLTEN_LAPIS.get(), 900), new ItemStackTemplate(Items.LAPIS_BLOCK))
                .save(recipeOutput, "productivemetalworks:casting/storage_blocks/lapis");
        BlockCastingRecipeBuilder.of(SizedFluidIngredient.of(MetalworksRegistrator.MOLTEN_GLOWSTONE.get(), 400), new ItemStackTemplate(Items.GLOWSTONE))
                .save(recipeOutput, "productivemetalworks:casting/storage_blocks/glowstone");
        ItemCastingRecipeBuilder.of(new ItemStackTemplate(MetalworksRegistrator.CAST_GEM.get()), SizedFluidIngredient.of(MetalworksRegistrator.MOLTEN_QUARTZ.get(), 100), new ItemStackTemplate(Items.QUARTZ))
                .save(recipeOutput, "productivemetalworks:casting/gems/quartz");
        BlockCastingRecipeBuilder.of(SizedFluidIngredient.of(MetalworksRegistrator.MOLTEN_QUARTZ.get(), 400), new ItemStackTemplate(Items.QUARTZ_BLOCK))
                .save(recipeOutput, "productivemetalworks:casting/gems/quartz_block");
        ItemCastingRecipeBuilder.of(new ItemStackTemplate(MetalworksRegistrator.CAST_GEM.get()), SizedFluidIngredient.of(MetalworksRegistrator.MOLTEN_AMETHYST.get(), 100), new ItemStackTemplate(Items.AMETHYST_SHARD))
                .save(recipeOutput, "productivemetalworks:casting/gems/amethyst");
        BlockCastingRecipeBuilder.of(SizedFluidIngredient.of(MetalworksRegistrator.MOLTEN_AMETHYST.get(), 400), new ItemStackTemplate(Items.AMETHYST_BLOCK))
                .save(recipeOutput, "productivemetalworks:casting/gems/amethyst_block");
        ItemCastingRecipeBuilder.of(new ItemStackTemplate(MetalworksRegistrator.CAST_INGOT.get()), SizedFluidIngredient.of(MetalworksRegistrator.MOLTEN_ANCIENT_DEBRIS.get(), 100), new ItemStackTemplate(Items.NETHERITE_SCRAP))
                .save(recipeOutput, "productivemetalworks:casting/netherite_scrap");
        ItemCastingRecipeBuilder.of(SizedFluidIngredient.of(MetalworksRegistrator.MOLTEN_SLIME.get(), 100), new ItemStackTemplate(Items.SLIME_BALL))
                .save(recipeOutput, "productivemetalworks:casting/slime_ball");
        ItemCastingRecipeBuilder.of(SizedFluidIngredient.of(MetalworksRegistrator.MOLTEN_MAGMA_CREAM.get(), 100), new ItemStackTemplate(Items.MAGMA_CREAM))
                .save(recipeOutput, "productivemetalworks:casting/magma_cream");
        ItemCastingRecipeBuilder.of(new ItemStackTemplate(MetalworksRegistrator.CAST_ROD.get()), SizedFluidIngredient.of(MetalworksRegistrator.MOLTEN_BLAZE.get(), 400), new ItemStackTemplate(Items.BLAZE_ROD))
                .save(recipeOutput, "productivemetalworks:casting/blaze_rod");
        ItemCastingRecipeBuilder.of(new ItemStackTemplate(MetalworksRegistrator.CAST_GEM.get()), SizedFluidIngredient.of(MetalworksRegistrator.MOLTEN_ENDER.get(), 100), new ItemStackTemplate(Items.ENDER_PEARL))
                .save(recipeOutput, "productivemetalworks:casting/ender_pearl");
        ItemCastingRecipeBuilder.of(new ItemStackTemplate(Items.ENDER_PEARL), SizedFluidIngredient.of(MetalworksRegistrator.MOLTEN_BLAZE.get(), 100), new ItemStackTemplate(Items.ENDER_EYE), true)
                .save(recipeOutput, "productivemetalworks:casting/ender_eye");
        ItemCastingRecipeBuilder.of(SizedFluidIngredient.of(MetalworksRegistrator.MOLTEN_HEAVY_CORE.get(), 810), new ItemStackTemplate(Items.HEAVY_CORE))
                .save(recipeOutput, "productivemetalworks:casting/heavy_core");
        BlockCastingRecipeBuilder.of(Ingredient.of(this.items.getOrThrow(Tags.Items.CHESTS)), SizedFluidIngredient.of(MetalworksRegistrator.MOLTEN_SHULKER_SHELL.get(), 200), new ItemStackTemplate(Items.SHULKER_BOX), true)
                .save(recipeOutput, "productivemetalworks:casting/shulker_box");

        // Casts
        ItemCastingRecipeBuilder.of(Ingredient.of(this.items.getOrThrow(Tags.Items.INGOTS)), fluidTag(ModTags.Fluids.MOLTEN_STEEL, 360), new ItemStackTemplate(MetalworksRegistrator.CAST_INGOT.get()), true)
                .save(recipeOutput, "productivemetalworks:casting/cast/ingot");
        ItemCastingRecipeBuilder.of(Ingredient.of(Items.BRICK), fluidTag(ModTags.Fluids.MOLTEN_STEEL, 360), new ItemStackTemplate(MetalworksRegistrator.CAST_INGOT.get()), true)
                .save(recipeOutput, "productivemetalworks:casting/cast/ingot_brick");
        ItemCastingRecipeBuilder.of(Ingredient.of(this.items.getOrThrow(Tags.Items.NUGGETS)), fluidTag(ModTags.Fluids.MOLTEN_STEEL, 360), new ItemStackTemplate(MetalworksRegistrator.CAST_NUGGET.get()), true)
                .save(recipeOutput, "productivemetalworks:casting/cast/nugget");
        ItemCastingRecipeBuilder.of(Ingredient.of(this.items.getOrThrow(Tags.Items.GEMS)), fluidTag(ModTags.Fluids.MOLTEN_STEEL, 360), new ItemStackTemplate(MetalworksRegistrator.CAST_GEM.get()), true)
                .save(recipeOutput, "productivemetalworks:casting/cast/gem");
        ItemCastingRecipeBuilder.of(Ingredient.of(this.items.getOrThrow(ModTags.Items.GEARS)), fluidTag(ModTags.Fluids.MOLTEN_STEEL, 360), new ItemStackTemplate(MetalworksRegistrator.CAST_GEAR.get()), true)
                .save(recipeOutput.withConditions(new NotCondition(new TagEmptyCondition(ModTags.Items.GEARS))), "productivemetalworks:casting/cast/gear");
        ItemCastingRecipeBuilder.of(Ingredient.of(this.items.getOrThrow(ModTags.Items.RODS)), fluidTag(ModTags.Fluids.MOLTEN_STEEL, 360), new ItemStackTemplate(MetalworksRegistrator.CAST_ROD.get()), true)
                .save(recipeOutput.withConditions(new NotCondition(new TagEmptyCondition(ModTags.Items.RODS))), "productivemetalworks:casting/cast/rod");
        ItemCastingRecipeBuilder.of(Ingredient.of(this.items.getOrThrow(ModTags.Items.PLATES)), fluidTag(ModTags.Fluids.MOLTEN_STEEL, 360), new ItemStackTemplate(MetalworksRegistrator.CAST_PLATE.get()), true)
                .save(recipeOutput.withConditions(new NotCondition(new TagEmptyCondition(ModTags.Items.PLATES))), "productivemetalworks:casting/cast/plates");


        // Misc items
        ItemCastingRecipeBuilder.of(new ItemStackTemplate(LibItems.UPGRADE_BASE.get()), fluidTag(ModTags.Fluids.MOLTEN_ENDER, 100), new ItemStackTemplate(LibItems.UPGRADE_STABILITY.get()), true)
                .save(recipeOutput, "productivemetalworks:casting/misc/upgrade_stability");
        BlockCastingRecipeBuilder.of(fluidTag(ModTags.Fluids.HONEY, 1000), new ItemStackTemplate(Items.HONEY_BLOCK))
                .save(recipeOutput.withConditions(new NotCondition(new FluidTagEmptyCondition(ModTags.Fluids.HONEY))), "productivemetalworks:casting/honey_block");
        ItemCastingRecipeBuilder.of(new ItemStackTemplate(Items.GLASS_BOTTLE), fluidTag(ModTags.Fluids.HONEY, 250), new ItemStackTemplate(Items.HONEY_BOTTLE), true)
                .save(recipeOutput, "productivemetalworks:casting/misc/honey_bottle");
        ItemCastingRecipeBuilder.of(new ItemStackTemplate(Items.CARROT), fluidTag(ModTags.Fluids.MOLTEN_GOLD, 80), new ItemStackTemplate(Items.GOLDEN_CARROT), true)
                .save(recipeOutput, "productivemetalworks:casting/misc/golden_carrot");
        ItemCastingRecipeBuilder.of(new ItemStackTemplate(Items.MELON_SLICE), fluidTag(ModTags.Fluids.MOLTEN_GOLD, 80), new ItemStackTemplate(Items.GLISTERING_MELON_SLICE), true)
                .save(recipeOutput, "productivemetalworks:casting/misc/glistering_melon");
        ItemCastingRecipeBuilder.of(new ItemStackTemplate(Items.APPLE), fluidTag(ModTags.Fluids.MOLTEN_GOLD, 720), new ItemStackTemplate(Items.GOLDEN_APPLE), true)
                .save(recipeOutput, "productivemetalworks:casting/misc/golden_apple");
        ItemCastingRecipeBuilder.of(new ItemStackTemplate(Items.REDSTONE), fluidTag(ModTags.Fluids.MOLTEN_GOLD, 360), new ItemStackTemplate(Items.CLOCK), true)
                .save(recipeOutput, "productivemetalworks:casting/misc/clock");
        ItemCastingRecipeBuilder.of(new ItemStackTemplate(Items.REDSTONE), fluidTag(ModTags.Fluids.MOLTEN_IRON, 360), new ItemStackTemplate(Items.COMPASS), true)
                .save(recipeOutput, "productivemetalworks:casting/misc/compass");
        BlockCastingRecipeBuilder.of(new ItemStackTemplate(Items.TORCH), fluidTag(ModTags.Fluids.MOLTEN_IRON, 80), new ItemStackTemplate(Items.LANTERN), true)
                .save(recipeOutput, "productivemetalworks:casting/misc/lantern");
        BlockCastingRecipeBuilder.of(new ItemStackTemplate(Items.SOUL_TORCH), fluidTag(ModTags.Fluids.MOLTEN_IRON, 80), new ItemStackTemplate(Items.SOUL_LANTERN), true)
                .save(recipeOutput, "productivemetalworks:casting/misc/soul_lantern");
        ItemCastingRecipeBuilder.of(new ItemStackTemplate(MetalworksRegistrator.CAST_NUGGET.get()), fluidTag(ModTags.Fluids.MEAT, 10), new ItemStackTemplate(MetalworksRegistrator.MEAT_NUGGET.get()), false)
                .save(recipeOutput, "productivemetalworks:casting/misc/meat_nugget");
        ItemCastingRecipeBuilder.of(new ItemStackTemplate(MetalworksRegistrator.CAST_INGOT.get()), fluidTag(ModTags.Fluids.MEAT, 90), new ItemStackTemplate(MetalworksRegistrator.MEAT_INGOT.get()), false)
                .save(recipeOutput, "productivemetalworks:casting/misc/meat_ingot");
        ItemCastingRecipeBuilder.of(new ItemStackTemplate(MetalworksRegistrator.MEAT_INGOT.get()), fluidTag(ModTags.Fluids.MOLTEN_GOLD, 80), new ItemStackTemplate(MetalworksRegistrator.SHINY_MEAT_INGOT.get()), true)
                .save(recipeOutput, "productivemetalworks:casting/misc/shiny_meat_ingot");
        BlockCastingRecipeBuilder.of(new ItemStackTemplate(Items.BONE), fluidTag(ModTags.Fluids.MEAT, 720), new ItemStackTemplate(MetalworksRegistrator.MEAT_BLOCK.get().asItem()), true)
                .save(recipeOutput, "productivemetalworks:casting/misc/meat_block");
        BlockCastingRecipeBuilder.of(new ItemStackTemplate(Items.PISTON), fluidTag(ModTags.Fluids.MOLTEN_SLIME, 100), new ItemStackTemplate(Items.STICKY_PISTON), true)
                .save(recipeOutput, "productivemetalworks:casting/misc/sticky_piston");


        // Bee stuff
        ItemMeltingRecipeBuilder.of(Ingredient.of(Items.HONEYCOMB), List.of(new FluidStackTemplate(BuiltInRegistries.FLUID.getValue(Identifier.fromNamespaceAndPath("productivebees", "honey")), 100), new FluidStackTemplate(MetalworksRegistrator.MOLTEN_WAX.get(), 50)))
                .save(recipeOutput.withConditions(new ModLoadedCondition("productivebees")), "productivemetalworks:melting/honeycomb");
        ItemMeltingRecipeBuilder.of(Ingredient.of(Items.HONEYCOMB_BLOCK), List.of(new FluidStackTemplate(BuiltInRegistries.FLUID.getValue(Identifier.fromNamespaceAndPath("productivebees", "honey")), 400), new FluidStackTemplate(MetalworksRegistrator.MOLTEN_WAX.get(), 200)))
                .save(recipeOutput.withConditions(new ModLoadedCondition("productivebees")), "productivemetalworks:melting/honeycomb_block");
        ItemMeltingRecipeBuilder.of(Ingredient.of(Items.HONEY_BLOCK), new FluidStackTemplate(BuiltInRegistries.FLUID.getValue(Identifier.fromNamespaceAndPath("productivebees", "honey")), 1000))
                .save(recipeOutput.withConditions(new ModLoadedCondition("productivebees")), "productivemetalworks:melting/honey_block");
        ItemMeltingRecipeBuilder.of(Ingredient.of(this.items.getOrThrow(ModTags.Items.WAXES)), List.of(new FluidStackTemplate(MetalworksRegistrator.MOLTEN_WAX.get(), 50)))
                .save(recipeOutput.withConditions(new NotCondition(new TagEmptyCondition(ModTags.Items.WAXES))), "productivemetalworks:melting/waxes");
        ItemMeltingRecipeBuilder.of(Ingredient.of(this.items.getOrThrow(ModTags.Items.STORAGE_BLOCK_WAXES)), List.of(new FluidStackTemplate(MetalworksRegistrator.MOLTEN_WAX.get(), 450)))
                .save(recipeOutput.withConditions(new NotCondition(new TagEmptyCondition(ModTags.Items.STORAGE_BLOCK_WAXES))), "productivemetalworks:melting/wax_blocks");
        // TODO full PB compat as a way to process combs

        // Alloying

        // Obsidian
        alloyRecipe(List.of(fluidTag(FluidTags.WATER, 1), fluidTag(FluidTags.LAVA, 2)), 50, new FluidStackTemplate(MetalworksRegistrator.MOLTEN_OBSIDIAN.get(), 2), recipeOutput);
        // Netherite
        alloyRecipe(List.of(fluidTag(ModTags.Fluids.MOLTEN_ANCIENT_DEBRIS, 40), fluidTag(ModTags.Fluids.MOLTEN_GOLD, 36)), 5, new FluidStackTemplate(MetalworksRegistrator.MOLTEN_NETHERITE, 9), recipeOutput);
        // Magma cream
        alloyRecipe(List.of(fluidTag(ModTags.Fluids.MOLTEN_BLAZE, 1), fluidTag(ModTags.Fluids.MOLTEN_SLIME, 1)), 20, new FluidStackTemplate(MetalworksRegistrator.MOLTEN_MAGMA_CREAM, 1), recipeOutput);
        // Steel
        alloyRecipe(List.of(fluidTag(ModTags.Fluids.MOLTEN_CARBON, 10), fluidTag(ModTags.Fluids.MOLTEN_IRON, 9)), 4, new FluidStackTemplate(MetalworksRegistrator.MOLTEN_STEEL, 9), recipeOutput);
        // Electrum
        alloyRecipe(List.of(fluidTag(ModTags.Fluids.MOLTEN_SILVER, 1), fluidTag(ModTags.Fluids.MOLTEN_GOLD, 1)), 10, new FluidStackTemplate(MetalworksRegistrator.MOLTEN_ELECTRUM, 2), recipeOutput);
        // Bronze
        alloyRecipe(List.of(fluidTag(ModTags.Fluids.MOLTEN_COPPER, 3), fluidTag(ModTags.Fluids.MOLTEN_TIN, 1)), 10, new FluidStackTemplate(MetalworksRegistrator.MOLTEN_BRONZE, 4), recipeOutput);
        // Brass
        alloyRecipe(List.of(fluidTag(ModTags.Fluids.MOLTEN_COPPER, 3), fluidTag(ModTags.Fluids.MOLTEN_ZINC, 1)), 10, new FluidStackTemplate(MetalworksRegistrator.MOLTEN_BRASS, 4), recipeOutput);
        // Invar
        alloyRecipe(List.of(fluidTag(ModTags.Fluids.MOLTEN_IRON, 2), fluidTag(ModTags.Fluids.MOLTEN_NICKEL, 1)), 10, new FluidStackTemplate(MetalworksRegistrator.MOLTEN_INVAR, 3), recipeOutput);
        // Constantan
        alloyRecipe(List.of(fluidTag(ModTags.Fluids.MOLTEN_COPPER, 1), fluidTag(ModTags.Fluids.MOLTEN_NICKEL, 1)), 10, new FluidStackTemplate(MetalworksRegistrator.MOLTEN_CONSTANTAN, 2), recipeOutput);
        // Signalum
        alloyRecipe(List.of(fluidTag(ModTags.Fluids.MOLTEN_REDSTONE, 40), fluidTag(ModTags.Fluids.MOLTEN_SILVER, 9), fluidTag(ModTags.Fluids.MOLTEN_COPPER, 27)), 1, new FluidStackTemplate(MetalworksRegistrator.MOLTEN_SIGNALUM, 36), recipeOutput);
        // Lumium
        alloyRecipe(List.of(fluidTag(ModTags.Fluids.MOLTEN_GLOWSTONE, 40), fluidTag(ModTags.Fluids.MOLTEN_SILVER, 9), fluidTag(ModTags.Fluids.MOLTEN_TIN, 27)), 1, new FluidStackTemplate(MetalworksRegistrator.MOLTEN_LUMIUM, 36), recipeOutput);
        // Enderium
        alloyRecipe(List.of(fluidTag(ModTags.Fluids.MOLTEN_LEAD, 27), fluidTag(ModTags.Fluids.MOLTEN_PLATINUM, 9), fluidTag(ModTags.Fluids.MOLTEN_ENDER, 20)), 1, new FluidStackTemplate(MetalworksRegistrator.MOLTEN_ENDERIUM, 36), recipeOutput);
        // Refined glowstone
        alloyRecipe(List.of(fluidTag(ModTags.Fluids.MOLTEN_OSMIUM, 9), fluidTag(ModTags.Fluids.MOLTEN_GLOWSTONE, 5)), 10, new FluidStackTemplate(MetalworksRegistrator.MOLTEN_REFINED_GLOWSTONE, 9), recipeOutput);
        // Refined obsidian
        alloyRecipe(List.of(fluidTag(ModTags.Fluids.MOLTEN_OSMIUM, 90), fluidTag(ModTags.Fluids.MOLTEN_OBSIDIAN, 25), fluidTag(ModTags.Fluids.MOLTEN_DIAMOND, 10)), 1, new FluidStackTemplate(MetalworksRegistrator.MOLTEN_REFINED_OBSIDIAN, 90), recipeOutput);

        metalCompat(ProductiveMetalworks.MODID, PM_METALS, recipeOutput);
        modMetalCompat("alltheores", ATO_METALS, recipeOutput);
        modMetalCompat("ftbmaterials", FTB_METALS, recipeOutput);
        modMetalCompat("modern_industrialization", MI_METALS, recipeOutput);

        pbeesCompat(recipeOutput);
        copperCompat(recipeOutput);
        atmCompat(recipeOutput);
        idCompat(recipeOutput);
        pncCompat(recipeOutput);
        maCompat(recipeOutput);
        mekanismCompat(recipeOutput);
        immersiveCompat(recipeOutput);
        createCompat(recipeOutput);
        createIronworksCompat(recipeOutput);
        georeCompat(recipeOutput);
        vanillaCopperComp(recipeOutput);
    }

    // Vanilla stuff and tags, whatever is not mod specific
    public void metalCompat(String fluidModId, String[] metals, RecipeOutput recipeOutput) {
        for (String id: metals) {
            var rLoc = Identifier.parse(id);
            String name = rLoc.getPath();

            var fluidTag = FluidTags.create(Identifier.fromNamespaceAndPath("c", "molten_" + name));
            var blockTag = ItemTags.create(Identifier.fromNamespaceAndPath("c", "storage_blocks/" + name));
            var oreTag = ItemTags.create(Identifier.fromNamespaceAndPath("c", "ores/" + name));
            var rawBlockTag = ItemTags.create(Identifier.fromNamespaceAndPath("c", "storage_blocks/raw_" + name));
            var rawItemTag = ItemTags.create(Identifier.fromNamespaceAndPath("c", "raw_materials/" + name));
            var ingotTag = ItemTags.create(Identifier.fromNamespaceAndPath("c", "ingots/" + name));
            var nuggetTag = ItemTags.create(Identifier.fromNamespaceAndPath("c", "nuggets/" + name));
            var dustTag = ItemTags.create(Identifier.fromNamespaceAndPath("c", "dusts/" + name));
            var gearTag = ItemTags.create(Identifier.fromNamespaceAndPath("c", "gears/" + name));
            var rodTag = ItemTags.create(Identifier.fromNamespaceAndPath("c", "rods/" + name));
            var plateTag = ItemTags.create(Identifier.fromNamespaceAndPath("c", "plates/" + name));

            var fluid = BuiltInRegistries.FLUID.getValue(Identifier.fromNamespaceAndPath(fluidModId, "molten_" + name));
            var blockItem = BuiltInRegistries.ITEM.getValue(rLoc.withPath(p -> p + "_block"));
            var ingotItem = BuiltInRegistries.ITEM.getValue(rLoc.withPath(p -> p + "_ingot"));
            var nuggetItem = BuiltInRegistries.ITEM.getValue(rLoc.withPath(p -> p + "_nugget"));

            // Cast vanilla stuff
            if (rLoc.getNamespace().equals("minecraft")) {
                // Cast block
                BlockCastingRecipeBuilder.of(fluidTag(fluidTag, 810), new ItemStackTemplate(blockItem))
                        .save(recipeOutput, ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting/storage_blocks/" + name)));
                // Cast ingot
                ItemCastingRecipeBuilder.of(new ItemStackTemplate(MetalworksRegistrator.CAST_INGOT.get()), fluidTag(fluidTag, 90), new ItemStackTemplate(ingotItem))
                        .save(recipeOutput, ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting/ingots/" + name)));
                if (!nuggetItem.equals(Items.AIR)) {
                    // Cast nugget
                    ItemCastingRecipeBuilder.of(new ItemStackTemplate(MetalworksRegistrator.CAST_NUGGET.get()), fluidTag(fluidTag, 10), new ItemStackTemplate(nuggetItem))
                            .save(recipeOutput, ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting/nuggets/" + name)));
                }
            }

            if (fluid.equals(Fluids.EMPTY)) {
                ProductiveMetalworks.LOGGER.warn("empty fluid " + name);
            }

            // Melt block
            itemMeltingRecipe(blockTag, new FluidStackTemplate(fluid, 810), recipeOutput);
            // Melt raw ore block
            itemMeltingRecipe(rawBlockTag, new FluidStackTemplate(fluid, 1620), recipeOutput);
            // Melt ore
            itemMeltingRecipe(oreTag, new FluidStackTemplate(fluid, 270), recipeOutput);
            // Melt ingot
            itemMeltingRecipe(ingotTag, new FluidStackTemplate(fluid, 90), recipeOutput);
            // Melt nugget
            itemMeltingRecipe(nuggetTag, new FluidStackTemplate(fluid, 10), recipeOutput);
            // Melt raw ore
            itemMeltingRecipe(rawItemTag, new FluidStackTemplate(fluid, 180), recipeOutput);
            // Melt dust
            itemMeltingRecipe(dustTag, new FluidStackTemplate(fluid, 90), recipeOutput);
            // Melt Gear
            itemMeltingRecipe(gearTag, new FluidStackTemplate(fluid, 360), recipeOutput);
            // Melt Rod
            itemMeltingRecipe(rodTag, new FluidStackTemplate(fluid, 45), recipeOutput);
            // Melt Plate
            itemMeltingRecipe(plateTag, new FluidStackTemplate(fluid, 90), recipeOutput);
        }
    }

    // Mod specific compat recipes
    public void modMetalCompat(String modId, String[] metals, RecipeOutput recipeOutput) {
        var compatRecipeOutput = recipeOutput.withConditions(new ModLoadedCondition(modId));
        
        // vanilla gear, rod, plate melting and casting
        for (String id: metals) {
            var rLoc = Identifier.parse(id);
            String name = rLoc.getPath();
            var fluidTag = FluidTags.create(Identifier.fromNamespaceAndPath("c", "molten_" + name));

            var blockItem = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath(modId, name + "_block"));
            var ingotItem = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath(modId, name + "_ingot"));
            var nuggetItem = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath(modId, name + "_nugget"));
            var gearItem = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath(modId, name + "_gear"));
            var rodItem = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath(modId, name + "_rod"));
            var plateItem = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath(modId, name + "_plate"));

            // Casting recipes
            if (!blockItem.equals(Items.AIR)) {
                // Cast block
                BlockCastingRecipeBuilder.of(fluidTag(fluidTag, 810), new ItemStackTemplate(blockItem))
                        .save(compatRecipeOutput, Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting/storage_blocks/" + modId + "/" + name));
            }
            if (!ingotItem.equals(Items.AIR)) {
                // Cast ingot
                ItemCastingRecipeBuilder.of(new ItemStackTemplate(MetalworksRegistrator.CAST_INGOT.get()), fluidTag(fluidTag, 90), new ItemStackTemplate(ingotItem))
                        .save(compatRecipeOutput, Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting/ingots/" + modId + "/" + name));
            }
            if (!nuggetItem.equals(Items.AIR)) {
                // Cast nugget
                ItemCastingRecipeBuilder.of(new ItemStackTemplate(MetalworksRegistrator.CAST_NUGGET.get()), fluidTag(fluidTag, 10), new ItemStackTemplate(nuggetItem))
                        .save(compatRecipeOutput, Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting/nuggets/" + modId + "/"  + name));
            }
            if (!gearItem.equals(Items.AIR)) {
                // Cast gear
                ItemCastingRecipeBuilder.of(new ItemStackTemplate(MetalworksRegistrator.CAST_GEAR.get()), fluidTag(fluidTag, 360), new ItemStackTemplate(gearItem))
                        .save(compatRecipeOutput, Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting/gears/" + modId + "/" + name));
            }
            if (!rodItem.equals(Items.AIR)) {
                // Cast rod
                ItemCastingRecipeBuilder.of(new ItemStackTemplate(MetalworksRegistrator.CAST_ROD.get()), fluidTag(fluidTag, 45), new ItemStackTemplate(rodItem))
                        .save(compatRecipeOutput, Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting/rods/" + modId + "/" + name));
            }
            if (!plateItem.equals(Items.AIR)) {
                // Cast Plate
                ItemCastingRecipeBuilder.of(new ItemStackTemplate(MetalworksRegistrator.CAST_PLATE.get()), fluidTag(fluidTag, 90), new ItemStackTemplate(plateItem))
                        .save(compatRecipeOutput, Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting/plates/" + modId + "/" + name));
            }
        }
    }

    // productivebees registers its configurable bees under category paths (raw_materials/iron, alloys/steel,
    // atm/vibranium, gems/diamond …), and the egg's entity_data "type" is that full registry key. A bare name
    // like "productivebees:iron" never matches the real bee egg, so the cast/result must use the full key.
    private static final Map<String, String> BEE_PATH = Map.ofEntries(
            Map.entry("iron", "raw_materials/iron"),
            Map.entry("silver", "raw_materials/silver"),
            Map.entry("copper", "raw_materials/copper"),
            Map.entry("tin", "raw_materials/tin"),
            Map.entry("nickel", "raw_materials/nickel"),
            Map.entry("zinc", "raw_materials/zinc"),
            Map.entry("gold", "raw_materials/gold"),
            Map.entry("lead", "raw_materials/lead"),
            Map.entry("platinum", "raw_materials/platinum"),
            Map.entry("netherite", "raw_materials/netherite"),
            Map.entry("diamond", "gems/diamond"),
            Map.entry("steel", "alloys/steel"),
            Map.entry("signalum", "alloys/signalum"),
            Map.entry("lumium", "alloys/lumium"),
            Map.entry("invar", "alloys/invar"),
            Map.entry("brass", "alloys/brass"),
            Map.entry("bronze", "alloys/bronze"),
            Map.entry("constantan", "alloys/constantan"),
            Map.entry("electrum", "alloys/electrum"),
            Map.entry("enderium", "alloys/enderium"),
            Map.entry("soul_lava", "atm/soul_lava"),
            Map.entry("allthemodium", "atm/allthemodium"),
            Map.entry("vibranium", "atm/vibranium"),
            Map.entry("unobtainium", "atm/unobtainium")
            // coal, ghostly: registered at the registry root, no category prefix.
    );

    private static Identifier beeId(String name) {
        return Identifier.fromNamespaceAndPath("productivebees", BEE_PATH.getOrDefault(name, name));
    }

    private ItemStackTemplate makeBeeEgg(String name) {
        var tag = new CompoundTag();
        tag.putString("type", beeId(name).toString());
        tag.putString("id", "productivebees:configurable_bee");
        DataComponentPatch patch = DataComponentPatch.builder()
                .set(DataComponents.ENTITY_DATA, TypedEntityData.of(EntityType.byString("productivebees:configurable_bee").orElseThrow(), tag)).build();
        Item egg = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("productivebees", "spawn_egg_configurable_bee"));
        return new ItemStackTemplate(egg == null ? Items.AIR : egg, patch);
    }

    // Build the productivebees ComponentIngredient cast from a bee-egg template without materializing the stack.
    private static Ingredient beeEggCast(ItemStackTemplate template) {
        return ComponentIngredient.of(template.components(), template.item().value());
    }

    private void pbeesCompat(RecipeOutput recipeOutput) {
        ItemCastingRecipeBuilder.of(beeEggCast(makeBeeEgg("ghostly")), SizedFluidIngredient.of(BuiltInRegistries.FLUID.getValue(Identifier.parse("allthemodium:soul_lava")), 1000), makeBeeEgg("soul_lava"), true)
                .save(recipeOutput
                                .withConditions(new ModLoadedCondition("allthemodium"))
                                .withConditions(new ModLoadedCondition("productivebees"))
                                .withConditions(new LazyCondition(new BeeExistsCondition(beeId("ghostly"))))
                                .withConditions(new LazyCondition(new BeeExistsCondition(beeId("soul_lava"))))
                        , "productivemetalworks:casting/pbees/soul_lava_bee");

        ItemCastingRecipeBuilder.of(beeEggCast(makeBeeEgg("netherite")), SizedFluidIngredient.of(BuiltInRegistries.FLUID.getValue(Identifier.parse("allthemodium:molten_allthemodium")), 360), makeBeeEgg("allthemodium"), true)
                .save(recipeOutput
                                .withConditions(new ModLoadedCondition("allthemodium"))
                                .withConditions(new ModLoadedCondition("productivebees"))
                                .withConditions(new LazyCondition(new BeeExistsCondition(beeId("netherite"))))
                                .withConditions(new LazyCondition(new BeeExistsCondition(beeId("allthemodium"))))
                        , "productivemetalworks:casting/pbees/allthemodium_bee");

        ItemCastingRecipeBuilder.of(beeEggCast(makeBeeEgg("allthemodium")), SizedFluidIngredient.of(BuiltInRegistries.FLUID.getValue(Identifier.parse("allthemodium:molten_vibranium")), 360), makeBeeEgg("vibranium"), true)
                .save(recipeOutput
                                .withConditions(new ModLoadedCondition("allthemodium"))
                                .withConditions(new ModLoadedCondition("productivebees"))
                                .withConditions(new LazyCondition(new BeeExistsCondition(beeId("allthemodium"))))
                                .withConditions(new LazyCondition(new BeeExistsCondition(beeId("vibranium"))))
                        , "productivemetalworks:casting/pbees/vibranium_bee");

        ItemCastingRecipeBuilder.of(beeEggCast(makeBeeEgg("vibranium")), SizedFluidIngredient.of(BuiltInRegistries.FLUID.getValue(Identifier.parse("allthemodium:molten_unobtainium")), 360), makeBeeEgg("unobtainium"), true)
                .save(recipeOutput
                                .withConditions(new ModLoadedCondition("allthemodium"))
                                .withConditions(new ModLoadedCondition("productivebees"))
                                .withConditions(new LazyCondition(new BeeExistsCondition(beeId("vibranium"))))
                                .withConditions(new LazyCondition(new BeeExistsCondition(beeId("unobtainium"))))
                        , "productivemetalworks:casting/pbees/unobtainium_bee");

        // melt combs

        // casting recipes for bees that produce the alloy fluids
        addBeeEggRecipe("iron", ModTags.Fluids.MOLTEN_STEEL, "steel", recipeOutput);
        addBeeEggRecipe("coal", ModTags.Fluids.MOLTEN_STEEL, "steel", recipeOutput);

        addBeeEggRecipe("silver", ModTags.Fluids.MOLTEN_SIGNALUM, "signalum", recipeOutput);
        addBeeEggRecipe("copper", ModTags.Fluids.MOLTEN_SIGNALUM, "signalum", recipeOutput);

        addBeeEggRecipe("silver", ModTags.Fluids.MOLTEN_LUMIUM, "lumium", recipeOutput);
        addBeeEggRecipe("tin", ModTags.Fluids.MOLTEN_LUMIUM, "lumium", recipeOutput);

        addBeeEggRecipe("iron", ModTags.Fluids.MOLTEN_INVAR, "invar", recipeOutput);
        addBeeEggRecipe("nickel", ModTags.Fluids.MOLTEN_INVAR, "invar", recipeOutput);

        addBeeEggRecipe("copper", ModTags.Fluids.MOLTEN_BRASS, "brass", recipeOutput);
        addBeeEggRecipe("zinc", ModTags.Fluids.MOLTEN_BRASS, "brass", recipeOutput);

        addBeeEggRecipe("copper", ModTags.Fluids.MOLTEN_BRONZE, "bronze", recipeOutput);
        addBeeEggRecipe("tin", ModTags.Fluids.MOLTEN_BRONZE, "bronze", recipeOutput);

        addBeeEggRecipe("copper", ModTags.Fluids.MOLTEN_CONSTANTAN, "constantan", recipeOutput);
        addBeeEggRecipe("nickel", ModTags.Fluids.MOLTEN_CONSTANTAN, "constantan", recipeOutput);

        addBeeEggRecipe("gold", ModTags.Fluids.MOLTEN_ELECTRUM, "electrum", recipeOutput);
        addBeeEggRecipe("silver", ModTags.Fluids.MOLTEN_ELECTRUM, "electrum", recipeOutput);

        addBeeEggRecipe("lead", ModTags.Fluids.MOLTEN_ENDERIUM, "enderium", recipeOutput);
        addBeeEggRecipe("diamond", ModTags.Fluids.MOLTEN_ENDERIUM, "enderium", recipeOutput);
        addBeeEggRecipe("platinum", ModTags.Fluids.MOLTEN_ENDERIUM, "enderium", recipeOutput);

        // cast wax block
        BlockCastingRecipeBuilder.of(fluidTag(ModTags.Fluids.MOLTEN_WAX, 450), modItem(Identifier.fromNamespaceAndPath("productivebees", "wax_block")))
                .save(recipeOutput.withConditions(new ModLoadedCondition("productivebees")), "productivemetalworks:casting/pbees/wax_block");
        // cast wax item
        ItemCastingRecipeBuilder.of(fluidTag(ModTags.Fluids.MOLTEN_WAX, 50), modItem(Identifier.fromNamespaceAndPath("productivebees", "wax")))
                .save(recipeOutput.withConditions(new ModLoadedCondition("productivebees")), "productivemetalworks:casting/pbees/wax");

    }

    private void addBeeEggRecipe(String inputBee, TagKey<Fluid> fluid, String outputBee, RecipeOutput recipeOutput) {
        ItemCastingRecipeBuilder.of(beeEggCast(getBeeSpawnEgg(beeId(inputBee).toString())), fluidTag(fluid, 810), getBeeSpawnEgg(beeId(outputBee).toString()), true)
                .save(recipeOutput.withConditions(new ModLoadedCondition("productivebees"))
                                .withConditions(new LazyCondition(new BeeExistsCondition(beeId(inputBee))))
                                .withConditions(new LazyCondition(new BeeExistsCondition(beeId(outputBee)))),
                        Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting/pbees/" + outputBee + "_bee_from_" + inputBee));
    }

    private ItemStackTemplate getBeeSpawnEgg(String type) {
        var inputTag = new CompoundTag();
        inputTag.putString("id", ModEntities.CONFIGURABLE_BEE.getId().toString());
        inputTag.putString("type", type);
        DataComponentPatch patch = DataComponentPatch.builder()
                .set(DataComponents.ENTITY_DATA, TypedEntityData.of(ModEntities.CONFIGURABLE_BEE.get(), inputTag)).build();
        return new ItemStackTemplate(ModItems.CONFIGURABLE_SPAWN_EGG.get(), patch);
    }

    private void copperCompat(RecipeOutput recipeOutput) {
        // item waxing recipes
//        BuiltInRegistries.ITEM.entrySet().forEach(item -> {
//            if (item.getValue() instanceof ICopperItem copperItem) {
//                var waxedOutput = item.getValue().getDefaultInstance();
//                ICopperItem.setWaxed(waxedOutput, true);
//                ItemCastingRecipeBuilder.of(new ItemStackTemplate(cy.jdkdigital.productivebees.common.crafting.ingredient.ComponentIngredient.of(item.getValue())), fluidTag(ModTags.Fluids.MOLTEN_WAX, 50), waxedOutput, true)
//                        .save(recipeOutput.withConditions(new ModLoadedCondition("everythingcopper")), Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting/everythingcopper/wax_" + item.getKey().location().getPath()));
//            }
//        });

        // maybe copper nugget
        // lantern casting
        BlockCastingRecipeBuilder.of(new ItemStackTemplate(Items.TORCH), fluidTag(ModTags.Fluids.MOLTEN_COPPER, 80), modItem(Identifier.fromNamespaceAndPath("everythingcopper", "copper_lantern")), true)
                .save(recipeOutput.withConditions(new ModLoadedCondition("everythingcopper")), "productivemetalworks:casting/everythingcopper/lantern");
        BlockCastingRecipeBuilder.of(new ItemStackTemplate(Items.SOUL_TORCH), fluidTag(ModTags.Fluids.MOLTEN_COPPER, 80), modItem(Identifier.fromNamespaceAndPath("everythingcopper", "copper_soul_lantern")), true)
                .save(recipeOutput.withConditions(new ModLoadedCondition("everythingcopper")), "productivemetalworks:casting/everythingcopper/soul_lantern");
    }

    private void atmCompat(RecipeOutput recipeOutput) {
        for (String resource: new String[] {"allthemodium", "vibranium", "unobtainium"}) {
            var fluid = BuiltInRegistries.FLUID.getValue(Identifier.fromNamespaceAndPath("allthemodium", "molten_" + resource));

            var blockTag = ItemTags.create(Identifier.fromNamespaceAndPath("c", "storage_blocks/" + resource));
            var oreTag = ItemTags.create(Identifier.fromNamespaceAndPath("c", "ores/" + resource));
            var rawBlockTag = ItemTags.create(Identifier.fromNamespaceAndPath("c", "storage_blocks/raw_" + resource));
            var rawItemTag = ItemTags.create(Identifier.fromNamespaceAndPath("c", "raw_materials/" + resource));
            var ingotTag = ItemTags.create(Identifier.fromNamespaceAndPath("c", "ingots/" + resource));
            var nuggetTag = ItemTags.create(Identifier.fromNamespaceAndPath("c", "nuggets/" + resource));
            var dustTag = ItemTags.create(Identifier.fromNamespaceAndPath("c", "dusts/" + resource));
            var gearTag = ItemTags.create(Identifier.fromNamespaceAndPath("c", "gears/" + resource));
            var rodTag = ItemTags.create(Identifier.fromNamespaceAndPath("c", "rods/" + resource));
            var plateTag = ItemTags.create(Identifier.fromNamespaceAndPath("c", "plates/" + resource));

            var blockItem = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("allthemodium", resource + "_block"));
            var ingotItem = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("allthemodium", resource + "_ingot"));
            var nuggetItem = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("allthemodium", resource + "_nugget"));
            var gearItem = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("allthemodium", resource + "_gear"));
            var rodItem = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("allthemodium", resource + "_rod"));
            var plateItem = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("allthemodium", resource + "_plate"));

            // Melt block
            itemMeltingRecipe(blockTag, new FluidStackTemplate(fluid, 810), 3000, 0, recipeOutput);
            // Melt raw ore block
            itemMeltingRecipe(rawBlockTag, new FluidStackTemplate(fluid, 1620), 3000, 0, recipeOutput);
            // Melt ore
            itemMeltingRecipe(oreTag, new FluidStackTemplate(fluid, 270), 3000, 0, recipeOutput);
            // Melt ingot
            itemMeltingRecipe(ingotTag, new FluidStackTemplate(fluid, 90), 3000, 0, recipeOutput);
            // Melt nugget
            itemMeltingRecipe(nuggetTag, new FluidStackTemplate(fluid, 10), 3000, 0, recipeOutput);
            // Melt raw ore
            itemMeltingRecipe(rawItemTag, new FluidStackTemplate(fluid, 180), 3000, 0, recipeOutput);
            // Melt dust
            itemMeltingRecipe(dustTag, new FluidStackTemplate(fluid, 90), 3000, 0, recipeOutput);
            // Melt Gear
            itemMeltingRecipe(gearTag, new FluidStackTemplate(fluid, 720), 3000, 0, recipeOutput);
            // Melt Rod
            itemMeltingRecipe(rodTag, new FluidStackTemplate(fluid, 135), 3000, 0, recipeOutput);
            // Melt Plate
            itemMeltingRecipe(plateTag, new FluidStackTemplate(fluid, 270), 3000, 0, recipeOutput);

            // Cast block
            BlockCastingRecipeBuilder.of(SizedFluidIngredient.of(fluid, 810), new ItemStackTemplate(blockItem))
                    .save(recipeOutput.withConditions(new ModLoadedCondition("allthemodium")), Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting/atm/" + resource + "_block"));
            // Cast ingot
            ItemCastingRecipeBuilder.of(new ItemStackTemplate(MetalworksRegistrator.CAST_INGOT.get()), SizedFluidIngredient.of(fluid, 90), new ItemStackTemplate(ingotItem))
                    .save(recipeOutput.withConditions(new ModLoadedCondition("allthemodium")), Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting/atm/" + resource + "_ingot"));
            // Cast nugget
            ItemCastingRecipeBuilder.of(new ItemStackTemplate(MetalworksRegistrator.CAST_NUGGET.get()), SizedFluidIngredient.of(fluid, 10), new ItemStackTemplate(nuggetItem))
                    .save(recipeOutput.withConditions(new ModLoadedCondition("allthemodium")), Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting/atm/" + resource + "_nugget"));
            // Cast gear
            ItemCastingRecipeBuilder.of(new ItemStackTemplate(MetalworksRegistrator.CAST_GEAR.get()), SizedFluidIngredient.of(fluid, 720), new ItemStackTemplate(gearItem))
                    .save(recipeOutput.withConditions(new ModLoadedCondition("allthemodium")), Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting/atm/" + resource + "_gear"));
            // Cast rod
            ItemCastingRecipeBuilder.of(new ItemStackTemplate(MetalworksRegistrator.CAST_ROD.get()), SizedFluidIngredient.of(fluid, 135), new ItemStackTemplate(rodItem))
                    .save(recipeOutput.withConditions(new ModLoadedCondition("allthemodium")), Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting/atm/" + resource + "_rod"));
            // Cast Plate
            ItemCastingRecipeBuilder.of(new ItemStackTemplate(MetalworksRegistrator.CAST_PLATE.get()), SizedFluidIngredient.of(fluid, 270), new ItemStackTemplate(plateItem))
                    .save(recipeOutput.withConditions(new ModLoadedCondition("allthemodium")), Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting/atm/" + resource + "_plate"));
        }

        var atmFluid = BuiltInRegistries.FLUID.getValue(Identifier.fromNamespaceAndPath("allthemodium", "molten_allthemodium"));

        // atm carrot and apple
        ItemCastingRecipeBuilder.of(new ItemStackTemplate(Items.APPLE), SizedFluidIngredient.of(atmFluid, 80), modItem(Identifier.fromNamespaceAndPath("allthemodium", "allthemodium_apple")))
                .save(recipeOutput.withConditions(new ModLoadedCondition("allthemodium")), "productivemetalworks:casting/atm/apple");
        ItemCastingRecipeBuilder.of(new ItemStackTemplate(Items.CARROT), SizedFluidIngredient.of(atmFluid, 80), modItem(Identifier.fromNamespaceAndPath("allthemodium", "allthemodium_carrot")))
                .save(recipeOutput.withConditions(new ModLoadedCondition("allthemodium")), "productivemetalworks:casting/atm/carrot");

        BlockCastingRecipeBuilder.of(new ItemStackTemplate(MetalworksRegistrator.POWERED_HEATING_COIL.get().asItem()), SizedFluidIngredient.of(BuiltInRegistries.FLUID.getValue(Identifier.fromNamespaceAndPath("allthemodium", "soul_lava")), 1000), new ItemStackTemplate(MetalworksRegistrator.HIGH_POWERED_HEATING_COIL.get().asItem()))
                .save(recipeOutput.withConditions(new ModLoadedCondition("allthemodium")), "productivemetalworks:casting/atm/high_powered_heating_coil");
    }

    private void idCompat(RecipeOutput recipeOutput) {
        if (!ModList.get().isLoaded("integrateddynamics")) return;
        var menrilBlock = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("integrateddynamics", "crystalized_menril_block"));
        var menrilChunk = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("integrateddynamics", "crystalized_menril_chunk"));
        var menrilResin = BuiltInRegistries.FLUID.getValue(Identifier.fromNamespaceAndPath("integrateddynamics", "menril_resin"));
        // menril block
        ItemMeltingRecipeBuilder.of(Ingredient.of(menrilBlock), new FluidStackTemplate(menrilResin, 900))
                .save(recipeOutput.withConditions(new ModLoadedCondition("integrateddynamics")), "productivemetalworks:melting/id/crystalized_menril_block");
        // menril chunk
        ItemMeltingRecipeBuilder.of(Ingredient.of(menrilChunk), new FluidStackTemplate(menrilResin, 100))
                .save(recipeOutput.withConditions(new ModLoadedCondition("integrateddynamics")), "productivemetalworks:melting/id/crystalized_menril_chunk");
        // menril logs
        ItemMeltingRecipeBuilder.of(Ingredient.of(this.items.getOrThrow(ItemTags.create(Identifier.fromNamespaceAndPath("integrateddynamics", "menril_logs")))), new FluidStackTemplate(menrilResin, 1300))
                .save(recipeOutput.withConditions(new ModLoadedCondition("integrateddynamics")), "productivemetalworks:melting/id/menril_logs");
        // menril planks
        ItemMeltingRecipeBuilder.of(Ingredient.of(BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("integrateddynamics", "menril_planks"))), new FluidStackTemplate(menrilResin, 350))
                .save(recipeOutput.withConditions(new ModLoadedCondition("integrateddynamics")), "productivemetalworks:melting/id/menril_planks");

        // menril block
        BlockCastingRecipeBuilder.of(SizedFluidIngredient.of(menrilResin, 900), new ItemStackTemplate(menrilBlock))
                .save(recipeOutput.withConditions(new ModLoadedCondition("integrateddynamics")), "productivemetalworks:casting/id/crystalized_menril_block");
        // menril chunk
        ItemCastingRecipeBuilder.of(new ItemStackTemplate(MetalworksRegistrator.CAST_NUGGET.get()), SizedFluidIngredient.of(menrilResin, 100), new ItemStackTemplate(menrilChunk))
                .save(recipeOutput.withConditions(new ModLoadedCondition("integrateddynamics")), "productivemetalworks:casting/id/crystalized_menril_chunk");

        var chorusBlock = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("integrateddynamics", "crystalized_chorus_block"));
        var chorusChunk = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("integrateddynamics", "crystalized_chorus_chunk"));
        var chorusResin = BuiltInRegistries.FLUID.getValue(Identifier.fromNamespaceAndPath("integrateddynamics", "liquid_chorus"));
        // chorus block
        ItemMeltingRecipeBuilder.of(Ingredient.of(chorusBlock), new FluidStackTemplate(chorusResin, 1000))
                .save(recipeOutput.withConditions(new ModLoadedCondition("integrateddynamics")), "productivemetalworks:melting/id/crystalized_chorus_block");
        // chorus chunk
        ItemMeltingRecipeBuilder.of(Ingredient.of(chorusChunk), new FluidStackTemplate(chorusResin, 100))
                .save(recipeOutput.withConditions(new ModLoadedCondition("integrateddynamics")), "productivemetalworks:melting/id/crystalized_chorus_chunk");
        // proto chorus
        ItemMeltingRecipeBuilder.of(Ingredient.of(BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("integrateddynamics", "proto_chorus"))), new FluidStackTemplate(chorusResin, 250))
                .save(recipeOutput.withConditions(new ModLoadedCondition("integrateddynamics")), "productivemetalworks:melting/id/proto_chorus");
        // popped chorus
        ItemMeltingRecipeBuilder.of(Ingredient.of(Items.POPPED_CHORUS_FRUIT), new FluidStackTemplate(chorusResin, 450))
                .save(recipeOutput.withConditions(new ModLoadedCondition("integrateddynamics")), "productivemetalworks:melting/id/popped_chorus");

        // chorus block
        BlockCastingRecipeBuilder.of(SizedFluidIngredient.of(chorusResin, 1000), new ItemStackTemplate(chorusBlock))
                .save(recipeOutput.withConditions(new ModLoadedCondition("integrateddynamics")), "productivemetalworks:casting/id/crystalized_chorus_block");
        // chorus chunk
        ItemCastingRecipeBuilder.of(new ItemStackTemplate(MetalworksRegistrator.CAST_NUGGET.get()), SizedFluidIngredient.of(chorusResin, 1000), new ItemStackTemplate(chorusChunk))
                .save(recipeOutput.withConditions(new ModLoadedCondition("integrateddynamics")), "productivemetalworks:casting/id/crystalized_chorus_chunk");
    }

    private void pncCompat(RecipeOutput recipeOutput) {
        if (!ModList.get().isLoaded("pneumaticcraft")) return;
        var plasticItem = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("pneumaticcraft", "plastic"));
        var moltenPlastic = BuiltInRegistries.FLUID.getValue(Identifier.fromNamespaceAndPath("pneumaticcraft", "plastic"));
        // molten plastic
        ItemMeltingRecipeBuilder.of(Ingredient.of(plasticItem), new FluidStackTemplate(moltenPlastic, 1000))
                .save(recipeOutput.withConditions(new ModLoadedCondition("pneumaticcraft")), "productivemetalworks:melting/pnc/plastic");

        // plastic
        ItemCastingRecipeBuilder.of(new ItemStackTemplate(MetalworksRegistrator.CAST_INGOT.get()), SizedFluidIngredient.of(moltenPlastic, 1000), new ItemStackTemplate(plasticItem))
                .save(recipeOutput.withConditions(new ModLoadedCondition("pneumaticcraft")), "productivemetalworks:casting/pnc/plastic");

        // plastic alloying
        FluidAlloyingRecipeBuilder.of(List.of(SizedFluidIngredient.of(BuiltInRegistries.FLUID.getValue(Identifier.fromNamespaceAndPath("pneumaticcraft", "biodiesel")), 100), fluidTag(ModTags.Fluids.MOLTEN_CARBON, 100)), new FluidStackTemplate(moltenPlastic, 1000))
                .save(recipeOutput.withConditions(new ModLoadedCondition("pneumaticcraft")), "productivemetalworks:alloying/pnc/plastic_from_biodiesel");

        FluidAlloyingRecipeBuilder.of(List.of(SizedFluidIngredient.of(BuiltInRegistries.FLUID.getValue(Identifier.fromNamespaceAndPath("pneumaticcraft", "lpg")), 100), fluidTag(ModTags.Fluids.MOLTEN_CARBON, 100)), new FluidStackTemplate(moltenPlastic, 1000))
                .save(recipeOutput.withConditions(new ModLoadedCondition("pneumaticcraft")), "productivemetalworks:alloying/pnc/plastic_from_lpg");
    }

    private void maCompat(RecipeOutput recipeOutput) {
        if (!ModList.get().isLoaded("mysticalagriculture")) return;
        for (String essence: new String[]{"inferium", "prudentium", "tertium", "imperium", "supremium"}) {
            var essenceFluid = BuiltInRegistries.FLUID.getValue(Identifier.fromNamespaceAndPath("mysticalagradditions", "molten_" + essence));
            // melt block
            ItemMeltingRecipeBuilder.of(
                    Ingredient.of(BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("mysticalagriculture", essence + "_block"))),
                    new FluidStackTemplate(essenceFluid, 810)
            ).save(recipeOutput.withConditions(new ModLoadedCondition("mysticalagradditions")), Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, "melting/ma/" + essence + "_block"));
            // melt essence
            ItemMeltingRecipeBuilder.of(
                    Ingredient.of(BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("mysticalagriculture", essence + "_essence"))),
                    new FluidStackTemplate(essenceFluid, 90)
            ).save(recipeOutput.withConditions(new ModLoadedCondition("mysticalagradditions")), Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, "melting/ma/" + essence + "_essence"));

            // casting essence
            ItemCastingRecipeBuilder.of(SizedFluidIngredient.of(essenceFluid, 90), modItem(Identifier.fromNamespaceAndPath("mysticalagriculture", essence + "_essence")))
                    .save(recipeOutput.withConditions(new ModLoadedCondition("mysticalagradditions")), Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting/ma/" + essence + "_essence"));
            // casting ingot
            ItemCastingRecipeBuilder.of(modItem(Identifier.fromNamespaceAndPath("mysticalagriculture", "prosperity_ingot")), SizedFluidIngredient.of(essenceFluid, 180), modItem(Identifier.fromNamespaceAndPath("mysticalagriculture", essence + "_ingot")))
                    .save(recipeOutput.withConditions(new ModLoadedCondition("mysticalagradditions")), Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting/ma/" + essence + "_ingot"));
            // casting apple
            ItemCastingRecipeBuilder.of(new ItemStackTemplate(Items.GOLDEN_APPLE), SizedFluidIngredient.of(essenceFluid, 720), modItem(Identifier.fromNamespaceAndPath("mysticalagradditions", essence + "_apple")))
                    .save(recipeOutput.withConditions(new ModLoadedCondition("mysticalagradditions")), Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting/ma/" + essence + "_apple"));
            // casting block
            BlockCastingRecipeBuilder.of(SizedFluidIngredient.of(essenceFluid, 810), modItem(Identifier.fromNamespaceAndPath("mysticalagriculture", essence + "_block")))
                    .save(recipeOutput.withConditions(new ModLoadedCondition("mysticalagradditions")), Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting/ma/" + essence + "_block"));
        }
        // melt inferium ores
        ItemMeltingRecipeBuilder.of(
                Ingredient.of(this.items.getOrThrow(ItemTags.create(Identifier.fromNamespaceAndPath("c", "ores/inferium")))),
                new FluidStackTemplate(BuiltInRegistries.FLUID.getValue(Identifier.fromNamespaceAndPath("mysticalagradditions", "molten_inferium")), 360)
        ).save(recipeOutput.withConditions(new ModLoadedCondition("mysticalagradditions")), "productivemetalworks:melting/ma/inferium_ores");

        // cast inferium_seeds
        ItemCastingRecipeBuilder.of(new ItemStackTemplate(Items.WHEAT_SEEDS), SizedFluidIngredient.of(BuiltInRegistries.FLUID.getValue(Identifier.fromNamespaceAndPath("mysticalagradditions", "molten_inferium")), 720), modItem(Identifier.fromNamespaceAndPath("mysticalagriculture", "inferium_seeds")))
                .save(recipeOutput.withConditions(new ModLoadedCondition("mysticalagradditions")), "productivemetalworks:casting/ma/inferium_seeds");
    }

    private void mekanismCompat(RecipeOutput recipeOutput) {
        if (!ModList.get().isLoaded("mekanism")) return;
        for (String resource: new String[]{"steel", "bronze", "refined_glowstone", "refined_obsidian", "osmium", "tin", "lead", "uranium"}) {
            var fluidTag = FluidTags.create(Identifier.fromNamespaceAndPath("c", "molten_" + resource));
            var blockItem = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("mekanism", "block_" + resource));
            var ingotItem = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("mekanism", "ingot_" + resource));
            var nuggetItem = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("mekanism", "nugget_" + resource));

            // Cast block
            BlockCastingRecipeBuilder.of(fluidTag(fluidTag, 810), new ItemStackTemplate(blockItem))
                    .save(recipeOutput.withConditions(new ModLoadedCondition("mekanism")), Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting/mekanism/" + resource + "_block"));
            // Cast ingot
            ItemCastingRecipeBuilder.of(new ItemStackTemplate(MetalworksRegistrator.CAST_INGOT.get()), fluidTag(fluidTag, 90), new ItemStackTemplate(ingotItem))
                    .save(recipeOutput.withConditions(new ModLoadedCondition("mekanism")), Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting/mekanism/" + resource + "_ingot"));
            // Cast nugget
            ItemCastingRecipeBuilder.of(new ItemStackTemplate(MetalworksRegistrator.CAST_NUGGET.get()), fluidTag(fluidTag, 10), new ItemStackTemplate(nuggetItem))
                    .save(recipeOutput.withConditions(new ModLoadedCondition("mekanism")), Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting/mekanism/" + resource + "_nugget"));
        }
    }

    private void immersiveCompat(RecipeOutput recipeOutput) {
        if (!ModList.get().isLoaded("immersiveengineering")) return;
        for (String resource: new String[]{"steel", "aluminum", "silver", "nickel", "constantan", "electrum", "lead", "uranium"}) {
            var fluidTag = FluidTags.create(Identifier.fromNamespaceAndPath("c", "molten_" + resource));
            var blockItem = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("immersiveengineering", "storage_" + resource));
            var ingotItem = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("immersiveengineering", "ingot_" + resource));
            var nuggetItem = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("immersiveengineering", "nugget_" + resource));

            // Cast block
            BlockCastingRecipeBuilder.of(fluidTag(fluidTag, 810), new ItemStackTemplate(blockItem))
                    .save(recipeOutput.withConditions(new ModLoadedCondition("immersiveengineering")), Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting/immersiveengineering/" + resource + "_block"));
            // Cast ingot
            ItemCastingRecipeBuilder.of(new ItemStackTemplate(MetalworksRegistrator.CAST_INGOT.get()), fluidTag(fluidTag, 90), new ItemStackTemplate(ingotItem))
                    .save(recipeOutput.withConditions(new ModLoadedCondition("immersiveengineering")), Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting/immersiveengineering/" + resource + "_ingot"));
            // Cast nugget
            ItemCastingRecipeBuilder.of(new ItemStackTemplate(MetalworksRegistrator.CAST_NUGGET.get()), fluidTag(fluidTag, 10), new ItemStackTemplate(nuggetItem))
                    .save(recipeOutput.withConditions(new ModLoadedCondition("immersiveengineering")), Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting/immersiveengineering/" + resource + "_nugget"));
        }
    }

    private void createCompat(RecipeOutput recipeOutput) {
        if (!ModList.get().isLoaded("create")) return;
        for (String resource : new String[]{"iron", "gold", "copper", "zinc", "osmium", "platinum", "silver", "tin", "lead", "aluminum", "uranium", "nickel", "brass"}) {
            var fluid = BuiltInRegistries.FLUID.getValue(Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, "molten_" + resource));
            var fluidTag = FluidTags.create(Identifier.fromNamespaceAndPath("c", "molten_" + resource));

            var crushedItem = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("create", "crushed_raw_" + resource));
            var blockItem = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("create", resource + "_block"));
            var ingotItem = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("create", resource + "_ingot"));
            var nuggetItem = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("create", resource + "_nugget"));
            var plateItem = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("create", (resource.equals("gold") ? "golden" : resource) + "_sheet"));

            // Melt Crushed Raw Ore
            if (!crushedItem.equals(Items.AIR)) {
                ItemMeltingRecipeBuilder.of(Ingredient.of(crushedItem), new FluidStackTemplate(fluid, 90))
                        .save(recipeOutput.withConditions(new ModLoadedCondition("create")), Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, "melting/create/crushed_raw_" + resource));
            }

            // Cast block
            if (!blockItem.equals(Items.AIR)) {
                BlockCastingRecipeBuilder.of(fluidTag(fluidTag, 810), new ItemStackTemplate(blockItem))
                        .save(recipeOutput.withConditions(new ModLoadedCondition("create")), Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting/create/" + resource + "_block"));
            }
            // Cast ingot
            if (!ingotItem.equals(Items.AIR)) {
                ItemCastingRecipeBuilder.of(new ItemStackTemplate(MetalworksRegistrator.CAST_INGOT.get()), fluidTag(fluidTag, 90), new ItemStackTemplate(ingotItem))
                        .save(recipeOutput.withConditions(new ModLoadedCondition("create")), Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting/create/" + resource + "_ingot"));
            }
            // Cast nugget
            if (!nuggetItem.equals(Items.AIR)) {
                ItemCastingRecipeBuilder.of(new ItemStackTemplate(MetalworksRegistrator.CAST_NUGGET.get()), fluidTag(fluidTag, 10), new ItemStackTemplate(nuggetItem))
                        .save(recipeOutput.withConditions(new ModLoadedCondition("create")), Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting/create/" + resource + "_nugget"));
            }
            // Cast Sheet
            if (!plateItem.equals(Items.AIR)) {
                ItemCastingRecipeBuilder.of(new ItemStackTemplate(MetalworksRegistrator.CAST_PLATE.get()), fluidTag(fluidTag, 90), new ItemStackTemplate(plateItem))
                        .save(recipeOutput.withConditions(new ModLoadedCondition("create")), Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting/create/" + resource + "_sheet"));
            }
        }

        // Chocolate
        var chocolateFluid = BuiltInRegistries.FLUID.getValue(Identifier.fromNamespaceAndPath("create", "chocolate"));
        ItemMeltingRecipeBuilder.of(Ingredient.of(BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("create", "bar_of_chocolate"))), new FluidStackTemplate(chocolateFluid, 250))
                .save(recipeOutput.withConditions(new ModLoadedCondition("create")), "productivemetalworks:melting/create/bar_of_chocolate");

        ItemCastingRecipeBuilder.of(new ItemStackTemplate(MetalworksRegistrator.CAST_INGOT.get()), SizedFluidIngredient.of(chocolateFluid, 250), modItem(Identifier.fromNamespaceAndPath("create", "bar_of_chocolate")))
                .save(recipeOutput.withConditions(new ModLoadedCondition("create")), "productivemetalworks:casting/create/bar_of_chocolate");
        ItemCastingRecipeBuilder.of(new ItemStackTemplate(Items.SWEET_BERRIES), SizedFluidIngredient.of(chocolateFluid, 250), modItem(Identifier.fromNamespaceAndPath("create", "chocolate_glazed_berries")))
                .save(recipeOutput.withConditions(new ModLoadedCondition("create")), "productivemetalworks:casting/create/chocolate_glazed_berries");
    }

    private void createIronworksCompat(RecipeOutput recipeOutput) {
        if (!ModList.get().isLoaded("create_ironworks")) return;
        for (String resource : new String[]{"tin", "bronze", "steel"}) {
            var fluid = BuiltInRegistries.FLUID.getValue(Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, "molten_" + resource));
            var fluidTag = FluidTags.create(Identifier.fromNamespaceAndPath("c", "molten_" + resource));

            var crushedItem = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("create_ironworks", "crushed_raw_" + resource));
            var blockItem = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("create_ironworks", resource + "_block"));
            var ingotItem = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("create_ironworks", resource + "_ingot"));
            var nuggetItem = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("create_ironworks", resource + "_nugget"));
            var plateItem = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("create_ironworks", resource + "_sheet"));

            // Melt Crushed Raw Ore
            if (!crushedItem.equals(Items.AIR)) {
                ItemMeltingRecipeBuilder.of(Ingredient.of(crushedItem), new FluidStackTemplate(fluid, 90))
                        .save(recipeOutput.withConditions(new ModLoadedCondition("create_ironworks")), Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, "melting/create_ironworks/crushed_raw_" + resource));
            }

            // Cast block
            if (!blockItem.equals(Items.AIR)) {
                BlockCastingRecipeBuilder.of(fluidTag(fluidTag, 810), new ItemStackTemplate(blockItem))
                        .save(recipeOutput.withConditions(new ModLoadedCondition("create_ironworks")), Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting/create_ironworks/" + resource + "_block"));
            }
            // Cast ingot
            if (!ingotItem.equals(Items.AIR)) {
                ItemCastingRecipeBuilder.of(new ItemStackTemplate(MetalworksRegistrator.CAST_INGOT.get()), fluidTag(fluidTag, 90), new ItemStackTemplate(ingotItem))
                        .save(recipeOutput.withConditions(new ModLoadedCondition("create_ironworks")), Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting/create_ironworks/" + resource + "_ingot"));
            }
            // Cast nugget
            if (!nuggetItem.equals(Items.AIR)) {
                ItemCastingRecipeBuilder.of(new ItemStackTemplate(MetalworksRegistrator.CAST_NUGGET.get()), fluidTag(fluidTag, 10), new ItemStackTemplate(nuggetItem))
                        .save(recipeOutput.withConditions(new ModLoadedCondition("create_ironworks")), Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting/create_ironworks/" + resource + "_nugget"));
            }
            // Cast Sheet
            if (!plateItem.equals(Items.AIR)) {
                ItemCastingRecipeBuilder.of(new ItemStackTemplate(MetalworksRegistrator.CAST_PLATE.get()), fluidTag(fluidTag, 90), new ItemStackTemplate(plateItem))
                        .save(recipeOutput.withConditions(new ModLoadedCondition("create_ironworks")), Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting/create_ironworks/" + resource + "_sheet"));
            }
        }
    }

    private void georeCompat(RecipeOutput recipeOutput) {
        if (!ModList.get().isLoaded("geore")) return;
        for (String resource : new String[]{"iron", "gold", "copper", "zinc", "uranium", "tin", "silver", "platinum", "osmium", "aluminum", "lead", "nickel"}) {
            var fluid = BuiltInRegistries.FLUID.getValue(Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, "molten_" + (resource.equals("coal") ? "carbon" : resource)));

            var shardItem = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("geore", resource + "_shard"));
            var blockItem = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("geore", resource + "_block"));

            // melt block
            ItemMeltingRecipeBuilder.of(
                    Ingredient.of(blockItem),
                    new FluidStackTemplate(fluid, 360)
            ).save(recipeOutput.withConditions(new ModLoadedCondition("geore")), Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, "melting/geore/" + resource + "_block"));
            // melt shard
            ItemMeltingRecipeBuilder.of(
                    Ingredient.of(shardItem),
                    new FluidStackTemplate(fluid, 90)
            ).save(recipeOutput.withConditions(new ModLoadedCondition("geore")), Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, "melting/geore/" + resource + "_shard"));
        }
        for (String resource : new String[]{"coal", "diamond", "emerald", "lapis", "quartz", "redstone", "ancient_debris"}) {
            var fluid = BuiltInRegistries.FLUID.getValue(Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, "molten_" + (resource.equals("coal") ? "carbon" : resource)));

            var shardItem = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("geore", resource + "_shard"));
            var blockItem = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("geore", resource + "_block"));

            // melt block
            ItemMeltingRecipeBuilder.of(
                    Ingredient.of(blockItem),
                    new FluidStackTemplate(fluid, 400)
            ).save(recipeOutput.withConditions(new ModLoadedCondition("geore")), Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, "melting/geore/" + resource + "_block"));
            // melt shard
            ItemMeltingRecipeBuilder.of(
                    Ingredient.of(shardItem),
                    new FluidStackTemplate(fluid, 100)
            ).save(recipeOutput.withConditions(new ModLoadedCondition("geore")), Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, "melting/geore/" + resource + "_shard"));
        }
    }

    private void vanillaCopperComp(RecipeOutput recipeOutput) {
        List<Item> copperBlocks = new ArrayList<>(Arrays.asList(copperItems("copper")));
        copperBlocks.add(Items.WAXED_COPPER_BLOCK);
        ItemMeltingRecipeBuilder.of(Ingredient.of(copperBlocks.toArray(new Item[]{})), new FluidStackTemplate(MetalworksRegistrator.MOLTEN_COPPER.get(), 810))
                .save(recipeOutput, "productivemetalworks:melting/copper");

        ItemMeltingRecipeBuilder.of(copperItemIngredient("chiseled_copper"), new FluidStackTemplate(MetalworksRegistrator.MOLTEN_COPPER.get(), 200))
            .save(recipeOutput, "productivemetalworks:melting/chiseled_copper");

        ItemMeltingRecipeBuilder.of(copperItemIngredient("copper_grate"), new FluidStackTemplate(MetalworksRegistrator.MOLTEN_COPPER.get(), 200))
                .save(recipeOutput, "productivemetalworks:melting/copper_grate");

        ItemMeltingRecipeBuilder.of(copperItemIngredient("cut_copper"), new FluidStackTemplate(MetalworksRegistrator.MOLTEN_COPPER.get(), 200))
                .save(recipeOutput, "productivemetalworks:melting/cut_copper");

        ItemMeltingRecipeBuilder.of(copperItemIngredient("cut_copper_stairs"), new FluidStackTemplate(MetalworksRegistrator.MOLTEN_COPPER.get(), 200))
                .save(recipeOutput, "productivemetalworks:melting/cut_copper_stairs");

        ItemMeltingRecipeBuilder.of(copperItemIngredient("cut_copper_slab"), new FluidStackTemplate(MetalworksRegistrator.MOLTEN_COPPER.get(), 100))
                .save(recipeOutput, "productivemetalworks:melting/cut_copper_slab");

        ItemMeltingRecipeBuilder.of(copperItemIngredient("copper_door"), new FluidStackTemplate(MetalworksRegistrator.MOLTEN_COPPER.get(), 180))
                .save(recipeOutput, "productivemetalworks:melting/copper_door");

        ItemMeltingRecipeBuilder.of(copperItemIngredient("copper_trapdoor"), new FluidStackTemplate(MetalworksRegistrator.MOLTEN_COPPER.get(), 270))
                .save(recipeOutput, "productivemetalworks:melting/copper_trapdoor");

        ItemMeltingRecipeBuilder.of(
                copperItemIngredient("copper_bulb"),
                List.of(
                    new FluidStackTemplate(MetalworksRegistrator.MOLTEN_COPPER.get(), 600),
                    new FluidStackTemplate(MetalworksRegistrator.MOLTEN_REDSTONE.get(), 20),
                    new FluidStackTemplate(MetalworksRegistrator.MOLTEN_BLAZE.get(), 100)
                )
        ).save(recipeOutput, "productivemetalworks:melting/copper_bulb");

        ItemMeltingRecipeBuilder.of(Ingredient.of(Items.LIGHTNING_ROD), new FluidStackTemplate(MetalworksRegistrator.MOLTEN_COPPER.get(), 270))
                .save(recipeOutput, "productivemetalworks:melting/lightning_rod");
        ItemMeltingRecipeBuilder.of(
                Ingredient.of(Items.SPYGLASS),
                List.of(
                        new FluidStackTemplate(MetalworksRegistrator.MOLTEN_COPPER.get(), 180),
                        new FluidStackTemplate(MetalworksRegistrator.MOLTEN_AMETHYST.get(), 100)
                )
        ).save(recipeOutput, "productivemetalworks:melting/spyglass");

        // melting iron items
        ItemMeltingRecipeBuilder.of(Ingredient.of(Items.IRON_BARS), new FluidStackTemplate(MetalworksRegistrator.MOLTEN_IRON.get(), 30))
                .save(recipeOutput, "productivemetalworks:melting/iron_bars");
        ItemMeltingRecipeBuilder.of(Ingredient.of(Items.IRON_TRAPDOOR), new FluidStackTemplate(MetalworksRegistrator.MOLTEN_IRON.get(), 360))
                .save(recipeOutput, "productivemetalworks:melting/iron_trapdoor");
        ItemMeltingRecipeBuilder.of(Ingredient.of(Items.IRON_DOOR), new FluidStackTemplate(MetalworksRegistrator.MOLTEN_IRON.get(), 180))
                .save(recipeOutput, "productivemetalworks:melting/iron_door");
    }

    private Ingredient copperItemIngredient(String name) {
        return Ingredient.of(copperItems(name));
    }

    private Item[] copperItems(String name) {
        var states = new String[]{"", "exposed_", "weathered_", "oxidized_", "waxed_", "waxed_exposed_", "waxed_weathered_", "waxed_oxidized_"};
        return Arrays.stream(states).map(state ->
                BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace(state + name))
        ).filter(item -> item != null && item != Items.AIR).toList().toArray(new Item[0]);
    }

    private void itemMeltingRecipe(TagKey<Item> tag, FluidStackTemplate output, RecipeOutput recipeOutput) {
        itemMeltingRecipe(tag, output, 1000, 0, recipeOutput);
    }

    private void itemMeltingRecipe(TagKey<Item> tag, FluidStackTemplate output, int min, int max, RecipeOutput recipeOutput) {
        ItemMeltingRecipeBuilder.of(Ingredient.of(this.items.getOrThrow(tag)), output, min, max).save(recipeOutput.withConditions(new NotCondition(new TagEmptyCondition(tag))), Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, "melting/" + tag.location().getPath()));
    }

    private void alloyRecipe(List<SizedFluidIngredient> inputs, int speed, FluidStackTemplate output, RecipeOutput recipeOutput) {
        // 26.1: TagFluidIngredient was removed, so the per-input fluid-tag-empty guard is dropped; the alloy
        // recipe is emitted unconditionally (PMW's own molten-fluid tags are always populated).
        FluidAlloyingRecipeBuilder.of(inputs, speed, output).save(recipeOutput, ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, "alloying/" + BuiltInRegistries.FLUID.getKey(output.getFluid()).getPath())));
    }

    public static class Runner extends net.minecraft.data.recipes.RecipeProvider.Runner
    {
        public Runner(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> registries) {
            super(packOutput, registries);
        }

        @Override
        protected net.minecraft.data.recipes.RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
            return new RecipeProvider(registries, output);
        }

        @Override
        public String getName() {
            return "Productive Metalworks Recipes";
        }
    }
}
