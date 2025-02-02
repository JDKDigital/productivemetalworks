package cy.jdkdigital.productivemetalworks.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import cy.jdkdigital.productivemetalworks.registry.MetalworksRegistrator;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

public class ItemCastingRecipe implements ICastingRecipe
{
    public final Ingredient cast;
    public final SizedFluidIngredient fluid;
    public final ItemStack result;
    public final boolean consumeCast;

    public ItemCastingRecipe(Ingredient cast, SizedFluidIngredient fluid, ItemStack result, boolean consumeCast) {
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
        return this.cast.test(cast) && (matchFluidAmount ? this.fluid.test(fluid) : this.fluid.ingredient().test(fluid));
    }

    @Override
    public int getFluidAmount(Level level, FluidStack containedFluid) {
        return this.fluid.amount();
    }

    @Override
    public ItemStack assemble(RecipeInput input, HolderLookup.Provider registries) {
        return this.result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public ItemStack getResultItem(Level level, FluidStack containedFluid) {
        return getResultItem(level.registryAccess());
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return this.result.copy();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return MetalworksRegistrator.ITEM_CASTING.get();
    }

    @Override
    public RecipeType<?> getType() {
        return MetalworksRegistrator.ITEM_CASTING_TYPE.get();
    }

    public static class Serializer implements RecipeSerializer<ItemCastingRecipe>
    {
        private static final MapCodec<ItemCastingRecipe> CODEC = RecordCodecBuilder.mapCodec(
                builder -> builder.group(
                                Ingredient.CODEC.fieldOf("cast").orElse(Ingredient.EMPTY).forGetter(recipe -> recipe.cast),
                                SizedFluidIngredient.FLAT_CODEC.fieldOf("fluid").forGetter(recipe -> recipe.fluid),
                                ItemStack.CODEC.fieldOf("result").forGetter(recipe -> recipe.result),
                                Codec.BOOL.fieldOf("consume_cast").orElse(false).forGetter(recipe -> recipe.consumeCast)
                        )
                        .apply(builder, ItemCastingRecipe::new)
        );

        public static final StreamCodec<RegistryFriendlyByteBuf, ItemCastingRecipe> STREAM_CODEC = StreamCodec.of(
                ItemCastingRecipe.Serializer::toNetwork, ItemCastingRecipe.Serializer::fromNetwork
        );

        @Override
        public MapCodec<ItemCastingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ItemCastingRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        public static ItemCastingRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
            return new ItemCastingRecipe(Ingredient.CONTENTS_STREAM_CODEC.decode(buffer), SizedFluidIngredient.STREAM_CODEC.decode(buffer), ItemStack.STREAM_CODEC.decode(buffer), buffer.readBoolean());
        }

        public static void toNetwork(RegistryFriendlyByteBuf buffer, ItemCastingRecipe recipe) {
            Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.cast);
            SizedFluidIngredient.STREAM_CODEC.encode(buffer, recipe.fluid);
            ItemStack.STREAM_CODEC.encode(buffer, recipe.result);
            buffer.writeBoolean(recipe.consumeCast);
        }
    }
}
