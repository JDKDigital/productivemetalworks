package cy.jdkdigital.productivemetalworks.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import cy.jdkdigital.productivemetalworks.registry.MetalworksRegistrator;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;

public class EntityMeltingRecipe implements Recipe<RecipeInput>
{
    public final ResourceLocation entity;
    public final List<FluidStack> result;

    public EntityMeltingRecipe(ResourceLocation entity, List<FluidStack> result) {
        this.entity = entity;
        this.result = result;
    }

    @Override
    public boolean matches(RecipeInput container, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(RecipeInput input, HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
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
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return MetalworksRegistrator.ENTITY_MELTING.get();
    }

    @Override
    public RecipeType<?> getType() {
        return MetalworksRegistrator.ENTITY_MELTING_TYPE.get();
    }

    public static class Serializer implements RecipeSerializer<EntityMeltingRecipe>
    {
        private static final MapCodec<EntityMeltingRecipe> CODEC = RecordCodecBuilder.mapCodec(
                builder -> builder.group(
                                ResourceLocation.CODEC.fieldOf("entity").forGetter(recipe -> recipe.entity),
                                FluidStack.CODEC.listOf().fieldOf("result").forGetter(recipe -> recipe.result)
                        )
                        .apply(builder, EntityMeltingRecipe::new)
        );

        public static final StreamCodec<RegistryFriendlyByteBuf, EntityMeltingRecipe> STREAM_CODEC = StreamCodec.of(
                EntityMeltingRecipe.Serializer::toNetwork, EntityMeltingRecipe.Serializer::fromNetwork
        );

        @Override
        public MapCodec<EntityMeltingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, EntityMeltingRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        public static EntityMeltingRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
            return new EntityMeltingRecipe(
                    ResourceLocation.STREAM_CODEC.decode(buffer),
                    FluidStack.STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buffer)
            );
        }

        public static void toNetwork(RegistryFriendlyByteBuf buffer, EntityMeltingRecipe recipe) {
            ResourceLocation.STREAM_CODEC.encode(buffer, recipe.entity);
            FluidStack.STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buffer, recipe.result);
        }
    }
}
