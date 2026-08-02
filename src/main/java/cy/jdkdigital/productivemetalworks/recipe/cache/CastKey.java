package cy.jdkdigital.productivemetalworks.recipe.cache;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

// Cache key for item/block casting lookups (cast item + fluid), keyed on the immutable component patches.
public record CastKey(Item item, DataComponentPatch components, Fluid fluid, DataComponentPatch fluidComponents) {
    public static CastKey of(ItemStack cast, FluidStack fluid) {
        return new CastKey(cast.getItem(), cast.getComponentsPatch(), fluid.getFluid(), fluid.getComponentsPatch());
    }
}
