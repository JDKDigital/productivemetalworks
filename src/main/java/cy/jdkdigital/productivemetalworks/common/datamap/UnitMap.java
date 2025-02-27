package cy.jdkdigital.productivemetalworks.common.datamap;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public record UnitMap(List<Unit> units)
{
    public static final Codec<UnitMap> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                Unit.CODEC.listOf().fieldOf("units").forGetter(UnitMap::units)
            )
            .apply(builder, UnitMap::new));

    public record Unit(int amount, String unit)
    {
        public static final Codec<Unit> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                        Codec.INT.fieldOf("amount").forGetter(Unit::amount),
                        Codec.STRING.fieldOf("unit").forGetter(Unit::unit)
                )
                .apply(builder, Unit::new));
    }
}
