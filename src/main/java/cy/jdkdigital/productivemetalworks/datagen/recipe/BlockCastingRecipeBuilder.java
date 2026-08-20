package cy.jdkdigital.productivemetalworks.datagen.recipe;

import cy.jdkdigital.productivemetalworks.recipe.BlockCastingRecipe;
import net.minecraft.advancements.Criterion;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import net.minecraft.world.item.Items;

public class BlockCastingRecipeBuilder implements RecipeBuilder
{
    private final Optional<Ingredient> cast;
    private final SizedFluidIngredient fluid;
    private final ItemStackTemplate result;
    private final boolean consumeCast;
    // true when a cross-mod item (cast or result) was absent on the data classpath → save() is a no-op.
    private final boolean skip;

    private BlockCastingRecipeBuilder(Optional<Ingredient> cast, SizedFluidIngredient fluid, ItemStackTemplate result, boolean consumeCast, boolean skip) {
        this.cast = cast;
        this.fluid = fluid;
        this.result = result;
        this.consumeCast = consumeCast;
        this.skip = skip;
    }

    public static BlockCastingRecipeBuilder of(SizedFluidIngredient fluid, ItemStackTemplate result) {
        return new BlockCastingRecipeBuilder(Optional.empty(), fluid, result, false, result == null);
    }

    public static BlockCastingRecipeBuilder of(ItemStackTemplate cast, SizedFluidIngredient fluid, ItemStackTemplate result) {
        return of(cast, fluid, result, true);
    }

    public static BlockCastingRecipeBuilder of(ItemStackTemplate cast, SizedFluidIngredient fluid, ItemStackTemplate result, boolean consumeCast) {
        if (cast == null || result == null) {
            return new BlockCastingRecipeBuilder(Optional.empty(), fluid, result, consumeCast, true);
        }
        Optional<Ingredient> ing = cast.item().value() == Items.AIR ? Optional.empty() : Optional.of(Ingredient.of(cast.item().value()));
        return new BlockCastingRecipeBuilder(ing, fluid, result, consumeCast, false);
    }

    public static BlockCastingRecipeBuilder of(Ingredient cast, SizedFluidIngredient fluid, ItemStackTemplate result, boolean consumeCast) {
        return new BlockCastingRecipeBuilder(Optional.of(cast), fluid, result, consumeCast, result == null);
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
        if (skip) {
            return;
        }
        recipeOutput.accept(id, new BlockCastingRecipe(cast, fluid, result, consumeCast), null);
    }

    public void save(RecipeOutput recipeOutput, Identifier id) {
        save(recipeOutput, ResourceKey.create(Registries.RECIPE, id));
    }
}
