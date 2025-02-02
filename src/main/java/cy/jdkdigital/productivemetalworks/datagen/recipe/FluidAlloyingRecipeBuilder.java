package cy.jdkdigital.productivemetalworks.datagen.recipe;

import cy.jdkdigital.productivemetalworks.recipe.FluidAlloyingRecipe;
import net.minecraft.advancements.Criterion;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FluidAlloyingRecipeBuilder implements RecipeBuilder
{
    private final List<SizedFluidIngredient> fluids;
    private final int speed;
    private final FluidStack result;

    private FluidAlloyingRecipeBuilder(List<SizedFluidIngredient> fluids, int speed, FluidStack result) {
        this.fluids = fluids;
        this.speed = speed;
        this.result = result;
    }

    public static FluidAlloyingRecipeBuilder of(List<SizedFluidIngredient> fluids, FluidStack result) {
        return new FluidAlloyingRecipeBuilder(fluids, 1, result);
    }

    public static FluidAlloyingRecipeBuilder of(List<SizedFluidIngredient> fluids, int speed, FluidStack result) {
        return new FluidAlloyingRecipeBuilder(fluids, speed, result);
    }

    @Override
    public RecipeBuilder unlockedBy(String name, Criterion<?> criterion) {
        return this;
    }

    @Override
    public RecipeBuilder group(@Nullable String groupName) {
        return null;
    }

    @Override
    public Item getResult() {
        return Items.AIR;
    }

    @Override
    public void save(RecipeOutput recipeOutput, ResourceLocation id) {
        recipeOutput.accept(id, new FluidAlloyingRecipe(fluids, speed, result), null);
    }
}
