package cy.jdkdigital.productivemetalworks.datagen.recipe;

import cy.jdkdigital.productivemetalworks.recipe.FluidAlloyingRecipe;
import cy.jdkdigital.productivemetalworks.util.FluidStackTemplate;
import net.minecraft.advancements.Criterion;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FluidAlloyingRecipeBuilder implements RecipeBuilder
{
    private final List<SizedFluidIngredient> fluids;
    private final int speed;
    private final FluidStackTemplate result;

    private FluidAlloyingRecipeBuilder(List<SizedFluidIngredient> fluids, int speed, FluidStackTemplate result) {
        this.fluids = fluids;
        this.speed = speed;
        this.result = result;
    }

    public static FluidAlloyingRecipeBuilder of(List<SizedFluidIngredient> fluids, FluidStackTemplate result) {
        return new FluidAlloyingRecipeBuilder(fluids, 1, result);
    }

    public static FluidAlloyingRecipeBuilder of(List<SizedFluidIngredient> fluids, int speed, FluidStackTemplate result) {
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
    public ResourceKey<Recipe<?>> defaultId() {
        return ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath("productivemetalworks", "empty"));
    }

    @Override
    public void save(RecipeOutput recipeOutput, ResourceKey<Recipe<?>> id) {
        recipeOutput.accept(id, new FluidAlloyingRecipe(fluids, speed, result), null);
    }

    public void save(RecipeOutput recipeOutput, Identifier id) {
        save(recipeOutput, ResourceKey.create(Registries.RECIPE, id));
    }
}
