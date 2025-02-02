package cy.jdkdigital.productivemetalworks.datagen.recipe;

import cy.jdkdigital.productivemetalworks.recipe.ItemMeltingRecipe;
import net.minecraft.advancements.Criterion;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ItemMeltingRecipeBuilder implements RecipeBuilder
{
    private final Ingredient item;
    private final List<FluidStack> result;
    private final int minTemperature;
    private final int maxTemperature;

    private ItemMeltingRecipeBuilder(Ingredient item, List<FluidStack> result, int minTemperature, int maxTemperature) {
        this.item = item;
        this.result = result;
        this.minTemperature = minTemperature;
        this.maxTemperature = maxTemperature;
    }

    public static ItemMeltingRecipeBuilder of(Ingredient item, FluidStack result) {
        return new ItemMeltingRecipeBuilder(item, List.of(result), 1000, 0);
    }

    public static ItemMeltingRecipeBuilder of(Ingredient item, FluidStack result, int minTemperature, int maxTemperature) {
        return new ItemMeltingRecipeBuilder(item, List.of(result), minTemperature, maxTemperature);
    }

    public static ItemMeltingRecipeBuilder of(Ingredient item, List<FluidStack> result) {
        return new ItemMeltingRecipeBuilder(item, result, 1000, 0);
    }

    public static ItemMeltingRecipeBuilder of(Ingredient item, List<FluidStack> result, int minTemperature, int maxTemperature) {
        return new ItemMeltingRecipeBuilder(item, result, minTemperature, maxTemperature);
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
        recipeOutput.accept(id, new ItemMeltingRecipe(item, result, minTemperature, maxTemperature), null);
    }
}
