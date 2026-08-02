package cy.jdkdigital.productivemetalworks.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import cy.jdkdigital.productivemetalworks.registry.MetalworksRegistrator;
import cy.jdkdigital.productivemetalworks.util.FluidStackTemplate;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import java.util.List;

public class EntityMeltingRecipe implements Recipe<RecipeInput>
{
    public static final MapCodec<EntityMeltingRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(
            builder -> builder.group(
                            Identifier.CODEC.fieldOf("entity").forGetter(recipe -> recipe.entity),
                            FluidStackTemplate.CODEC.listOf().fieldOf("result").forGetter(recipe -> recipe.result)
                    )
                    .apply(builder, EntityMeltingRecipe::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, EntityMeltingRecipe> STREAM_CODEC = StreamCodec.of(
            EntityMeltingRecipe::toNetwork, EntityMeltingRecipe::fromNetwork
    );

    public static final RecipeSerializer<EntityMeltingRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    public final Identifier entity;
    public final List<FluidStackTemplate> result;

    public EntityMeltingRecipe(Identifier entity, List<FluidStackTemplate> result) {
        this.entity = entity;
        this.result = result;
    }

    @Override
    public boolean matches(RecipeInput container, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(RecipeInput input) {
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<EntityMeltingRecipe> getSerializer() {
        return MetalworksRegistrator.ENTITY_MELTING.get();
    }

    @Override
    public RecipeType<EntityMeltingRecipe> getType() {
        return MetalworksRegistrator.ENTITY_MELTING_TYPE.get();
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

    public static EntityMeltingRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
        return new EntityMeltingRecipe(
                Identifier.STREAM_CODEC.decode(buffer),
                FluidStackTemplate.STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buffer)
        );
    }

    public static void toNetwork(RegistryFriendlyByteBuf buffer, EntityMeltingRecipe recipe) {
        Identifier.STREAM_CODEC.encode(buffer, recipe.entity);
        FluidStackTemplate.STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buffer, recipe.result);
    }
}
