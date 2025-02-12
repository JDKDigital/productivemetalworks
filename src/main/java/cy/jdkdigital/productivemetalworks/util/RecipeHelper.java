package cy.jdkdigital.productivemetalworks.util;

import cy.jdkdigital.productivelib.util.MultiFluidTank;
import cy.jdkdigital.productivemetalworks.recipe.*;
import cy.jdkdigital.productivemetalworks.registry.MetalworksRegistrator;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipeHelper
{
    static Map<String, RecipeHolder<ItemMeltingRecipe>> itemMeltingRecipeCache = new HashMap<>();
    @Nullable
    public static RecipeHolder<ItemMeltingRecipe> getItemMeltingRecipe(Level level, ItemStack item) {
        String cacheKey = itemCacheKey(item);
        if (!itemMeltingRecipeCache.containsKey(cacheKey)) {
            for (RecipeHolder<ItemMeltingRecipe> recipeHolder : level.getRecipeManager().getAllRecipesFor(MetalworksRegistrator.ITEM_MELTING_TYPE.get())) {
                if (recipeHolder.value().matches(item)) {
                    itemMeltingRecipeCache.put(cacheKey, recipeHolder);
                }
            }
        }
        return itemMeltingRecipeCache.getOrDefault(cacheKey, null);
    }

    static Map<String, RecipeHolder<ItemCastingRecipe>> itemCastingRecipeCache = new HashMap<>();
    @Nullable
    public static RecipeHolder<ItemCastingRecipe> getItemCastingRecipe(Level level, ItemStack cast, FluidStack fluid) {
        String cacheKey = itemCacheKey(cast) + fluidCacheKey(fluid);
        if (!itemCastingRecipeCache.containsKey(cacheKey)) {
            for (RecipeHolder<ItemCastingRecipe> recipeHolder : level.getRecipeManager().getAllRecipesFor(MetalworksRegistrator.ITEM_CASTING_TYPE.get())) {
                if (recipeHolder.value().matches(cast, fluid, level)) {
                    itemCastingRecipeCache.put(cacheKey, recipeHolder);
                }
            }
        }
        return itemCastingRecipeCache.getOrDefault(cacheKey, null);
    }

    static Map<String, RecipeHolder<BlockCastingRecipe>> blockCastingRecipeCache = new HashMap<>();
    @Nullable
    public static RecipeHolder<BlockCastingRecipe> getBlockCastingRecipe(Level level, ItemStack cast, FluidStack fluid) {
        String cacheKey = itemCacheKey(cast) + fluidCacheKey(fluid);
        if (!blockCastingRecipeCache.containsKey(cacheKey)) {
            for (RecipeHolder<BlockCastingRecipe> recipeHolder : level.getRecipeManager().getAllRecipesFor(MetalworksRegistrator.BLOCK_CASTING_TYPE.get())) {
                if (recipeHolder.value().matches(cast, fluid, level)) {
                    blockCastingRecipeCache.put(cacheKey, recipeHolder);
                }
            }
        }
        return blockCastingRecipeCache.getOrDefault(cacheKey, null);
    }

    static List<RecipeHolder<FluidAlloyingRecipe>> alloyRecipes = new ArrayList<>();
    public static List<RecipeHolder<FluidAlloyingRecipe>> getAlloyRecipes(Level level, MultiFluidTank fluidHandler) {
        // Iterate fluid tanks and try to alloy fluids from 2 tanks
        if (alloyRecipes.isEmpty()) {
            alloyRecipes = level.getRecipeManager().getAllRecipesFor(MetalworksRegistrator.FLUID_ALLOYING_TYPE.get());
        }

        List<FluidStack> availableFluids = new ArrayList<>();
        for (int tank = 0; tank < fluidHandler.getTanks(); tank++){
            var fluidStack = fluidHandler.getFluidInTank(tank);
            if (!fluidStack.isEmpty()) {
                availableFluids.add(fluidStack);
            }
        }
        List<RecipeHolder<FluidAlloyingRecipe>> recipeProcessList = new ArrayList<>();
        for (RecipeHolder<FluidAlloyingRecipe> recipeHolder : alloyRecipes) {
            if (recipeHolder.value().matches(availableFluids)) {
                recipeProcessList.add(recipeHolder);
            }
        }
        return recipeProcessList;
    }

    // TODO move to lib
    public static String itemCacheKey(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).toString() + (!stack.getComponents().isEmpty() ? stack.getComponents().stream().map(TypedDataComponent::toString).reduce((s, s2) -> s + s2) : "");
    }
    public static String fluidCacheKey(FluidStack stack) {
        return BuiltInRegistries.FLUID.getKey(stack.getFluid()).toString() + (!stack.getComponents().isEmpty() ? stack.getComponents().stream().map(TypedDataComponent::toString).reduce((s, s2) -> s + s2) : "");
    }
}
