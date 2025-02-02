package cy.jdkdigital.productivemetalworks.datagen.recipe;

import cy.jdkdigital.productivemetalworks.recipe.BlockCastingRecipe;
import net.minecraft.advancements.Criterion;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import org.jetbrains.annotations.Nullable;

public class BlockCastingRecipeBuilder implements RecipeBuilder
{
    private final Ingredient cast;
    private final SizedFluidIngredient fluid;
    private final ItemStack result;
    private final boolean consumeCast;

    private BlockCastingRecipeBuilder(Ingredient cast, SizedFluidIngredient fluid, ItemStack result, boolean consumeCast) {
        this.cast = cast;
        this.fluid = fluid;
        this.result = result;
        this.consumeCast = consumeCast;
    }

    public static BlockCastingRecipeBuilder of(SizedFluidIngredient fluid, ItemStack result) {
        return of(ItemStack.EMPTY, fluid, result, false);
    }

    public static BlockCastingRecipeBuilder of(ItemStack cast, SizedFluidIngredient fluid, ItemStack result) {
        return of(cast, fluid, result, true);
    }

    public static BlockCastingRecipeBuilder of(ItemStack cast, SizedFluidIngredient fluid, ItemStack result, boolean consumeCast) {
        return of(Ingredient.of(cast), fluid, result, consumeCast);
    }

    public static BlockCastingRecipeBuilder of(Ingredient cast, SizedFluidIngredient fluid, ItemStack result, boolean consumeCast) {
        return new BlockCastingRecipeBuilder(cast, fluid, result, consumeCast);
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
        recipeOutput.accept(id, new BlockCastingRecipe(cast, fluid, result, consumeCast), null);
    }
}
