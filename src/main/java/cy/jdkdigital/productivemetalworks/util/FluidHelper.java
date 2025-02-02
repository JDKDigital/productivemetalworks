package cy.jdkdigital.productivemetalworks.util;

import com.mojang.datafixers.util.Pair;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class FluidHelper
{
    /**
     * Finds the fluid an item melts into
     */
    static Map<Item, FluidStack> itemToFluidCache = new HashMap<>();
    public static FluidStack itemToFluid(Level level, ItemStack itemStack) {
        if (!itemToFluidCache.containsKey(itemStack.getItem())) {
            var meltingRecipe = RecipeHelper.getItemMeltingRecipe(level, itemStack);
            if (meltingRecipe != null && meltingRecipe.value().result.size() == 1) {
                itemToFluidCache.put(itemStack.getItem(), meltingRecipe.value().result.getFirst().copy());
            }
        }
        return itemToFluidCache.getOrDefault(itemStack.getItem(), null);
    }

    /**
     * Finds list of fluids and amount from a list of items based on what the items melt into
     */
    public static Map<Fluid, Pair<ItemStack, Integer>> materialsToFluids(Level level, Stream<ItemStack> items, int materialCount) {
        Map<Fluid, Pair<ItemStack, Integer>> fluids = new HashMap<>();
        items.forEach(stack -> {
            if (!stack.isEmpty() && !stack.is(Items.BARRIER)) {
                var fluid = itemToFluid(level, stack);
                if (fluid != null) {
                    if (!fluids.containsKey(fluid.getFluid())) {
                        fluids.put(fluid.getFluid(), Pair.of(stack, (fluid.getAmount() * materialCount)));
                    }
                }
            }
        });
        return fluids;
    }
}
