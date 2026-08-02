package cy.jdkdigital.productivemetalworks.recipe.cache;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

// Cache key for event-driven compat casting lookups; distinguishes table vs basin as well as cast + fluid.
public record CompatKey(Item item, DataComponentPatch components, Fluid fluid, DataComponentPatch fluidComponents, boolean isTable) {
    public static CompatKey of(ItemStack cast, FluidStack fluid, boolean isTable) {
        return new CompatKey(cast.getItem(), cast.getComponentsPatch(), fluid.getFluid(), fluid.getComponentsPatch(), isTable);
    }
}
