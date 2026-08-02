package cy.jdkdigital.productivemetalworks.common.datamap;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import cy.jdkdigital.productivemetalworks.util.FluidStackTemplate;

// 26.1: the fluid is stored as a deferred FluidStackTemplate so the entity-melting datamap can be generated at
// datagen time (Fluid.components() isn't bound during data generation). Call fluid().create() at runtime.
public record EntityMeltingMap(FluidStackTemplate fluid, float scale)
{
    public static final Codec<EntityMeltingMap> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                    FluidStackTemplate.CODEC.fieldOf("fluid").forGetter(EntityMeltingMap::fluid),
                    Codec.FLOAT.fieldOf("gui_scale").orElse(1.0f).forGetter(EntityMeltingMap::scale)
            )
            .apply(builder, EntityMeltingMap::new));
}
