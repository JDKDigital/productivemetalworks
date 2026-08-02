package cy.jdkdigital.productivemetalworks.datagen.recipe;

import cy.jdkdigital.productivemetalworks.recipe.ItemMeltingRecipe;
import cy.jdkdigital.productivemetalworks.util.FluidStackTemplate;
import net.minecraft.advancements.Criterion;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ItemMeltingRecipeBuilder implements RecipeBuilder
{
    private final Ingredient item;
    private final List<FluidStackTemplate> result;
    private final int minTemperature;
    private final int maxTemperature;

    private ItemMeltingRecipeBuilder(Ingredient item, List<FluidStackTemplate> result, int minTemperature, int maxTemperature) {
        this.item = item;
        this.result = result;
        this.minTemperature = minTemperature;
        this.maxTemperature = maxTemperature;
    }

    public static ItemMeltingRecipeBuilder of(Ingredient item, FluidStackTemplate result) {
        return new ItemMeltingRecipeBuilder(item, List.of(result), 1000, 0);
    }

    public static ItemMeltingRecipeBuilder of(Ingredient item, FluidStackTemplate result, int minTemperature, int maxTemperature) {
        return new ItemMeltingRecipeBuilder(item, List.of(result), minTemperature, maxTemperature);
    }

    public static ItemMeltingRecipeBuilder of(Ingredient item, List<FluidStackTemplate> result) {
        return new ItemMeltingRecipeBuilder(item, result, 1000, 0);
    }

    public static ItemMeltingRecipeBuilder of(Ingredient item, List<FluidStackTemplate> result, int minTemperature, int maxTemperature) {
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
    public ResourceKey<Recipe<?>> defaultId() {
        return ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath("productivemetalworks", "empty"));
    }

    @Override
    public void save(RecipeOutput recipeOutput, ResourceKey<Recipe<?>> id) {
        recipeOutput.accept(id, new ItemMeltingRecipe(item, result, minTemperature, maxTemperature), null);
    }

    public void save(RecipeOutput recipeOutput, Identifier id) {
        save(recipeOutput, ResourceKey.create(Registries.RECIPE, id));
    }
}
