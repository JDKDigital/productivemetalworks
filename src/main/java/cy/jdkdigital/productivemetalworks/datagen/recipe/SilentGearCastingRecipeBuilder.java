package cy.jdkdigital.productivemetalworks.datagen.recipe;

import cy.jdkdigital.productivemetalworks.recipe.SilentGearCastingRecipe;
import net.minecraft.advancements.Criterion;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.silentchaos512.gear.crafting.ingredient.PartMaterialIngredient;
import org.jetbrains.annotations.Nullable;

public class SilentGearCastingRecipeBuilder implements RecipeBuilder
{
    private final Ingredient cast;
    private final PartMaterialIngredient fluid;
    private final int materialCount;
    private final ItemStack result;
    private final boolean consumeCast;

    private SilentGearCastingRecipeBuilder(Ingredient cast, PartMaterialIngredient material, int materialCount, ItemStack result, boolean consumeCast) {
        this.cast = cast;
        this.fluid = material;
        this.materialCount = materialCount;
        this.result = result;
        this.consumeCast = consumeCast;
    }

    public static SilentGearCastingRecipeBuilder of(ItemStack cast, PartMaterialIngredient material, int materialCount, ItemStack result, boolean consumeCast) {
        return of(Ingredient.of(cast), material, materialCount, result, consumeCast);
    }

    public static SilentGearCastingRecipeBuilder of(Ingredient cast, PartMaterialIngredient material, int materialCount, ItemStack result, boolean consumeCast) {
        return new SilentGearCastingRecipeBuilder(cast, material, materialCount, result, consumeCast);
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
        recipeOutput.accept(id, new SilentGearCastingRecipe(cast, fluid, materialCount, result, consumeCast), null);
    }
}
