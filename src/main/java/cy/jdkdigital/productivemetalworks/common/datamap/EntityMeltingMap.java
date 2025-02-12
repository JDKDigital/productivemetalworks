package cy.jdkdigital.productivemetalworks.common.datamap;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.neoforged.neoforge.fluids.FluidStack;

public record EntityMeltingMap(FluidStack fluid)
{
    public static final Codec<EntityMeltingMap> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                    FluidStack.CODEC.fieldOf("fluid").forGetter(EntityMeltingMap::fluid)
            )
            .apply(builder, EntityMeltingMap::new));
}
