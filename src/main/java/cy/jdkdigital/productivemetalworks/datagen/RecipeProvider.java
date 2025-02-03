package cy.jdkdigital.productivemetalworks.datagen;

import com.mojang.datafixers.util.Pair;
import cy.jdkdigital.productivelib.crafting.condition.FluidTagEmptyCondition;
import cy.jdkdigital.productivemetalworks.ProductiveMetalworks;
import cy.jdkdigital.productivemetalworks.datagen.recipe.*;
import cy.jdkdigital.productivemetalworks.registry.MetalworksRegistrator;
import cy.jdkdigital.productivemetalworks.registry.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.conditions.IConditionBuilder;
import net.neoforged.neoforge.common.conditions.ModLoadedCondition;
import net.neoforged.neoforge.common.conditions.NotCondition;
import net.neoforged.neoforge.common.conditions.TagEmptyCondition;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import net.neoforged.neoforge.fluids.crafting.TagFluidIngredient;
import net.silentchaos512.gear.crafting.ingredient.PartMaterialIngredient;
import net.silentchaos512.gear.gear.material.MaterialCategories;
import net.silentchaos512.gear.setup.GearItemSets;
import net.silentchaos512.gear.setup.SgItems;
import net.silentchaos512.gear.setup.gear.GearTypes;
import net.silentchaos512.gear.setup.gear.PartTypes;
import vazkii.patchouli.api.PatchouliAPI;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class RecipeProvider extends net.minecraft.data.recipes.RecipeProvider implements IConditionBuilder
{
    static String[] PM_METALS = new String[]{
            "minecraft:iron", "minecraft:copper", "minecraft:gold", "minecraft:netherite",
            "productivemetalwork:aluminum", "productivemetalwork:lead", "productivemetalwork:nickel", "productivemetalwork:osmium",
            "productivemetalwork:platinum", "productivemetalwork:silver", "productivemetalwork:tin", "productivemetalwork:uranium",
            "productivemetalwork:zinc", "productivemetalwork:iridium", "productivemetalwork:steel", "productivemetalwork:invar",
            "productivemetalwork:electrum", "productivemetalwork:bronze", "productivemetalwork:brass", "productivemetalwork:enderium",
            "productivemetalwork:lumium", "productivemetalwork:signalum", "productivemetalwork:refined_glowstone"
    };
    static String[] ATO_METALS = new String[]{
            "minecraft:iron", "minecraft:copper", "minecraft:gold", "minecraft:netherite",
            "alltheores:aluminum", "alltheores:lead", "alltheores:nickel", "alltheores:osmium",
            "alltheores:platinum", "alltheores:silver", "alltheores:tin", "alltheores:uranium",
            "alltheores:zinc", "alltheores:iridium", "alltheores:steel", "alltheores:invar",
            "alltheores:electrum", "alltheores:bronze", "alltheores:brass", "alltheores:enderium",
            "alltheores:lumium", "alltheores:signalum"
    };
    static String[] FTB_METALS = new String[]{
            "minecraft:iron", "minecraft:gold", "minecraft:netherite",
            "ftbmaterials:aluminum", "ftbmaterials:lead", "ftbmaterials:nickel", "ftbmaterials:osmium",
            "ftbmaterials:platinum", "ftbmaterials:silver", "ftbmaterials:tin", "ftbmaterials:uranium",
            "ftbmaterials:zinc", "ftbmaterials:iridium", "ftbmaterials:steel", "ftbmaterials:invar",
            "ftbmaterials:electrum", "ftbmaterials:bronze", "ftbmaterials:brass", "ftbmaterials:enderium",
            "ftbmaterials:lumium", "ftbmaterials:signalum", "ftbmaterials:refined_glowstone", "ftbmaterials:obsidian"
    };

    public RecipeProvider(PackOutput gen, CompletableFuture<HolderLookup.Provider> registries) {
        super(gen, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {
//        BlockCastingRecipeBuilder.of(Items.GLASS.getDefaultInstance(), SizedFluidIngredient.of(MetalworksRegistrator.MOLTEN_QUARTZ.get(),  400), BuiltInRegistries.ITEM.get(ResourceLocation.parse("enderio:fused_quartz")).getDefaultInstance()).save(recipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "test"));

        var guideBook = PatchouliAPI.get().getBookStack(ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "guide"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, guideBook)
                .pattern(" X ").pattern("XBX").pattern(" X ")
                .define('X', MetalworksRegistrator.FIRE_BRICK.get())
                .define('B', Items.BOOK)
                .unlockedBy(getHasName(MetalworksRegistrator.FIRE_BRICK.get()), has(MetalworksRegistrator.FIRE_BRICK.get()))
                .save(recipeOutput.withConditions(new ModLoadedCondition("patchouli")), ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "crafting/guide_book"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MetalworksRegistrator.FIRE_CLAY.get(), 4)
                .pattern("XOX").pattern("OXO").pattern("XOX")
                .define('X', Items.CLAY_BALL)
                .define('O', Tags.Items.SANDS)
                .unlockedBy(getHasName(Items.CLAY_BALL), has(Items.CLAY_BALL))
                .unlockedBy(getHasName(Items.SAND), has(Tags.Items.SANDS))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "crafting/fire_clay"));

        SimpleCookingRecipeBuilder.blasting(Ingredient.of(MetalworksRegistrator.FIRE_CLAY.get()), RecipeCategory.MISC, MetalworksRegistrator.FIRE_BRICK.get(), 0.4f, 200)
                .unlockedBy(getHasName(MetalworksRegistrator.FIRE_CLAY.get()), has(MetalworksRegistrator.FIRE_CLAY.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "blasting/fire_brick_blasting"));

        SimpleCookingRecipeBuilder.smelting(Ingredient.of(MetalworksRegistrator.FIRE_CLAY.get()), RecipeCategory.MISC, MetalworksRegistrator.FIRE_BRICK.get(), 0.4f, 200)
                .unlockedBy(getHasName(MetalworksRegistrator.FIRE_CLAY.get()), has(MetalworksRegistrator.FIRE_CLAY.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "blasting/fire_brick"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MetalworksRegistrator.FIRE_BRICKS.get(DyeColor.BLACK).get(), 4)
                .pattern("XX").pattern("XX")
                .define('X', MetalworksRegistrator.FIRE_BRICK.get())
                .unlockedBy(getHasName(MetalworksRegistrator.FIRE_BRICK.get()), has(MetalworksRegistrator.FIRE_BRICK.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "crafting/black_fire_bricks"));
        MetalworksRegistrator.FIRE_BRICKS.forEach((color, holder) -> {
            ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, holder.get(), 1)
                    .requires(ModTags.Items.FIRE_BRICKS)
                    .requires(color.getTag())
                    .unlockedBy("has_fire_bricks", has(ModTags.Items.FIRE_BRICKS))
                    .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "crafting/" + color.getSerializedName() + "_fire_bricks_from_dye_single"));
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, holder.get(), 8)
                    .pattern("XXX").pattern("XDX").pattern("XXX")
                    .define('X', ModTags.Items.FIRE_BRICKS)
                    .define('D', color.getTag())
                    .unlockedBy("has_fire_bricks", has(ModTags.Items.FIRE_BRICKS))
                    .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "crafting/" + color.getSerializedName() + "_fire_bricks_from_dye"));
        });

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MetalworksRegistrator.FOUNDRY_CONTROLLERS.get(DyeColor.BLACK).get(), 1)
                .pattern("XXX").pattern("XOX").pattern("XXX")
                .define('X', MetalworksRegistrator.FIRE_BRICK.get())
                .define('O', Items.BLAST_FURNACE)
                .unlockedBy(getHasName(MetalworksRegistrator.FIRE_BRICK.get()), has(MetalworksRegistrator.FIRE_BRICK.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "crafting/black_foundry_controller"));
        MetalworksRegistrator.FOUNDRY_CONTROLLERS.forEach((color, holder) -> {
            ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, holder.get(), 1)
                    .requires(ModTags.Items.FOUNDRY_CONTROLLERS)
                    .requires(color.getTag())
                    .unlockedBy("has_foundry_controller", has(ModTags.Items.FOUNDRY_CONTROLLERS))
                    .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "crafting/" + color.getSerializedName() + "_foundry_controller_from_dye"));
        });

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MetalworksRegistrator.FOUNDRY_DRAINS.get(DyeColor.BLACK).get(), 1)
                .pattern("XXX").pattern("X X").pattern("XXX")
                .define('X', MetalworksRegistrator.FIRE_BRICK.get())
                .unlockedBy(getHasName(MetalworksRegistrator.FIRE_BRICK.get()), has(MetalworksRegistrator.FIRE_BRICK.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "crafting/black_foundry_drain"));
        MetalworksRegistrator.FOUNDRY_DRAINS.forEach((color, holder) -> {
            ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, holder.get(), 1)
                    .requires(ModTags.Items.FOUNDRY_DRAINS)
                    .requires(color.getTag())
                    .unlockedBy("has_foundry_drain", has(ModTags.Items.FOUNDRY_DRAINS))
                    .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "crafting/" + color.getSerializedName() + "_foundry_drain_from_dye"));
        });

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MetalworksRegistrator.FOUNDRY_TANKS.get(DyeColor.BLACK).get(), 1)
                .pattern("XXX").pattern("XOX").pattern("XXX")
                .define('X', MetalworksRegistrator.FIRE_BRICK.get())
                .define('O', Tags.Items.GLASS_BLOCKS_COLORLESS)
                .unlockedBy(getHasName(MetalworksRegistrator.FIRE_BRICK.get()), has(MetalworksRegistrator.FIRE_BRICK.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "crafting/black_foundry_tank"));
        MetalworksRegistrator.FOUNDRY_TANKS.forEach((color, holder) -> {
            ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, holder.get(), 1)
                    .requires(ModTags.Items.FOUNDRY_TANKS)
                    .requires(color.getTag())
                    .unlockedBy("has_foundry_tank", has(ModTags.Items.FOUNDRY_TANKS))
                    .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "crafting/" + color.getSerializedName() + "_foundry_tank_from_dye"));
        });

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MetalworksRegistrator.FOUNDRY_WINDOWS.get(DyeColor.BLACK).get(), 1)
                .pattern("XOX")
                .define('X', MetalworksRegistrator.FIRE_BRICK.get())
                .define('O', Tags.Items.GLASS_BLOCKS_COLORLESS)
                .unlockedBy(getHasName(MetalworksRegistrator.FIRE_BRICK.get()), has(MetalworksRegistrator.FIRE_BRICK.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "crafting/black_foundry_window"));
        MetalworksRegistrator.FOUNDRY_WINDOWS.forEach((color, holder) -> {
            ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, holder.get(), 1)
                    .requires(ModTags.Items.FOUNDRY_WINDOWS)
                    .requires(color.getTag())
                    .unlockedBy("has_foundry_window", has(ModTags.Items.FOUNDRY_WINDOWS))
                    .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "crafting/" + color.getSerializedName() + "_foundry_window_from_dye"));
        });

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MetalworksRegistrator.FOUNDRY_TAP.get(), 1)
                .pattern("X X").pattern(" X ")
                .define('X', MetalworksRegistrator.FIRE_BRICK.get())
                .unlockedBy(getHasName(MetalworksRegistrator.FIRE_BRICK.get()), has(MetalworksRegistrator.FIRE_BRICK.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "crafting/foundry_tap"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MetalworksRegistrator.CASTING_TABLE.get(), 1)
                .pattern("XXX").pattern("X X").pattern("X X")
                .define('X', MetalworksRegistrator.FIRE_BRICK.get())
                .unlockedBy(getHasName(MetalworksRegistrator.FIRE_BRICK.get()), has(MetalworksRegistrator.FIRE_BRICK.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "crafting/casting_table"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MetalworksRegistrator.CASTING_BASIN.get(), 1)
                .pattern("X X").pattern("X X").pattern("XXX")
                .define('X', MetalworksRegistrator.FIRE_BRICK.get())
                .unlockedBy(getHasName(MetalworksRegistrator.FIRE_BRICK.get()), has(MetalworksRegistrator.FIRE_BRICK.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "crafting/casting_basin"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MetalworksRegistrator.LIQUID_HEATING_COIL.get(), 1)
                .pattern("CCC").pattern("CBC").pattern("XXX")
                .define('C', Tags.Items.INGOTS_COPPER)
                .define('B', Tags.Items.BUCKETS_EMPTY)
                .define('X', MetalworksRegistrator.FIRE_BRICK.get())
                .unlockedBy(getHasName(MetalworksRegistrator.FIRE_BRICK.get()), has(MetalworksRegistrator.FIRE_BRICK.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "crafting/liquid_heating_coil"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MetalworksRegistrator.POWERED_HEATING_COIL.get(), 1)
                .pattern("CCC").pattern("CAC").pattern("XXX")
                .define('C', Tags.Items.INGOTS_COPPER)
                .define('A', Items.AMETHYST_BLOCK)
                .define('X', MetalworksRegistrator.FIRE_BRICK.get())
                .unlockedBy(getHasName(MetalworksRegistrator.FIRE_BRICK.get()), has(MetalworksRegistrator.FIRE_BRICK.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "crafting/powered_heating_coil"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, MetalworksRegistrator.MEAT_INGOT.get(), 8)
                .requires(MetalworksRegistrator.MEAT_BLOCK.get())
                .unlockedBy(getHasName(MetalworksRegistrator.MEAT_BLOCK.get()), has(MetalworksRegistrator.MEAT_BLOCK.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "crafting/meat_ingot_from_block"));

        // Alloying
        alloyRecipe(List.of(SizedFluidIngredient.of(FluidTags.WATER, 1), SizedFluidIngredient.of(FluidTags.LAVA, 2)), 50, new FluidStack(MetalworksRegistrator.MOLTEN_OBSIDIAN.get(), 2), recipeOutput);

        // Melting
        ItemMeltingRecipeBuilder.of(Ingredient.of(Tags.Items.FOODS_RAW_MEAT), new FluidStack(MetalworksRegistrator.LIQUID_MEAT.get(), 20), 1000, 0)
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "melting/meat"));
        ItemMeltingRecipeBuilder.of(Ingredient.of(Tags.Items.GLASS_BLOCKS_CHEAP), new FluidStack(MetalworksRegistrator.MOLTEN_GLASS.get(), 1000), 1400, 0)
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "melting/glass"));
        ItemMeltingRecipeBuilder.of(Ingredient.of(Tags.Items.SANDS), new FluidStack(MetalworksRegistrator.MOLTEN_GLASS.get(), 1000), 1400, 0)
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "melting/sand"));
        ItemMeltingRecipeBuilder.of(Ingredient.of(Tags.Items.OBSIDIANS), new FluidStack(MetalworksRegistrator.MOLTEN_OBSIDIAN.get(), 1000), 1300, 0)
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "melting/obsidian_block"));
        itemMeltingRecipe(ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "dusts/obsidian")), new FluidStack(MetalworksRegistrator.MOLTEN_OBSIDIAN.get(), 250), 1400, 0, recipeOutput);
        ItemMeltingRecipeBuilder.of(Ingredient.of(Tags.Items.GEMS_DIAMOND), new FluidStack(MetalworksRegistrator.MOLTEN_DIAMOND.get(), 100))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "melting/gems/diamond"));
        ItemMeltingRecipeBuilder.of(Ingredient.of(Tags.Items.ORES_DIAMOND), new FluidStack(MetalworksRegistrator.MOLTEN_DIAMOND.get(), 200))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "melting/ores/diamond"));
        ItemMeltingRecipeBuilder.of(Ingredient.of(Tags.Items.STORAGE_BLOCKS_DIAMOND), new FluidStack(MetalworksRegistrator.MOLTEN_DIAMOND.get(), 900))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "melting/storage_blocks/diamond"));
        itemMeltingRecipe(ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "dusts/diamond")), new FluidStack(MetalworksRegistrator.MOLTEN_DIAMOND.get(), 100), recipeOutput);
        ItemMeltingRecipeBuilder.of(Ingredient.of(Tags.Items.GEMS_EMERALD), new FluidStack(MetalworksRegistrator.MOLTEN_EMERALD.get(), 100))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "melting/gems/emerald"));
        ItemMeltingRecipeBuilder.of(Ingredient.of(Tags.Items.ORES_EMERALD), new FluidStack(MetalworksRegistrator.MOLTEN_EMERALD.get(), 200))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "melting/ores/emerald"));
        ItemMeltingRecipeBuilder.of(Ingredient.of(Tags.Items.STORAGE_BLOCKS_EMERALD), new FluidStack(MetalworksRegistrator.MOLTEN_EMERALD.get(), 900))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "melting/storage_blocks/emerald"));
        ItemMeltingRecipeBuilder.of(Ingredient.of(Tags.Items.DUSTS_REDSTONE), new FluidStack(MetalworksRegistrator.MOLTEN_REDSTONE.get(), 100))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "melting/dusts/redstone"));
        ItemMeltingRecipeBuilder.of(Ingredient.of(Tags.Items.STORAGE_BLOCKS_REDSTONE), new FluidStack(MetalworksRegistrator.MOLTEN_REDSTONE.get(), 900))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "melting/storage_blocks/redstone"));
        ItemMeltingRecipeBuilder.of(Ingredient.of(Tags.Items.ORES_REDSTONE), new FluidStack(MetalworksRegistrator.MOLTEN_REDSTONE.get(), 400))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "melting/ores/redstone"));
        ItemMeltingRecipeBuilder.of(Ingredient.of(Tags.Items.GEMS_LAPIS), new FluidStack(MetalworksRegistrator.MOLTEN_LAPIS.get(), 100))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "melting/gems/lapis"));
        ItemMeltingRecipeBuilder.of(Ingredient.of(Tags.Items.STORAGE_BLOCKS_LAPIS), new FluidStack(MetalworksRegistrator.MOLTEN_LAPIS.get(), 900))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "melting/storage_blocks/lapis"));
        ItemMeltingRecipeBuilder.of(Ingredient.of(Tags.Items.ORES_LAPIS), new FluidStack(MetalworksRegistrator.MOLTEN_LAPIS.get(), 400))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "melting/ores/lapis"));
        ItemMeltingRecipeBuilder.of(Ingredient.of(Items.COAL, Items.CHARCOAL), new FluidStack(MetalworksRegistrator.MOLTEN_CARBON.get(), 100))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "melting/coals"));
        ItemMeltingRecipeBuilder.of(Ingredient.of(Tags.Items.STORAGE_BLOCKS_COAL), new FluidStack(MetalworksRegistrator.MOLTEN_CARBON.get(), 900))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "melting/storage_blocks/coals"));
        ItemMeltingRecipeBuilder.of(Ingredient.of(Tags.Items.ORES_COAL), new FluidStack(MetalworksRegistrator.MOLTEN_CARBON.get(), 200))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "melting/ores/coals"));
        ItemMeltingRecipeBuilder.of(Ingredient.of(Tags.Items.DUSTS_GLOWSTONE), new FluidStack(MetalworksRegistrator.MOLTEN_GLOWSTONE.get(), 100))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "melting/dusts/glowstone"));
        ItemMeltingRecipeBuilder.of(Ingredient.of(Items.GLOWSTONE), new FluidStack(MetalworksRegistrator.MOLTEN_GLOWSTONE.get(), 400))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "melting/storage_blocks/glowstone"));
        ItemMeltingRecipeBuilder.of(Ingredient.of(Tags.Items.ENDER_PEARLS), new FluidStack(MetalworksRegistrator.MOLTEN_ENDER.get(), 100))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "melting/ender_pearl"));
        ItemMeltingRecipeBuilder.of(Ingredient.of(Items.ENDER_EYE), new FluidStack(MetalworksRegistrator.MOLTEN_ENDER.get(), 100))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "melting/ender_eye"));
        ItemMeltingRecipeBuilder.of(Ingredient.of(Tags.Items.GEMS_AMETHYST), new FluidStack(MetalworksRegistrator.MOLTEN_AMETHYST.get(), 100))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "melting/gems/amethyst"));
        ItemMeltingRecipeBuilder.of(Ingredient.of(Items.AMETHYST_BLOCK), new FluidStack(MetalworksRegistrator.MOLTEN_AMETHYST.get(), 400))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "melting/storage_blocks/amethyst"));
        ItemMeltingRecipeBuilder.of(Ingredient.of(Tags.Items.GEMS_QUARTZ), new FluidStack(MetalworksRegistrator.MOLTEN_QUARTZ.get(), 100))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "melting/gems/quarts"));
        ItemMeltingRecipeBuilder.of(Ingredient.of(Tags.Items.ORES_QUARTZ), new FluidStack(MetalworksRegistrator.MOLTEN_QUARTZ.get(), 200))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "melting/ores/quarts"));
        ItemMeltingRecipeBuilder.of(Ingredient.of(Items.QUARTZ_BLOCK, Items.QUARTZ_BRICKS, Items.QUARTZ_PILLAR), new FluidStack(MetalworksRegistrator.MOLTEN_QUARTZ.get(), 400))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "melting/storage_blocks/quarts"));
        itemMeltingRecipe(ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "dusts/quartz")), new FluidStack(MetalworksRegistrator.MOLTEN_QUARTZ.get(), 100), recipeOutput);
        ItemMeltingRecipeBuilder.of(Ingredient.of(Items.NETHERITE_SCRAP), new FluidStack(MetalworksRegistrator.MOLTEN_ANCIENT_DEBRIS.get(), 100))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "melting/netherite_scrap"));
        ItemMeltingRecipeBuilder.of(Ingredient.of(Items.ANCIENT_DEBRIS), new FluidStack(MetalworksRegistrator.MOLTEN_ANCIENT_DEBRIS.get(), 200))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "melting/ancient_debris"));
        ItemMeltingRecipeBuilder.of(Ingredient.of(MetalworksRegistrator.MEAT_INGOT.get()), new FluidStack(MetalworksRegistrator.LIQUID_MEAT.get(), 100))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "melting/meat_ingot"));
        ItemMeltingRecipeBuilder.of(Ingredient.of(MetalworksRegistrator.SHINY_MEAT_INGOT.get()), List.of(new FluidStack(MetalworksRegistrator.LIQUID_MEAT.get(), 100), new FluidStack(MetalworksRegistrator.MOLTEN_GOLD.get(), 80)))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "melting/shiny_meat_ingot"));
        ItemMeltingRecipeBuilder.of(Ingredient.of(Items.COMPASS), List.of(new FluidStack(MetalworksRegistrator.MOLTEN_IRON.get(), 360), new FluidStack(MetalworksRegistrator.MOLTEN_REDSTONE.get(), 100)))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "melting/compass"));
        ItemMeltingRecipeBuilder.of(Ingredient.of(Items.CLOCK), List.of(new FluidStack(MetalworksRegistrator.MOLTEN_GOLD.get(), 360), new FluidStack(MetalworksRegistrator.MOLTEN_REDSTONE.get(), 100)))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "melting/clock"));
        ItemMeltingRecipeBuilder.of(Ingredient.of(Items.DIAMOND_HORSE_ARMOR), List.of(new FluidStack(MetalworksRegistrator.MOLTEN_DIAMOND.get(), 600)))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "melting/diamond_horse_armor"));
        ItemMeltingRecipeBuilder.of(Ingredient.of(Items.GOLDEN_HORSE_ARMOR), List.of(new FluidStack(MetalworksRegistrator.MOLTEN_GOLD.get(), 540)))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "melting/golden_horse_armor"));
        ItemMeltingRecipeBuilder.of(Ingredient.of(Items.IRON_HORSE_ARMOR), List.of(new FluidStack(MetalworksRegistrator.MOLTEN_IRON.get(), 540)))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "melting/iron_horse_armor"));

        // Casting
        BlockCastingRecipeBuilder.of(SizedFluidIngredient.of(MetalworksRegistrator.MOLTEN_GLASS.get(), 1000), Items.GLASS.getDefaultInstance())
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting/misc/glass_block"));
        BlockCastingRecipeBuilder.of(SizedFluidIngredient.of(MetalworksRegistrator.MOLTEN_OBSIDIAN.get(), 1000), Items.OBSIDIAN.getDefaultInstance())
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting/misc/obsidian_block"));
        ItemCastingRecipeBuilder.of(MetalworksRegistrator.CAST_GEM.get().getDefaultInstance(), SizedFluidIngredient.of(MetalworksRegistrator.MOLTEN_DIAMOND.get(), 100), Items.DIAMOND.getDefaultInstance())
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting/gems/diamond"));
        BlockCastingRecipeBuilder.of(SizedFluidIngredient.of(MetalworksRegistrator.MOLTEN_DIAMOND.get(), 900), Items.DIAMOND_BLOCK.getDefaultInstance())
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting/storage_blocks/diamond"));
        ItemCastingRecipeBuilder.of(MetalworksRegistrator.CAST_GEM.get().getDefaultInstance(), SizedFluidIngredient.of(MetalworksRegistrator.MOLTEN_EMERALD.get(), 100), Items.EMERALD.getDefaultInstance())
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting/gems/emerald"));
        BlockCastingRecipeBuilder.of(SizedFluidIngredient.of(MetalworksRegistrator.MOLTEN_EMERALD.get(), 900), Items.EMERALD_BLOCK.getDefaultInstance())
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting/storage_blocks/emerald"));
        BlockCastingRecipeBuilder.of(SizedFluidIngredient.of(MetalworksRegistrator.MOLTEN_REDSTONE.get(), 900), Items.REDSTONE_BLOCK.getDefaultInstance())
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting/storage_blocks/redstone"));
        ItemCastingRecipeBuilder.of(MetalworksRegistrator.CAST_GEM.get().getDefaultInstance(), SizedFluidIngredient.of(MetalworksRegistrator.MOLTEN_LAPIS.get(), 100), Items.LAPIS_LAZULI.getDefaultInstance())
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting/gems/lapis"));
        BlockCastingRecipeBuilder.of(SizedFluidIngredient.of(MetalworksRegistrator.MOLTEN_LAPIS.get(), 900), Items.LAPIS_BLOCK.getDefaultInstance())
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting/storage_blocks/lapis"));
        BlockCastingRecipeBuilder.of(SizedFluidIngredient.of(MetalworksRegistrator.MOLTEN_GLOWSTONE.get(), 400), Items.GLOWSTONE.getDefaultInstance())
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting/storage_blocks/glowstone"));
        ItemCastingRecipeBuilder.of(MetalworksRegistrator.CAST_GEM.get().getDefaultInstance(), SizedFluidIngredient.of(MetalworksRegistrator.MOLTEN_QUARTZ.get(), 100), Items.QUARTZ.getDefaultInstance())
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting/gems/quartz"));
        ItemCastingRecipeBuilder.of(MetalworksRegistrator.CAST_GEM.get().getDefaultInstance(), SizedFluidIngredient.of(MetalworksRegistrator.MOLTEN_LAPIS.get(), 100), Items.AMETHYST_SHARD.getDefaultInstance())
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting/gems/amethyst"));
        ItemCastingRecipeBuilder.of(MetalworksRegistrator.CAST_INGOT.get().getDefaultInstance(), SizedFluidIngredient.of(MetalworksRegistrator.MOLTEN_ANCIENT_DEBRIS.get(), 100), Items.NETHERITE_SCRAP.getDefaultInstance())
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting/netherite_scrap"));

        // Casts
        ItemCastingRecipeBuilder.of(Tags.Items.INGOTS, SizedFluidIngredient.of(ModTags.Fluids.MOLTEN_STEEL, 360), MetalworksRegistrator.CAST_INGOT.get().getDefaultInstance(), true)
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting/cast/ingot"));
        ItemCastingRecipeBuilder.of(Tags.Items.NUGGETS, SizedFluidIngredient.of(ModTags.Fluids.MOLTEN_STEEL, 360), MetalworksRegistrator.CAST_NUGGET.get().getDefaultInstance(), true)
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting/cast/nugget"));
        ItemCastingRecipeBuilder.of(Tags.Items.GEMS, SizedFluidIngredient.of(ModTags.Fluids.MOLTEN_STEEL, 360), MetalworksRegistrator.CAST_GEM.get().getDefaultInstance(), true)
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting/cast/gem"));
        ItemCastingRecipeBuilder.of(ModTags.Items.GEARS, SizedFluidIngredient.of(ModTags.Fluids.MOLTEN_STEEL, 360), MetalworksRegistrator.CAST_GEAR.get().getDefaultInstance(), true)
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting/cast/gear"));
        ItemCastingRecipeBuilder.of(ModTags.Items.RODS, SizedFluidIngredient.of(ModTags.Fluids.MOLTEN_STEEL, 360), MetalworksRegistrator.CAST_ROD.get().getDefaultInstance(), true)
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting/cast/rod"));
        ItemCastingRecipeBuilder.of(ModTags.Items.PLATES, SizedFluidIngredient.of(ModTags.Fluids.MOLTEN_STEEL, 360), MetalworksRegistrator.CAST_PLATE.get().getDefaultInstance(), true)
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting/cast/plates"));


        // Misc items
        BlockCastingRecipeBuilder.of(SizedFluidIngredient.of(ModTags.Fluids.HONEY, 1000), Items.HONEY_BLOCK.getDefaultInstance())
                .save(recipeOutput.withConditions(new NotCondition(new FluidTagEmptyCondition(ModTags.Fluids.HONEY))), ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting/honey_block"));
        ItemCastingRecipeBuilder.of(Items.GLASS_BOTTLE.getDefaultInstance(), SizedFluidIngredient.of(ModTags.Fluids.HONEY, 250), Items.HONEY_BOTTLE.getDefaultInstance())
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting/misc/honey_bottle"));
        ItemCastingRecipeBuilder.of(Items.CARROT.getDefaultInstance(), SizedFluidIngredient.of(ModTags.Fluids.MOLTEN_GOLD, 80), Items.GOLDEN_CARROT.getDefaultInstance(), true)
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting/misc/golden_carrot"));
        ItemCastingRecipeBuilder.of(Items.MELON_SLICE.getDefaultInstance(), SizedFluidIngredient.of(ModTags.Fluids.MOLTEN_GOLD, 80), Items.GLISTERING_MELON_SLICE.getDefaultInstance(), true)
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting/misc/glistering_melon"));
        ItemCastingRecipeBuilder.of(Items.APPLE.getDefaultInstance(), SizedFluidIngredient.of(ModTags.Fluids.MOLTEN_GOLD, 720), Items.GOLDEN_APPLE.getDefaultInstance(), true)
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting/misc/golden_apple"));
        ItemCastingRecipeBuilder.of(Items.REDSTONE.getDefaultInstance(), SizedFluidIngredient.of(ModTags.Fluids.MOLTEN_GOLD, 360), Items.CLOCK.getDefaultInstance(), true)
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting/misc/clock"));
        ItemCastingRecipeBuilder.of(Items.REDSTONE.getDefaultInstance(), SizedFluidIngredient.of(ModTags.Fluids.MOLTEN_IRON, 360), Items.COMPASS.getDefaultInstance(), true)
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting/misc/compass"));
        ItemCastingRecipeBuilder.of(Items.TORCH.getDefaultInstance(), SizedFluidIngredient.of(ModTags.Fluids.MOLTEN_IRON, 80), Items.LANTERN.getDefaultInstance(), true)
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting/misc/lantern"));
        ItemCastingRecipeBuilder.of(Items.SOUL_TORCH.getDefaultInstance(), SizedFluidIngredient.of(ModTags.Fluids.MOLTEN_IRON, 80), Items.SOUL_LANTERN.getDefaultInstance(), true)
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting/misc/soul_lantern"));
        ItemCastingRecipeBuilder.of(MetalworksRegistrator.CAST_INGOT.get().getDefaultInstance(), SizedFluidIngredient.of(ModTags.Fluids.MEAT, 100), MetalworksRegistrator.MEAT_INGOT.get().getDefaultInstance(), false)
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting/misc/meat_ingot"));
        ItemCastingRecipeBuilder.of(MetalworksRegistrator.MEAT_INGOT.get().getDefaultInstance(), SizedFluidIngredient.of(ModTags.Fluids.MOLTEN_GOLD, 80), MetalworksRegistrator.SHINY_MEAT_INGOT.get().getDefaultInstance(), true)
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting/misc/shiny_meat_ingot"));
        BlockCastingRecipeBuilder.of(Items.BONE.getDefaultInstance(), SizedFluidIngredient.of(ModTags.Fluids.MEAT, 800), MetalworksRegistrator.MEAT_BLOCK.get().asItem().getDefaultInstance(), true)
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting/misc/meat_block"));


        // Bee stuff
        ItemMeltingRecipeBuilder.of(Ingredient.of(Items.HONEYCOMB), List.of(new FluidStack(BuiltInRegistries.FLUID.get(ResourceLocation.fromNamespaceAndPath("productivebees", "honey")), 100), new FluidStack(MetalworksRegistrator.MOLTEN_WAX.get(), 50)))
                .save(recipeOutput.withConditions(new ModLoadedCondition("productivebees")), ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "melting/honeycomb"));
        ItemMeltingRecipeBuilder.of(Ingredient.of(Items.HONEYCOMB_BLOCK), List.of(new FluidStack(BuiltInRegistries.FLUID.get(ResourceLocation.fromNamespaceAndPath("productivebees", "honey")), 400), new FluidStack(MetalworksRegistrator.MOLTEN_WAX.get(), 200)))
                .save(recipeOutput.withConditions(new ModLoadedCondition("productivebees")), ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "melting/honeycomb_block"));
        ItemMeltingRecipeBuilder.of(Ingredient.of(Items.HONEY_BLOCK), new FluidStack(BuiltInRegistries.FLUID.get(ResourceLocation.fromNamespaceAndPath("productivebees", "honey")), 1000))
                .save(recipeOutput.withConditions(new ModLoadedCondition("productivebees")), ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "melting/honey_block"));
        ItemMeltingRecipeBuilder.of(Ingredient.of(ModTags.Items.WAXES), List.of(new FluidStack(MetalworksRegistrator.MOLTEN_WAX.get(), 50)))
                .save(recipeOutput.withConditions(new NotCondition(new TagEmptyCondition(ModTags.Items.WAXES))), ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "melting/waxes"));
        ItemMeltingRecipeBuilder.of(Ingredient.of(ModTags.Items.STORAGE_BLOCK_WAXES), List.of(new FluidStack(MetalworksRegistrator.MOLTEN_WAX.get(), 450)))
                .save(recipeOutput.withConditions(new NotCondition(new TagEmptyCondition(ModTags.Items.STORAGE_BLOCK_WAXES))), ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "melting/wax_blocks"));
        // TODO full PB compat as a way to process combs

        // Alloying

        // Netherite
        alloyRecipe(List.of(SizedFluidIngredient.of(ModTags.Fluids.MOLTEN_ANCIENT_DEBRIS, 4), SizedFluidIngredient.of(ModTags.Fluids.MOLTEN_GOLD, 4)), 10, new FluidStack(MetalworksRegistrator.MOLTEN_NETHERITE, 1), recipeOutput);
        // Steel
        alloyRecipe(List.of(SizedFluidIngredient.of(ModTags.Fluids.MOLTEN_CARBON, 10), SizedFluidIngredient.of(ModTags.Fluids.MOLTEN_IRON, 9)), 4, new FluidStack(MetalworksRegistrator.MOLTEN_STEEL, 9), recipeOutput);
        // Electrum
        alloyRecipe(List.of(SizedFluidIngredient.of(ModTags.Fluids.MOLTEN_SILVER, 1), SizedFluidIngredient.of(ModTags.Fluids.MOLTEN_GOLD, 1)), 10, new FluidStack(MetalworksRegistrator.MOLTEN_ELECTRUM, 2), recipeOutput);
        // Bronze
        alloyRecipe(List.of(SizedFluidIngredient.of(ModTags.Fluids.MOLTEN_COPPER, 3), SizedFluidIngredient.of(ModTags.Fluids.MOLTEN_TIN, 1)), 10, new FluidStack(MetalworksRegistrator.MOLTEN_BRONZE, 4), recipeOutput);
        // Brass
        alloyRecipe(List.of(SizedFluidIngredient.of(ModTags.Fluids.MOLTEN_COPPER, 3), SizedFluidIngredient.of(ModTags.Fluids.MOLTEN_ZINC, 1)), 10, new FluidStack(MetalworksRegistrator.MOLTEN_BRASS, 4), recipeOutput);
        // Invar
        alloyRecipe(List.of(SizedFluidIngredient.of(ModTags.Fluids.MOLTEN_IRON, 2), SizedFluidIngredient.of(ModTags.Fluids.MOLTEN_NICKEL, 1)), 10, new FluidStack(MetalworksRegistrator.MOLTEN_INVAR, 3), recipeOutput);
        // Constantan
        alloyRecipe(List.of(SizedFluidIngredient.of(ModTags.Fluids.MOLTEN_COPPER, 1), SizedFluidIngredient.of(ModTags.Fluids.MOLTEN_NICKEL, 1)), 10, new FluidStack(MetalworksRegistrator.MOLTEN_CONSTANTAN, 2), recipeOutput);
        // Lumium
        alloyRecipe(List.of(SizedFluidIngredient.of(ModTags.Fluids.MOLTEN_REDSTONE, 40), SizedFluidIngredient.of(ModTags.Fluids.MOLTEN_SILVER, 9), SizedFluidIngredient.of(ModTags.Fluids.MOLTEN_COPPER, 27)), 1, new FluidStack(MetalworksRegistrator.MOLTEN_LUMIUM, 36), recipeOutput);
        // Signalum
        alloyRecipe(List.of(SizedFluidIngredient.of(ModTags.Fluids.MOLTEN_GLOWSTONE, 40), SizedFluidIngredient.of(ModTags.Fluids.MOLTEN_SILVER, 9), SizedFluidIngredient.of(ModTags.Fluids.MOLTEN_TIN, 27)), 1, new FluidStack(MetalworksRegistrator.MOLTEN_SIGNALUM, 36), recipeOutput);
        // Enderium
        alloyRecipe(List.of(SizedFluidIngredient.of(ModTags.Fluids.MOLTEN_LEAD, 27), SizedFluidIngredient.of(ModTags.Fluids.MOLTEN_PLATINUM, 9), SizedFluidIngredient.of(ModTags.Fluids.MOLTEN_ENDER, 20)), 1, new FluidStack(MetalworksRegistrator.MOLTEN_ENDERIUM, 36), recipeOutput);
        // Refined glowstone
        alloyRecipe(List.of(SizedFluidIngredient.of(ModTags.Fluids.MOLTEN_OSMIUM, 9), SizedFluidIngredient.of(ModTags.Fluids.MOLTEN_GLOWSTONE, 5)), 10, new FluidStack(MetalworksRegistrator.MOLTEN_REFINED_GLOWSTONE, 9), recipeOutput);

        metalCompat(PM_METALS, recipeOutput);
        modMetalCompat("alltheores", ATO_METALS, recipeOutput);
        modMetalCompat("ftbmaterials", FTB_METALS, recipeOutput);

        // TODO enderio alloy casting recipes
        atmCompat(recipeOutput);
        idCompat(recipeOutput);
        pncCompat(recipeOutput);
        maCompat(recipeOutput);
        sgCompat(recipeOutput);
    }

    // Vanilla stuff and tags, whatever is not mod specific
    private void metalCompat(String[] metals, RecipeOutput recipeOutput) {
        for (String id: metals) {
            var rLoc = ResourceLocation.parse(id);
            String name = rLoc.getPath();

            var fluidTag = FluidTags.create(ResourceLocation.fromNamespaceAndPath("c", "molten_" + name));
            var blockTag = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "storage_blocks/" + name));
            var oreTag = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "ores/" + name));
            var rawBlockTag = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "storage_blocks/raw_" + name));
            var rawItemTag = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "raw_materials/" + name));
            var ingotTag = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "ingots/" + name));
            var nuggetTag = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "nuggets/" + name));
            var dustTag = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "dusts/" + name));
            var gearTag = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "gears/" + name));
            var rodTag = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "rods/" + name));
            var plateTag = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "plates/" + name));

            var fluid = BuiltInRegistries.FLUID.get(ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "molten_" + name));
            var blockItem = BuiltInRegistries.ITEM.get(rLoc.withPath(p -> p + "_block"));
            var ingotItem = BuiltInRegistries.ITEM.get(rLoc.withPath(p -> p + "_ingot"));
            var nuggetItem = BuiltInRegistries.ITEM.get(rLoc.withPath(p -> p + "_nugget"));

            // Cast vanilla stuff
            if (rLoc.getNamespace().equals("minecraft")) {
                // Cast block
                BlockCastingRecipeBuilder.of(SizedFluidIngredient.of(fluidTag, 810), blockItem.getDefaultInstance())
                        .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting/storage_blocks/" + name));
                // Cast ingot
                ItemCastingRecipeBuilder.of(MetalworksRegistrator.CAST_INGOT.get().getDefaultInstance(), SizedFluidIngredient.of(fluidTag, 90), ingotItem.getDefaultInstance())
                        .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting/ingots/" + name));
                if (!nuggetItem.equals(Items.AIR)) {
                    // Cast nugget
                    ItemCastingRecipeBuilder.of(MetalworksRegistrator.CAST_NUGGET.get().getDefaultInstance(), SizedFluidIngredient.of(fluidTag, 10), nuggetItem.getDefaultInstance())
                            .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting/nuggets/" + name));
                }
            }

            if (fluid.equals(Fluids.EMPTY)) {
                ProductiveMetalworks.LOGGER.info("empty fluid " + name);
            }

            // Melt block
            itemMeltingRecipe(blockTag, new FluidStack(fluid, 810), recipeOutput);
            // Melt raw ore block
            itemMeltingRecipe(rawBlockTag, new FluidStack(fluid, 1080), recipeOutput);
            // Melt ore
            itemMeltingRecipe(oreTag, new FluidStack(fluid, 240), recipeOutput);
            // Melt ingot
            itemMeltingRecipe(ingotTag, new FluidStack(fluid, 90), recipeOutput);
            // Melt nugget
            itemMeltingRecipe(nuggetTag, new FluidStack(fluid, 10), recipeOutput);
            // Melt raw ore
            itemMeltingRecipe(rawItemTag, new FluidStack(fluid, 120), recipeOutput);
            // Melt dust
            itemMeltingRecipe(dustTag, new FluidStack(fluid, 90), recipeOutput);
            // Melt Gear
            itemMeltingRecipe(gearTag, new FluidStack(fluid, 360), recipeOutput);
            // Melt Rod
            itemMeltingRecipe(rodTag, new FluidStack(fluid, 180), recipeOutput);
            // Melt Plate
            itemMeltingRecipe(plateTag, new FluidStack(fluid, 180), recipeOutput);
        }
    }

    // Mod specific compat recipes
    private void modMetalCompat(String modId, String[] metals, RecipeOutput recipeOutput) {
        var compatRecipeOutput = recipeOutput.withConditions(new ModLoadedCondition(modId));
        
        // vanilla gear, rod, plate melting and casting
        for (String id: metals) {
            var rLoc = ResourceLocation.parse(id);
            String name = rLoc.getPath();
            var fluidTag = FluidTags.create(ResourceLocation.fromNamespaceAndPath("c", "molten_" + name));

            var blockItem = BuiltInRegistries.ITEM.get(rLoc.withPath(p -> p + "_block"));
            var ingotItem = BuiltInRegistries.ITEM.get(rLoc.withPath(p -> p + "_ingot"));
            var nuggetItem = BuiltInRegistries.ITEM.get(rLoc.withPath(p -> p + "_nugget"));
            var gearItem = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(modId, name + "_gear"));
            var rodItem = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(modId, name + "_rod"));
            var plateItem = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(modId, name + "_plate"));

            // Melting recipes

            // Casting recipes
            if (rLoc.getNamespace().equals(modId)) {
                if (!blockItem.equals(Items.AIR)) {
                    // Cast block
                    BlockCastingRecipeBuilder.of(SizedFluidIngredient.of(fluidTag, 810), blockItem.getDefaultInstance())
                            .save(compatRecipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting/storage_blocks/" + modId + "/" + name));
                }
                if (!ingotItem.equals(Items.AIR)) {
                    // Cast ingot
                    ItemCastingRecipeBuilder.of(MetalworksRegistrator.CAST_INGOT.get().getDefaultInstance(), SizedFluidIngredient.of(fluidTag, 90), ingotItem.getDefaultInstance())
                            .save(compatRecipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting/ingots/" + modId + "/" + name));
                }
                if (!nuggetItem.equals(Items.AIR)) {
                    // Cast nugget
                    ItemCastingRecipeBuilder.of(MetalworksRegistrator.CAST_NUGGET.get().getDefaultInstance(), SizedFluidIngredient.of(fluidTag, 10), nuggetItem.getDefaultInstance())
                            .save(compatRecipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting/nuggets/" + modId + "/"  + name));
                }
            }
            if (!gearItem.equals(Items.AIR)) {
                // Cast gear
                ItemCastingRecipeBuilder.of(MetalworksRegistrator.CAST_GEAR.get().getDefaultInstance(), SizedFluidIngredient.of(fluidTag, 360), gearItem.getDefaultInstance())
                        .save(compatRecipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting/gears/" + modId + "/" + name));
            }
            if (!rodItem.equals(Items.AIR)) {
                // Cast rod
                ItemCastingRecipeBuilder.of(MetalworksRegistrator.CAST_ROD.get().getDefaultInstance(), SizedFluidIngredient.of(fluidTag, 180), rodItem.getDefaultInstance())
                        .save(compatRecipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting/rods/" + modId + "/" + name));
            }
            if (!plateItem.equals(Items.AIR)) {
                // Cast Plate
                ItemCastingRecipeBuilder.of(MetalworksRegistrator.CAST_PLATE.get().getDefaultInstance(), SizedFluidIngredient.of(fluidTag, 180), plateItem.getDefaultInstance())
                        .save(compatRecipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting/plates/" + modId + "/" + name));
            }
        }
    }

    private void atmCompat(RecipeOutput recipeOutput) {
        for (String resource: new String[] {"allthemodium", "vibranium", "unobtainium"}) {
            var fluid = BuiltInRegistries.FLUID.get(ResourceLocation.fromNamespaceAndPath("allthemodium", "molten_" + resource));

            var blockTag = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "storage_blocks/" + resource));
            var oreTag = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "ores/" + resource));
            var rawBlockTag = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "storage_blocks/raw_" + resource));
            var rawItemTag = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "raw_materials/" + resource));
            var ingotTag = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "ingots/" + resource));
            var nuggetTag = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "nuggets/" + resource));
            var dustTag = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "dusts/" + resource));
            var gearTag = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "gears/" + resource));
            var rodTag = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "rods/" + resource));
            var plateTag = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "plates/" + resource));

            var blockItem = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("allthemodium", resource + "_block"));
            var ingotItem = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("allthemodium", resource + "_ingot"));
            var nuggetItem = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("allthemodium", resource + "_nugget"));
            var gearItem = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("allthemodium", resource + "_gear"));
            var rodItem = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("allthemodium", resource + "_rod"));
            var plateItem = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("allthemodium", resource + "_plate"));

            // Melt block
            itemMeltingRecipe(blockTag, new FluidStack(fluid, 810), 3000, 0, recipeOutput);
            // Melt raw ore block
            itemMeltingRecipe(rawBlockTag, new FluidStack(fluid, 1080), 3000, 0, recipeOutput);
            // Melt ore
            itemMeltingRecipe(oreTag, new FluidStack(fluid, 240), 3000, 0, recipeOutput);
            // Melt ingot
            itemMeltingRecipe(ingotTag, new FluidStack(fluid, 90), 3000, 0, recipeOutput);
            // Melt nugget
            itemMeltingRecipe(nuggetTag, new FluidStack(fluid, 10), 3000, 0, recipeOutput);
            // Melt raw ore
            itemMeltingRecipe(rawItemTag, new FluidStack(fluid, 120), 3000, 0, recipeOutput);
            // Melt dust
            itemMeltingRecipe(dustTag, new FluidStack(fluid, 90), 3000, 0, recipeOutput);
            // Melt Gear
            itemMeltingRecipe(gearTag, new FluidStack(fluid, 720), 3000, 0, recipeOutput);
            // Melt Rod
            itemMeltingRecipe(rodTag, new FluidStack(fluid, 270), 3000, 0, recipeOutput);
            // Melt Plate
            itemMeltingRecipe(plateTag, new FluidStack(fluid, 360), 3000, 0, recipeOutput);

            // Cast block
            BlockCastingRecipeBuilder.of(SizedFluidIngredient.of(fluid, 810), blockItem.getDefaultInstance())
                    .save(recipeOutput.withConditions(new ModLoadedCondition("allthemodium")), ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting/atm/" + resource + "_block"));
            // Cast ingot
            ItemCastingRecipeBuilder.of(MetalworksRegistrator.CAST_INGOT.get().getDefaultInstance(), SizedFluidIngredient.of(fluid, 90), ingotItem.getDefaultInstance())
                    .save(recipeOutput.withConditions(new ModLoadedCondition("allthemodium")), ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting/atm/" + resource + "_ingot"));
            // Cast nugget
            ItemCastingRecipeBuilder.of(MetalworksRegistrator.CAST_NUGGET.get().getDefaultInstance(), SizedFluidIngredient.of(fluid, 10), nuggetItem.getDefaultInstance())
                    .save(recipeOutput.withConditions(new ModLoadedCondition("allthemodium")), ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting/atm/" + resource + "_nugget"));
            // Cast gear
            ItemCastingRecipeBuilder.of(MetalworksRegistrator.CAST_GEAR.get().getDefaultInstance(), SizedFluidIngredient.of(fluid, 720), gearItem.getDefaultInstance())
                    .save(recipeOutput.withConditions(new ModLoadedCondition("allthemodium")), ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting/atm/" + resource + "_gear"));
            // Cast rod
            ItemCastingRecipeBuilder.of(MetalworksRegistrator.CAST_ROD.get().getDefaultInstance(), SizedFluidIngredient.of(fluid, 270), rodItem.getDefaultInstance())
                    .save(recipeOutput.withConditions(new ModLoadedCondition("allthemodium")), ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting/atm/" + resource + "_rod"));
            // Cast Plate
            ItemCastingRecipeBuilder.of(MetalworksRegistrator.CAST_PLATE.get().getDefaultInstance(), SizedFluidIngredient.of(fluid, 360), plateItem.getDefaultInstance())
                    .save(recipeOutput.withConditions(new ModLoadedCondition("allthemodium")), ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting/atm/" + resource + "_plate"));
        }

        var atmFluid = BuiltInRegistries.FLUID.get(ResourceLocation.fromNamespaceAndPath("allthemodium", "molten_allthemodium"));

        // atm carrot and apple
        ItemCastingRecipeBuilder.of(Items.APPLE.getDefaultInstance(), SizedFluidIngredient.of(atmFluid, 80), BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("allthemodium", "allthemodium_apple")).getDefaultInstance())
                .save(recipeOutput.withConditions(new ModLoadedCondition("allthemodium")), ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting/atm/apple"));
        ItemCastingRecipeBuilder.of(Items.CARROT.getDefaultInstance(), SizedFluidIngredient.of(atmFluid, 80), BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("allthemodium", "allthemodium_carrot")).getDefaultInstance())
                .save(recipeOutput.withConditions(new ModLoadedCondition("allthemodium")), ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting/atm/carrot"));

    }

    private void idCompat(RecipeOutput recipeOutput) {
        var menrilBlock = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("integrateddynamics", "crystalized_menril_block"));
        var menrilChunk = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("integrateddynamics", "crystalized_menril_chunk"));
        var menrilResin = BuiltInRegistries.FLUID.get(ResourceLocation.fromNamespaceAndPath("integrateddynamics", "menril_resin"));
        // menril block
        ItemMeltingRecipeBuilder.of(Ingredient.of(menrilBlock), new FluidStack(menrilResin, 1000))
                .save(recipeOutput.withConditions(new ModLoadedCondition("integrateddynamics")), ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "melting/id/crystalized_menril_block"));
        // menril chunk
        ItemMeltingRecipeBuilder.of(Ingredient.of(menrilChunk), new FluidStack(menrilResin, 100))
                .save(recipeOutput.withConditions(new ModLoadedCondition("integrateddynamics")), ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "melting/id/crystalized_menril_chunk"));
        // menril logs
        ItemMeltingRecipeBuilder.of(Ingredient.of(ItemTags.create(ResourceLocation.fromNamespaceAndPath("integrateddynamics", "menril_logs"))), new FluidStack(menrilResin, 1300))
                .save(recipeOutput.withConditions(new ModLoadedCondition("integrateddynamics")), ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "melting/id/menril_logs"));
        // menril planks
        ItemMeltingRecipeBuilder.of(Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("integrateddynamics", "menril_planks"))), new FluidStack(menrilResin, 350))
                .save(recipeOutput.withConditions(new ModLoadedCondition("integrateddynamics")), ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "melting/id/menril_planks"));

        // menril block
        BlockCastingRecipeBuilder.of(SizedFluidIngredient.of(menrilResin, 1000), menrilBlock.getDefaultInstance())
                .save(recipeOutput.withConditions(new ModLoadedCondition("integrateddynamics")), ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting/id/crystalized_menril_block"));
        // menril chunk
        ItemCastingRecipeBuilder.of(MetalworksRegistrator.CAST_NUGGET.get().getDefaultInstance(), SizedFluidIngredient.of(menrilResin, 1000), menrilChunk.getDefaultInstance())
                .save(recipeOutput.withConditions(new ModLoadedCondition("integrateddynamics")), ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting/id/crystalized_menril_chunk"));

        var chorusBlock = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("integrateddynamics", "crystalized_chorus_block"));
        var chorusChunk = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("integrateddynamics", "crystalized_chorus_chunk"));
        var chorusResin = BuiltInRegistries.FLUID.get(ResourceLocation.fromNamespaceAndPath("integrateddynamics", "liquid_chorus"));
        // chorus block
        ItemMeltingRecipeBuilder.of(Ingredient.of(chorusBlock), new FluidStack(chorusResin, 1000))
                .save(recipeOutput.withConditions(new ModLoadedCondition("integrateddynamics")), ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "melting/id/crystalized_chorus_block"));
        // chorus chunk
        ItemMeltingRecipeBuilder.of(Ingredient.of(chorusChunk), new FluidStack(chorusResin, 100))
                .save(recipeOutput.withConditions(new ModLoadedCondition("integrateddynamics")), ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "melting/id/crystalized_chorus_chunk"));
        // proto chorus
        ItemMeltingRecipeBuilder.of(Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("integrateddynamics", "proto_chorus"))), new FluidStack(chorusResin, 250))
                .save(recipeOutput.withConditions(new ModLoadedCondition("integrateddynamics")), ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "melting/id/proto_chorus"));
        // popped chorus
        ItemMeltingRecipeBuilder.of(Ingredient.of(Items.POPPED_CHORUS_FRUIT), new FluidStack(chorusResin, 450))
                .save(recipeOutput.withConditions(new ModLoadedCondition("integrateddynamics")), ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "melting/id/popped_chorus"));

        // chorus block
        BlockCastingRecipeBuilder.of(SizedFluidIngredient.of(chorusResin, 1000), chorusBlock.getDefaultInstance())
                .save(recipeOutput.withConditions(new ModLoadedCondition("integrateddynamics")), ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting/id/crystalized_chorus_block"));
        // chorus chunk
        ItemCastingRecipeBuilder.of(MetalworksRegistrator.CAST_NUGGET.get().getDefaultInstance(), SizedFluidIngredient.of(chorusResin, 1000), chorusChunk.getDefaultInstance())
                .save(recipeOutput.withConditions(new ModLoadedCondition("integrateddynamics")), ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting/id/crystalized_chorus_chunk"));
    }

    private void pncCompat(RecipeOutput recipeOutput) {
        var plasticItem = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("pneumaticcraft", "plastic"));
        var moltenPlastic = BuiltInRegistries.FLUID.get(ResourceLocation.fromNamespaceAndPath("pneumaticcraft", "plastic"));
        // molten plastic
        ItemMeltingRecipeBuilder.of(Ingredient.of(plasticItem), new FluidStack(moltenPlastic, 1000))
                .save(recipeOutput.withConditions(new ModLoadedCondition("pneumaticcraft")), ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "melting/pnc/plastic"));

        // plastic
        ItemCastingRecipeBuilder.of(MetalworksRegistrator.CAST_INGOT.get().getDefaultInstance(), SizedFluidIngredient.of(moltenPlastic, 1000), plasticItem.getDefaultInstance())
                .save(recipeOutput.withConditions(new ModLoadedCondition("pneumaticcraft")), ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting/pnc/plastic"));

        // plastic alloying
        FluidAlloyingRecipeBuilder.of(List.of(SizedFluidIngredient.of(BuiltInRegistries.FLUID.get(ResourceLocation.fromNamespaceAndPath("pneumaticcraft", "biodiesel")), 100), SizedFluidIngredient.of(ModTags.Fluids.MOLTEN_CARBON, 100)), new FluidStack(moltenPlastic, 1000))
                .save(recipeOutput.withConditions(new ModLoadedCondition("pneumaticcraft")), ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "alloying/pnc/plastic_from_biodiesel"));

        FluidAlloyingRecipeBuilder.of(List.of(SizedFluidIngredient.of(BuiltInRegistries.FLUID.get(ResourceLocation.fromNamespaceAndPath("pneumaticcraft", "lpg")), 100), SizedFluidIngredient.of(ModTags.Fluids.MOLTEN_CARBON, 100)), new FluidStack(moltenPlastic, 1000))
                .save(recipeOutput.withConditions(new ModLoadedCondition("pneumaticcraft")), ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "alloying/pnc/plastic_from_lpg"));
    }

    private void maCompat(RecipeOutput recipeOutput) {
        for (String essence: new String[]{"inferium", "prudentium", "tertium", "imperium", "supremium"}) {
            var essenceFluid = BuiltInRegistries.FLUID.get(ResourceLocation.fromNamespaceAndPath("mysticalagradditions", "molten_" + essence));
            ProductiveMetalworks.LOGGER.info("essence fluid for " + essence + " " + essenceFluid);
            // melt block
            ItemMeltingRecipeBuilder.of(
                    Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("mysticalagriculture", essence + "_block"))),
                    new FluidStack(essenceFluid, 810)
            ).save(recipeOutput.withConditions(new ModLoadedCondition("mysticalagradditions")), ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "melting/ma/" + essence + "_block"));
            // melt essence
            ItemMeltingRecipeBuilder.of(
                    Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("mysticalagriculture", essence + "_essence"))),
                    new FluidStack(essenceFluid, 90)
            ).save(recipeOutput.withConditions(new ModLoadedCondition("mysticalagradditions")), ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "melting/ma/" + essence + "_essence"));

            // casting essence
            ItemCastingRecipeBuilder.of(SizedFluidIngredient.of(essenceFluid, 90), BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("mysticalagriculture", essence + "_essence")).getDefaultInstance())
                    .save(recipeOutput.withConditions(new ModLoadedCondition("mysticalagradditions")), ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting/ma/" + essence + "_essence"));
            // casting ingot
            ItemCastingRecipeBuilder.of(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("mysticalagriculture", "prosperity_ingot")).getDefaultInstance(), SizedFluidIngredient.of(essenceFluid, 180), BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("mysticalagriculture", essence + "_ingot")).getDefaultInstance())
                    .save(recipeOutput.withConditions(new ModLoadedCondition("mysticalagradditions")), ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting/ma/" + essence + "_ingot"));
            // casting apple
            ItemCastingRecipeBuilder.of(Items.GOLDEN_APPLE.getDefaultInstance(), SizedFluidIngredient.of(essenceFluid, 720), BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("mysticalagradditions", essence + "_apple")).getDefaultInstance())
                    .save(recipeOutput.withConditions(new ModLoadedCondition("mysticalagradditions")), ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting/ma/" + essence + "_apple"));
            // casting block
            BlockCastingRecipeBuilder.of(SizedFluidIngredient.of(essenceFluid, 810), BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("mysticalagriculture", essence + "_block")).getDefaultInstance())
                    .save(recipeOutput.withConditions(new ModLoadedCondition("mysticalagradditions")), ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting/ma/" + essence + "_block"));
        }
        // melt inferium ores
        ItemMeltingRecipeBuilder.of(
                Ingredient.of(ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "ores/inferium"))),
                new FluidStack(BuiltInRegistries.FLUID.get(ResourceLocation.fromNamespaceAndPath("mysticalagradditions", "molten_inferium")), 360)
        ).save(recipeOutput.withConditions(new ModLoadedCondition("mysticalagradditions")), ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "melting/ma/inferium_ores"));

        // cast inferium_seeds
        ItemCastingRecipeBuilder.of(Items.WHEAT_SEEDS.getDefaultInstance(), SizedFluidIngredient.of(BuiltInRegistries.FLUID.get(ResourceLocation.fromNamespaceAndPath("mysticalagradditions", "molten_inferium")), 720), BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("mysticalagriculture", "inferium_seeds")).getDefaultInstance())
                .save(recipeOutput.withConditions(new ModLoadedCondition("mysticalagradditions")), ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting/ma/inferium_seeds"));
    }

    private void sgCompat(RecipeOutput recipeOutput) {
        var compatRecipeOutput = recipeOutput.withConditions(new ModLoadedCondition("silentgear"));

        Map<String, Pair<PartMaterialIngredient, Integer>> materialCost = new HashMap<>() {{
            put("sword", Pair.of(PartMaterialIngredient.of(PartTypes.MAIN.get(), GearTypes.TOOL.get()), 2));
            put("katana", Pair.of(PartMaterialIngredient.of(PartTypes.MAIN.get(), GearTypes.TOOL.get()), 3));
            put("machete", Pair.of(PartMaterialIngredient.of(PartTypes.MAIN.get(), GearTypes.TOOL.get()), 3));
            put("spear", Pair.of(PartMaterialIngredient.of(PartTypes.MAIN.get(), GearTypes.TOOL.get()), 1));
            put("trident", Pair.of(PartMaterialIngredient.of(PartTypes.MAIN.get(), GearTypes.TOOL.get()), 3));
            put("mace", Pair.of(PartMaterialIngredient.of(PartTypes.MAIN.get(), GearTypes.TOOL.get()), 3));
            put("knife", Pair.of(PartMaterialIngredient.of(PartTypes.MAIN.get(), GearTypes.TOOL.get()), 1));
            put("dagger", Pair.of(PartMaterialIngredient.of(PartTypes.MAIN.get(), GearTypes.TOOL.get()), 1));
            put("pickaxe", Pair.of(PartMaterialIngredient.of(PartTypes.MAIN.get(), GearTypes.TOOL.get()), 3));
            put("shovel", Pair.of(PartMaterialIngredient.of(PartTypes.MAIN.get(), GearTypes.TOOL.get()), 1));
            put("axe", Pair.of(PartMaterialIngredient.of(PartTypes.MAIN.get(), GearTypes.TOOL.get()), 3));
            put("paxel", Pair.of(PartMaterialIngredient.of(PartTypes.MAIN.get(), GearTypes.TOOL.get()), 5));
            put("hammer", Pair.of(PartMaterialIngredient.of(PartTypes.MAIN.get(), GearTypes.TOOL.get()), 6));
            put("excavator", Pair.of(PartMaterialIngredient.of(PartTypes.MAIN.get(), GearTypes.TOOL.get()), 5));
            put("hoe", Pair.of(PartMaterialIngredient.of(PartTypes.MAIN.get(), GearTypes.TOOL.get()), 2));
            put("mattock", Pair.of(PartMaterialIngredient.of(PartTypes.MAIN.get(), GearTypes.TOOL.get()), 4));
            put("prospector_hammer", Pair.of(PartMaterialIngredient.of(PartTypes.MAIN.get(), GearTypes.TOOL.get()), 2));
            put("saw", Pair.of(PartMaterialIngredient.of(PartTypes.MAIN.get(), GearTypes.TOOL.get()), 5));
            put("sickle", Pair.of(PartMaterialIngredient.of(PartTypes.MAIN.get(), GearTypes.TOOL.get()), 3));
            put("shears", Pair.of(PartMaterialIngredient.of(PartTypes.MAIN.get(), GearTypes.TOOL.get()), 2));
            put("fishing_rod", Pair.of(PartMaterialIngredient.of(PartTypes.MAIN.get(), GearTypes.TOOL.get()), 2));
            put("bow", Pair.of(PartMaterialIngredient.of(PartTypes.MAIN.get(), GearTypes.TOOL.get()), 3));
            put("crossbow", Pair.of(PartMaterialIngredient.of(PartTypes.MAIN.get(), GearTypes.TOOL.get()), 3));
            put("slingshot", Pair.of(PartMaterialIngredient.of(PartTypes.MAIN.get(), GearTypes.TOOL.get()), 2));
            put("arrow", Pair.of(PartMaterialIngredient.of(PartTypes.MAIN.get(), GearTypes.PROJECTILE.get()), 1));

            put("ring", Pair.of(PartMaterialIngredient.of(PartTypes.MAIN.get(), GearTypes.CURIO.get(), MaterialCategories.METAL), 2));
            put("bracelet", Pair.of(PartMaterialIngredient.of(PartTypes.MAIN.get(), GearTypes.CURIO.get(), MaterialCategories.METAL), 3));
            put("necklace", Pair.of(PartMaterialIngredient.of(PartTypes.MAIN.get(), GearTypes.CURIO.get(), MaterialCategories.METAL), 3));
            put("helmet", Pair.of(PartMaterialIngredient.of(PartTypes.MAIN.get(), GearTypes.HELMET.get()), 5));
            put("chestplate", Pair.of(PartMaterialIngredient.of(PartTypes.MAIN.get(), GearTypes.CHESTPLATE.get()), 8));
            put("leggings", Pair.of(PartMaterialIngredient.of(PartTypes.MAIN.get(), GearTypes.LEGGINGS.get()), 7));
            put("boots", Pair.of(PartMaterialIngredient.of(PartTypes.MAIN.get(), GearTypes.BOOTS.get()), 4));
            put("shield", Pair.of(PartMaterialIngredient.of(PartTypes.MAIN.get(), GearTypes.ARMOR.get()), 2));
            put("elytra", Pair.of(PartMaterialIngredient.of(PartTypes.MAIN.get(), GearTypes.ELYTRA.get(), MaterialCategories.CLOTH, MaterialCategories.SHEET), 6));
        }};

        // Iterate blueprints and use them as the cast
        GearItemSets.getIterator().forEachRemaining(gearItemSet -> {
            var amount = materialCost.get(gearItemSet.name());
            if (gearItemSet.partName().equals("mace_core")) {
                SilentGearCastingRecipeBuilder.of(Items.HEAVY_CORE.getDefaultInstance(), amount.getFirst(), amount.getSecond(), gearItemSet.mainPart().getDefaultInstance(), true)
                    .save(compatRecipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting/silentgear/" + gearItemSet.partName()));
            } else if (!gearItemSet.partName().equals("elytra_wings")) {
                SilentGearCastingRecipeBuilder.of(gearItemSet.blueprint().getDefaultInstance(), amount.getFirst(), amount.getSecond(), gearItemSet.mainPart().getDefaultInstance(), false)
                    .save(compatRecipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting/silentgear/" + gearItemSet.partName()));
            }
        });
        SilentGearCastingRecipeBuilder.of(SgItems.ROD_BLUEPRINT.get().getDefaultInstance(), PartMaterialIngredient.of(PartTypes.ROD.get()), 2, SgItems.ROD.get().getDefaultInstance(), false)
                .save(compatRecipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting/silentgear/tool_rod"));
        SilentGearCastingRecipeBuilder.of(SgItems.TIP_BLUEPRINT.get().getDefaultInstance(), PartMaterialIngredient.of(PartTypes.TIP.get()), 2, SgItems.TIP.get().getDefaultInstance(), false)
                .save(compatRecipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting/silentgear/tip"));

        ItemCastingRecipeBuilder.of(SgItems.NETHER_BANANA.get().getDefaultInstance(), SizedFluidIngredient.of(ModTags.Fluids.MOLTEN_GOLD, 720), SgItems.GOLDEN_NETHER_BANANA.get().getDefaultInstance(), true)
                .save(compatRecipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting/silentgear/golden_nether_banana"));

        // TODO Overwrite /data/silentgear/recipe/gear/* recipes to only take simple material categories and not metal or gems that we have fluids for
    }

    private void itemMeltingRecipe(TagKey<Item> tag, FluidStack output, RecipeOutput recipeOutput) {
        itemMeltingRecipe(tag, output, 1000, 0, recipeOutput);
    }

    private void itemMeltingRecipe(TagKey<Item> tag, FluidStack output, int min, int max, RecipeOutput recipeOutput) {
        ItemMeltingRecipeBuilder.of(Ingredient.of(tag), output, min, max).save(recipeOutput.withConditions(new NotCondition(new TagEmptyCondition(tag))), ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "melting/" + tag.location().getPath()));
    }

    private void alloyRecipe(List<SizedFluidIngredient> inputs, int speed, FluidStack output, RecipeOutput recipeOutput) {
        var conditionalRecipeOutput = recipeOutput;
        for (SizedFluidIngredient sizedFluidIngredient : inputs) {
            if (sizedFluidIngredient.ingredient() instanceof TagFluidIngredient tagFluidIngredient) {
                conditionalRecipeOutput = conditionalRecipeOutput.withConditions(new NotCondition(new FluidTagEmptyCondition(tagFluidIngredient.tag())));
            }
        }
        FluidAlloyingRecipeBuilder.of(inputs, speed, output).save(conditionalRecipeOutput, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "alloying/" + BuiltInRegistries.FLUID.getKey(output.getFluid()).getPath()));
    }
}
