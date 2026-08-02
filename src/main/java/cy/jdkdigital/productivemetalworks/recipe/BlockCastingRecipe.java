package cy.jdkdigital.productivemetalworks.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import cy.jdkdigital.productivemetalworks.registry.MetalworksRegistrator;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

public class BlockCastingRecipe extends ItemCastingRecipe
{
    public static final MapCodec<BlockCastingRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(
            builder -> builder.group(
                            Ingredient.CODEC.optionalFieldOf("cast").forGetter(recipe -> recipe.cast),
                            SizedFluidIngredient.CODEC.fieldOf("fluid").forGetter(recipe -> recipe.fluid),
                            ItemStackTemplate.CODEC.fieldOf("result").forGetter(recipe -> recipe.result),
                            Codec.BOOL.fieldOf("consume_cast").orElse(false).forGetter(recipe -> recipe.consumeCast)
                    )
                    .apply(builder, BlockCastingRecipe::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, BlockCastingRecipe> STREAM_CODEC = StreamCodec.of(
            BlockCastingRecipe::toNetwork, BlockCastingRecipe::fromNetwork
    );

    public static final RecipeSerializer<BlockCastingRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    public BlockCastingRecipe(java.util.Optional<Ingredient> cast, SizedFluidIngredient fluid, ItemStackTemplate result, boolean consumeCast) {
        super(cast, fluid, result, consumeCast);
    }

    @Override
    public RecipeSerializer<? extends ICastingRecipe> getSerializer() {
        return MetalworksRegistrator.BLOCK_CASTING.get();
    }

    @Override
    public RecipeType<? extends ICastingRecipe> getType() {
        return MetalworksRegistrator.BLOCK_CASTING_TYPE.get();
    }

    public static BlockCastingRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
        return new BlockCastingRecipe(buffer.readBoolean() ? java.util.Optional.of(Ingredient.CONTENTS_STREAM_CODEC.decode(buffer)) : java.util.Optional.empty(), SizedFluidIngredient.STREAM_CODEC.decode(buffer), ItemStackTemplate.STREAM_CODEC.decode(buffer), buffer.readBoolean());
    }

    public static void toNetwork(RegistryFriendlyByteBuf buffer, BlockCastingRecipe recipe) {
        buffer.writeBoolean(recipe.cast.isPresent());
        recipe.cast.ifPresent(c -> Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, c));
        SizedFluidIngredient.STREAM_CODEC.encode(buffer, recipe.fluid);
        ItemStackTemplate.STREAM_CODEC.encode(buffer, recipe.result);
        buffer.writeBoolean(recipe.consumeCast);
    }
}
