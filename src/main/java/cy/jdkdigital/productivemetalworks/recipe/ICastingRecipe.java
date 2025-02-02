package cy.jdkdigital.productivemetalworks.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;

public interface ICastingRecipe extends Recipe<RecipeInput>
{
   boolean matches(ItemStack cast, FluidStack fluid, Level level);

   boolean matches(ItemStack cast, FluidStack fluid, boolean matchFluidAmount, Level level);

   ItemStack getResultItem(Level level, FluidStack containedFluid);

   int getFluidAmount(Level level, FluidStack containedFluid);
}
