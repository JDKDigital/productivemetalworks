package cy.jdkdigital.productivemetalworks.common.datamap;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record FuelMap(int temperature, float consumption, float speed)
{
    public static final Codec<FuelMap> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                    Codec.INT.fieldOf("temperature").forGetter(FuelMap::temperature),
                    Codec.FLOAT.fieldOf("consumption").forGetter(FuelMap::consumption),
                    Codec.FLOAT.fieldOf("speed").forGetter(FuelMap::speed)
            )
            .apply(builder, FuelMap::new));
}
