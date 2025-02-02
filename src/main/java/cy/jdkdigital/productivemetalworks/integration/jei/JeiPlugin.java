package cy.jdkdigital.productivemetalworks.integration.jei;

import cy.jdkdigital.productivemetalworks.ProductiveMetalworks;
import cy.jdkdigital.productivemetalworks.recipe.*;
import cy.jdkdigital.productivemetalworks.registry.MetalworksRegistrator;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;

import java.util.ArrayList;
import java.util.List;

@mezz.jei.api.JeiPlugin
public class JeiPlugin implements IModPlugin
{
    private static final ResourceLocation pluginId = ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, ProductiveMetalworks.MODID);

    public static final RecipeType<RecipeHolder<ItemMeltingRecipe>> ITEM_MELTING = RecipeType.createRecipeHolderType(MetalworksRegistrator.ITEM_MELTING_TYPE.getId());
    public static final RecipeType<RecipeHolder<ItemCastingRecipe>> ITEM_CASTING = RecipeType.createRecipeHolderType(MetalworksRegistrator.ITEM_CASTING_TYPE.getId());
    public static final RecipeType<RecipeHolder<BlockCastingRecipe>> BLOCK_CASTING = RecipeType.createRecipeHolderType(MetalworksRegistrator.BLOCK_CASTING_TYPE.getId());
    public static final RecipeType<RecipeHolder<FluidAlloyingRecipe>> FLUID_ALLOYING = RecipeType.createRecipeHolderType(MetalworksRegistrator.FLUID_ALLOYING_TYPE.getId());
    public static final RecipeType<RecipeHolder<SilentGearCastingRecipe>> SG_CASTING = RecipeType.createRecipeHolderType(MetalworksRegistrator.SG_GEAR_CASTING_TYPE.getId());

    @Override
    public ResourceLocation getPluginUid() {
        return pluginId;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IJeiHelpers jeiHelpers = registration.getJeiHelpers();
        IGuiHelper guiHelper = jeiHelpers.getGuiHelper();

        registration.addRecipeCategories(new ItemMeltingRecipeCategory(guiHelper));
        registration.addRecipeCategories(new ItemCastingRecipeCategory(guiHelper));
        registration.addRecipeCategories(new BlockCastingRecipeCategory(guiHelper));
        registration.addRecipeCategories(new FluidAlloyingRecipeCategory(guiHelper));
        if (ModList.get().isLoaded("silentgear")) {
            registration.addRecipeCategories(new SilentGearCastingRecipeCategory(guiHelper));
        }
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(MetalworksRegistrator.FOUNDRY_CONTROLLERS.get(DyeColor.BLACK).get(), ITEM_MELTING);
        registration.addRecipeCatalyst(MetalworksRegistrator.FOUNDRY_CONTROLLERS.get(DyeColor.BLACK).get(), FLUID_ALLOYING);
        registration.addRecipeCatalyst(MetalworksRegistrator.CASTING_TABLE.get(), ITEM_CASTING);
        registration.addRecipeCatalyst(MetalworksRegistrator.CASTING_BASIN.get(), BLOCK_CASTING);
        if (ModList.get().isLoaded("silentgear")) {
            registration.addRecipeCatalyst(MetalworksRegistrator.CASTING_TABLE.get(), SG_CASTING);
        }
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        RecipeManager recipeManager = Minecraft.getInstance().level.getRecipeManager();

        registration.addRecipes(ITEM_MELTING, recipeManager.getAllRecipesFor(MetalworksRegistrator.ITEM_MELTING_TYPE.get()));
        registration.addRecipes(ITEM_CASTING, recipeManager.getAllRecipesFor(MetalworksRegistrator.ITEM_CASTING_TYPE.get()));
        registration.addRecipes(BLOCK_CASTING, recipeManager.getAllRecipesFor(MetalworksRegistrator.BLOCK_CASTING_TYPE.get()));
        registration.addRecipes(FLUID_ALLOYING, recipeManager.getAllRecipesFor(MetalworksRegistrator.FLUID_ALLOYING_TYPE.get()));
        if (ModList.get().isLoaded("silentgear")) {
            registration.addRecipes(SG_CASTING, recipeManager.getAllRecipesFor(MetalworksRegistrator.SG_GEAR_CASTING_TYPE.get()));
        }

        // Waxing recipes
        List<RecipeHolder<BlockCastingRecipe>> WAXING_RECIPES = new ArrayList<>();
        BuiltInRegistries.BLOCK.holders().forEach(blockReference -> {
            var waxData = blockReference.getData(NeoForgeDataMaps.WAXABLES);
            if (waxData != null) {
                WAXING_RECIPES.add(new RecipeHolder<>(ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, blockReference.key().location().getPath()), new BlockCastingRecipe(Ingredient.of(blockReference.value()), SizedFluidIngredient.of(MetalworksRegistrator.MOLTEN_WAX.get(), 50), waxData.waxed().asItem().getDefaultInstance(), true)));
            }
        });
        registration.addRecipes(BLOCK_CASTING, WAXING_RECIPES);

        // Bucket filling recipes
        List<RecipeHolder<ItemCastingRecipe>> BUCKET_RECIPES = new ArrayList<>();
        BuiltInRegistries.FLUID.entrySet().forEach(fluidHolder -> {
            var fluid = fluidHolder.getValue();
            if (fluid.getBucket() != Items.AIR) {
                BUCKET_RECIPES.add(new RecipeHolder<>(ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, fluidHolder.getKey().location().getPath()), new BlockCastingRecipe(Ingredient.of(Items.BUCKET), SizedFluidIngredient.of(fluid, 1000), fluid.getBucket().getDefaultInstance(), true)));
            }
        });
        registration.addRecipes(ITEM_CASTING, BUCKET_RECIPES);
    }
}
