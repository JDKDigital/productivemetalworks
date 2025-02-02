package cy.jdkdigital.productivemetalworks.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class DummyRecipe extends SilentGearCastingRecipe {
    public DummyRecipe(boolean consumeCast) {
        super(null, null, 0, null, consumeCast);
    }

    public static class DummySerializer implements RecipeSerializer<DummyRecipe>
    {
        private static final MapCodec<DummyRecipe> CODEC = RecordCodecBuilder.mapCodec(
                builder -> builder.group(
                                Codec.BOOL.fieldOf("consume_cast").orElse(false).forGetter(recipe -> recipe.consumeCast)
                        )
                        .apply(builder, DummyRecipe::new)
        );

        public static final StreamCodec<RegistryFriendlyByteBuf, DummyRecipe> STREAM_CODEC = StreamCodec.of(
                DummySerializer::toNetwork, DummySerializer::fromNetwork
        );

        @Override
        public MapCodec<DummyRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, DummyRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        public static DummyRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
            return new DummyRecipe(buffer.readBoolean());
        }

        public static void toNetwork(RegistryFriendlyByteBuf buffer, DummyRecipe recipe) {
            buffer.writeBoolean(recipe.consumeCast);
        }
    }
}
