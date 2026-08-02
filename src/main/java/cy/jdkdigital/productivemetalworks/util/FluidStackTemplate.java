package cy.jdkdigital.productivemetalworks.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * 26.1 datagen helper: a deferred {@link FluidStack}. Building a FluidStack at datagen time throws
 * "Components not bound yet" because {@code Fluid.components()} isn't bound during data generation. This
 * record holds the fluid holder + amount + component patch (no component materialization) and serializes
 * to the exact same JSON as {@link FluidStack#CODEC} ({@code {id, amount, components?}}), so shipped recipe
 * JSON still loads. {@link #create()} materializes the real FluidStack at runtime (when components are bound).
 */
public record FluidStackTemplate(Holder<Fluid> fluid, int amount, DataComponentPatch components)
{
    public static final Codec<FluidStackTemplate> CODEC = RecordCodecBuilder.create(i -> i.group(
            BuiltInRegistries.FLUID.holderByNameCodec().fieldOf("id").forGetter(FluidStackTemplate::fluid),
            ExtraCodecs.POSITIVE_INT.fieldOf("amount").forGetter(FluidStackTemplate::amount),
            DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(FluidStackTemplate::components)
    ).apply(i, FluidStackTemplate::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, FluidStackTemplate> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.holderRegistry(Registries.FLUID), FluidStackTemplate::fluid,
            ByteBufCodecs.VAR_INT, FluidStackTemplate::amount,
            DataComponentPatch.STREAM_CODEC, FluidStackTemplate::components,
            FluidStackTemplate::new
    );

    public FluidStackTemplate(Fluid fluid, int amount) {
        this(fluid.builtInRegistryHolder(), amount, DataComponentPatch.EMPTY);
    }

    public FluidStackTemplate(Holder<Fluid> fluid, int amount) {
        this(fluid, amount, DataComponentPatch.EMPTY);
    }

    public static FluidStackTemplate of(FluidStack stack) {
        return new FluidStackTemplate(stack.typeHolder(), stack.getAmount(), stack.getComponentsPatch());
    }

    public int getAmount() {
        return amount;
    }

    public Fluid getFluid() {
        return fluid.value();
    }

    public FluidStack create() {
        return new FluidStack(fluid, amount, components);
    }
}
