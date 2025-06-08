package cy.jdkdigital.productivemetalworks.common.datamap;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.neoforged.neoforge.fluids.FluidStack;

public record EntityMeltingMap(FluidStack fluid, float scale)
{
    public static final Codec<EntityMeltingMap> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                    FluidStack.CODEC.fieldOf("fluid").forGetter(EntityMeltingMap::fluid),
                    Codec.FLOAT.fieldOf("gui_scale").orElse(1.0f).forGetter(EntityMeltingMap::scale)
            )
            .apply(builder, EntityMeltingMap::new));
}
