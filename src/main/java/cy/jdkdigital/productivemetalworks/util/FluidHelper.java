package cy.jdkdigital.productivemetalworks.util;

import com.mojang.datafixers.util.Pair;
import cy.jdkdigital.productivemetalworks.ProductiveMetalworks;
import cy.jdkdigital.productivemetalworks.common.datamap.FuelMap;
import cy.jdkdigital.productivemetalworks.registry.MetalworksRegistrator;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
            var meltingRecipe = RecipeHelper.getItemMeltingRecipe(level, itemStack, new FuelMap(100000, 1f, 1f));
            if (meltingRecipe != null && meltingRecipe.value().result.size() == 1) {
                itemToFluidCache.put(itemStack.getItem(), meltingRecipe.value().result.getFirst().create());
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

    /**
     * The fluids matched by a {@link SizedFluidIngredient}, each at the ingredient's amount.
     * Replaces the removed {@code SizedFluidIngredient#getFluids()}.
     */
    public static List<FluidStack> fluidStacks(SizedFluidIngredient fluidIngredient) {
        return fluidIngredient.ingredient().fluids().stream().map(holder -> new FluidStack(holder, fluidIngredient.amount())).toList();
    }

    public static List<Component> formatTooltip(SizedFluidIngredient fluidIngredient) {
        var firstFluid = fluidIngredient.ingredient().fluids().getFirst();
        return formatTooltip(new FluidStack(firstFluid, fluidIngredient.amount()));
    }
    public static List<Component> formatTooltip(FluidStack fluidStack) {
        List<Component> tooltips = new ArrayList<>();
        if (!fluidStack.isEmpty()) {
            var units = fluidStack.getFluid().builtInRegistryHolder().getData(MetalworksRegistrator.UNIT_MAP);
            if (units != null) {
                final int[] amountLeft = {fluidStack.getAmount()};
                units.units().reversed().forEach(unit -> {
                    var numberOfThisUnit = Math.floor((double) amountLeft[0] / unit.amount());
                    if (numberOfThisUnit > 0) {
                        tooltips.add(Component.translatable(ProductiveMetalworks.MODID + ".unit." + unit.unit() + "." + (numberOfThisUnit == 1 ? "single" : "multiple"), (int)numberOfThisUnit));
                        amountLeft[0] = amountLeft[0] - (int)numberOfThisUnit * unit.amount();
                    }
                });
                if (amountLeft[0] > 0) {
                    tooltips.add(Component.translatable(ProductiveMetalworks.MODID + ".unit.leftover", amountLeft[0]));
                }
            }
        }
        return tooltips;
    }
}
