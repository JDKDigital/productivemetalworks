package cy.jdkdigital.productivemetalworks.event;

import cy.jdkdigital.productivemetalworks.recipe.ItemCastingRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.neoforge.fluids.FluidStack;

public class CastingRecipeEvent extends Event implements IModBusEvent
{
    public final Level level;
    public final ItemStack cast;
    public final FluidStack fluid;
    public final boolean isTable;
    private ItemCastingRecipe recipe;

    public CastingRecipeEvent(Level level, ItemStack cast, FluidStack fluid, boolean isTable) {
        this.level = level;
        this.cast = cast;
        this.fluid = fluid;
        this.isTable = isTable;
    }

    public ItemCastingRecipe getRecipe() {
        return recipe;
    }

    public void setRecipe(ItemCastingRecipe recipe) {
        this.recipe = recipe;
    }

    public boolean hasRecipe() {
        return recipe != null;
    }
}
