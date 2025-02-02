package cy.jdkdigital.productivemetalworks.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import cy.jdkdigital.productivemetalworks.registry.MetalworksRegistrator;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

public class BlockCastingRecipe extends ItemCastingRecipe
{
    public BlockCastingRecipe(Ingredient cast, SizedFluidIngredient fluid, ItemStack result, boolean consumeCast) {
        super(cast, fluid, result, consumeCast);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return MetalworksRegistrator.BLOCK_CASTING.get();
    }

    @Override
    public RecipeType<?> getType() {
        return MetalworksRegistrator.BLOCK_CASTING_TYPE.get();
    }

    public static class Serializer implements RecipeSerializer<BlockCastingRecipe>
    {
        private static final MapCodec<BlockCastingRecipe> CODEC = RecordCodecBuilder.mapCodec(
                builder -> builder.group(
                                Ingredient.CODEC.fieldOf("cast").orElse(Ingredient.EMPTY).forGetter(recipe -> recipe.cast),
                                SizedFluidIngredient.FLAT_CODEC.fieldOf("fluid").forGetter(recipe -> recipe.fluid),
                                ItemStack.CODEC.fieldOf("result").forGetter(recipe -> recipe.result),
                                Codec.BOOL.fieldOf("consume_cast").orElse(false).forGetter(recipe -> recipe.consumeCast)
                        )
                        .apply(builder, BlockCastingRecipe::new)
        );

        public static final StreamCodec<RegistryFriendlyByteBuf, BlockCastingRecipe> STREAM_CODEC = StreamCodec.of(
                BlockCastingRecipe.Serializer::toNetwork, BlockCastingRecipe.Serializer::fromNetwork
        );

        @Override
        public MapCodec<BlockCastingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, BlockCastingRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        public static BlockCastingRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
            return new BlockCastingRecipe(Ingredient.CONTENTS_STREAM_CODEC.decode(buffer), SizedFluidIngredient.STREAM_CODEC.decode(buffer), ItemStack.STREAM_CODEC.decode(buffer), buffer.readBoolean());
        }

        public static void toNetwork(RegistryFriendlyByteBuf buffer, BlockCastingRecipe recipe) {
            Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.cast);
            SizedFluidIngredient.STREAM_CODEC.encode(buffer, recipe.fluid);
            ItemStack.STREAM_CODEC.encode(buffer, recipe.result);
            buffer.writeBoolean(recipe.consumeCast);
        }
    }
}
