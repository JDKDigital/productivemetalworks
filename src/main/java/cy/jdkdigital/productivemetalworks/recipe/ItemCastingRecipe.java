package cy.jdkdigital.productivemetalworks.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import cy.jdkdigital.productivemetalworks.registry.MetalworksRegistrator;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.util.Optional;

public class ItemCastingRecipe implements ICastingRecipe
{
    public static final MapCodec<ItemCastingRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(
            builder -> builder.group(
                            Ingredient.CODEC.optionalFieldOf("cast").forGetter(recipe -> recipe.cast),
                            SizedFluidIngredient.CODEC.fieldOf("fluid").forGetter(recipe -> recipe.fluid),
                            ItemStackTemplate.CODEC.fieldOf("result").forGetter(recipe -> recipe.result),
                            Codec.BOOL.fieldOf("consume_cast").orElse(false).forGetter(recipe -> recipe.consumeCast)
                    )
                    .apply(builder, ItemCastingRecipe::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, ItemCastingRecipe> STREAM_CODEC = StreamCodec.of(
            ItemCastingRecipe::toNetwork, ItemCastingRecipe::fromNetwork
    );

    public static final RecipeSerializer<ItemCastingRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    public final Optional<Ingredient> cast;
    public final SizedFluidIngredient fluid;
    public final ItemStackTemplate result;
    public final boolean consumeCast;

    public ItemCastingRecipe(Optional<Ingredient> cast, SizedFluidIngredient fluid, ItemStackTemplate result, boolean consumeCast) {
        this.cast = cast;
        this.fluid = fluid;
        this.result = result;
        this.consumeCast = consumeCast;
    }

    @Override
    public boolean matches(RecipeInput container, Level level) {
        return false;
    }

    @Override
    public boolean matches(ItemStack cast, FluidStack fluid, Level level) {
        return matches(cast, fluid, false, level);
    }

    @Override
    public boolean matches(ItemStack cast, FluidStack fluid, boolean matchFluidAmount, Level level) {
        boolean castMatches = this.cast.isEmpty() ? cast.isEmpty() : this.cast.get().test(cast);
        return castMatches && (matchFluidAmount ? this.fluid.test(fluid) : this.fluid.ingredient().test(fluid));
    }

    @Override
    public int getFluidAmount(Level level, FluidStack containedFluid) {
        return this.fluid.amount();
    }

    @Override
    public ItemStack assemble(RecipeInput input) {
        return this.result.create();
    }

    @Override
    public ItemStack getResultItem(Level level, FluidStack containedFluid) {
        return this.result.create();
    }

    @Override
    public RecipeSerializer<? extends ICastingRecipe> getSerializer() {
        return MetalworksRegistrator.ITEM_CASTING.get();
    }

    @Override
    public RecipeType<? extends ICastingRecipe> getType() {
        return MetalworksRegistrator.ITEM_CASTING_TYPE.get();
    }

    @Override
    public String group() {
        return "";
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    public static ItemCastingRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
        return new ItemCastingRecipe(buffer.readBoolean() ? Optional.of(Ingredient.CONTENTS_STREAM_CODEC.decode(buffer)) : Optional.empty(), SizedFluidIngredient.STREAM_CODEC.decode(buffer), ItemStackTemplate.STREAM_CODEC.decode(buffer), buffer.readBoolean());
    }

    public static void toNetwork(RegistryFriendlyByteBuf buffer, ItemCastingRecipe recipe) {
        buffer.writeBoolean(recipe.cast.isPresent());
        recipe.cast.ifPresent(c -> Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, c));
        SizedFluidIngredient.STREAM_CODEC.encode(buffer, recipe.fluid);
        ItemStackTemplate.STREAM_CODEC.encode(buffer, recipe.result);
        buffer.writeBoolean(recipe.consumeCast);
    }
}
