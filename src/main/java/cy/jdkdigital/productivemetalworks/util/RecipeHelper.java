package cy.jdkdigital.productivemetalworks.util;

import cy.jdkdigital.productivelib.compat.jei.RecipeMapCache;
import cy.jdkdigital.productivemetalworks.common.datamap.FuelMap;
import cy.jdkdigital.productivemetalworks.event.CastingRecipeEvent;
import cy.jdkdigital.productivemetalworks.recipe.BlockCastingRecipe;
import cy.jdkdigital.productivemetalworks.recipe.cache.CastKey;
import cy.jdkdigital.productivemetalworks.recipe.cache.CompatKey;
import cy.jdkdigital.productivemetalworks.recipe.cache.MeltKey;
import cy.jdkdigital.productivemetalworks.recipe.FluidAlloyingRecipe;
import cy.jdkdigital.productivemetalworks.recipe.ItemCastingRecipe;
import cy.jdkdigital.productivemetalworks.recipe.ItemMeltingRecipe;
import cy.jdkdigital.productivemetalworks.registry.MetalworksRegistrator;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipeHelper
{
    // The server has the full recipe manager; the client only has the foundry recipe types opted into sync
    // (EventHandler#onDatapackSync), cached in RecipeMapCache. Resolve from whichever side we're on so that
    // client-side lookups — e.g. the melt time the foundry GUI shows — work too, not just the server.
    @Nullable
    private static RecipeMap recipeMap(Level level) {
        return level instanceof ServerLevel serverLevel ? serverLevel.recipeAccess().recipeMap() : RecipeMapCache.getRecipeMap();
    }

    static Map<MeltKey, RecipeHolder<ItemMeltingRecipe>> itemMeltingRecipeCache = new HashMap<>();
    @Nullable
    public static RecipeHolder<ItemMeltingRecipe> getItemMeltingRecipe(Level level, ItemStack item, @Nullable FuelMap fuelData) {
        if (fuelData == null) {
            return null;
        }
        MeltKey cacheKey = MeltKey.of(item, fuelData.temperature());
        RecipeMap recipeMap = recipeMap(level);
        if (!itemMeltingRecipeCache.containsKey(cacheKey) && recipeMap != null) {
            for (RecipeHolder<ItemMeltingRecipe> recipeHolder : recipeMap.byType(MetalworksRegistrator.ITEM_MELTING_TYPE.get())) {
                if (recipeHolder.value().matches(item, fuelData.temperature())) {
                    itemMeltingRecipeCache.put(cacheKey, recipeHolder);
                }
            }
        }
        return itemMeltingRecipeCache.getOrDefault(cacheKey, null);
    }

    static Map<CastKey, RecipeHolder<ItemCastingRecipe>> itemCastingRecipeCache = new HashMap<>();
    @Nullable
    public static RecipeHolder<ItemCastingRecipe> getItemCastingRecipe(Level level, ItemStack cast, FluidStack fluid) {
        CastKey cacheKey = CastKey.of(cast, fluid);
        RecipeMap recipeMap = recipeMap(level);
        if (!itemCastingRecipeCache.containsKey(cacheKey) && recipeMap != null) {
            for (RecipeHolder<ItemCastingRecipe> recipeHolder : recipeMap.byType(MetalworksRegistrator.ITEM_CASTING_TYPE.get())) {
                if (recipeHolder.value().matches(cast, fluid, level)) {
                    itemCastingRecipeCache.put(cacheKey, recipeHolder);
                }
            }
        }
        return itemCastingRecipeCache.getOrDefault(cacheKey, null);
    }

    static Map<CastKey, RecipeHolder<BlockCastingRecipe>> blockCastingRecipeCache = new HashMap<>();
    @Nullable
    public static RecipeHolder<BlockCastingRecipe> getBlockCastingRecipe(Level level, ItemStack cast, FluidStack fluid) {
        CastKey cacheKey = CastKey.of(cast, fluid);
        RecipeMap recipeMap = recipeMap(level);
        if (!blockCastingRecipeCache.containsKey(cacheKey) && recipeMap != null) {
            for (RecipeHolder<BlockCastingRecipe> recipeHolder : recipeMap.byType(MetalworksRegistrator.BLOCK_CASTING_TYPE.get())) {
                if (recipeHolder.value().matches(cast, fluid, level)) {
                    blockCastingRecipeCache.put(cacheKey, recipeHolder);
                }
            }
        }
        return blockCastingRecipeCache.getOrDefault(cacheKey, null);
    }

    static List<RecipeHolder<FluidAlloyingRecipe>> alloyRecipes = new ArrayList<>();
    public static List<RecipeHolder<FluidAlloyingRecipe>> getAlloyRecipes(Level level, ModFluidTank fluidHandler) {
        // Iterate fluid tanks and try to alloy fluids from 2 tanks
        RecipeMap recipeMap = recipeMap(level);
        if (alloyRecipes.isEmpty() && recipeMap != null) {
            alloyRecipes = new ArrayList<>(recipeMap.byType(MetalworksRegistrator.FLUID_ALLOYING_TYPE.get()));
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

    static Map<CompatKey, ItemCastingRecipe> compatCastingRecipeCache = new HashMap<>();
    public static ItemCastingRecipe getCompatRecipe(Level level, ItemStack cast, FluidStack fluid, boolean isTable) {
        CompatKey cacheKey = CompatKey.of(cast, fluid, isTable);
        if (!compatCastingRecipeCache.containsKey(cacheKey)) {
            var event = new CastingRecipeEvent(level, cast, fluid, isTable);
            ModLoader.postEvent(event);
            compatCastingRecipeCache.put(cacheKey, event.hasRecipe() ? event.getRecipe() : null);
        }
        return compatCastingRecipeCache.getOrDefault(cacheKey, null);
    }

}
