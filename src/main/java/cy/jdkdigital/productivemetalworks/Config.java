package cy.jdkdigital.productivemetalworks;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
@EventBusSubscriber(modid = ProductiveMetalworks.MODID, bus = EventBusSubscriber.Bus.MOD)
public class Config
{
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.IntValue FOUNDRY_MAX_VOLUME = BUILDER
            .comment("Max internal volume of the foundry multiblock")
            .defineInRange("foundryMaxVolume", 200, 1, Integer.MAX_VALUE);

    private static final ModConfigSpec.IntValue FOUNDRY_MAX_CIRCUMFERENCE = BUILDER
            .comment("Max circumference of the foundry multiblock")
            .defineInRange("foundryMaxCircumference", 200, 1, Integer.MAX_VALUE);

    private static final ModConfigSpec.IntValue FOUNDRY_MAX_HEIGHT = BUILDER
            .comment("Max height of the foundry multiblock")
            .defineInRange("foundryMaxHeight", 20, 1, Integer.MAX_VALUE);

    private static final ModConfigSpec.IntValue FOUNDRY_FLUID_CAPACITY_PER_BLOCK_VOLUME = BUILDER
            .comment("Number of mb per block volume")
            .defineInRange("foundryFluidCapacityPerBlockVolume", 1000, 1, Integer.MAX_VALUE);

    private static final ModConfigSpec.BooleanValue FOUNDRY_DAMAGE_ENTITIES = BUILDER
            .comment("Should an active foundry damage entities inside it")
            .define("foundryDamageEntities", true);

    private static final ModConfigSpec.BooleanValue FOUNDRY_COLLECT_ITEMS = BUILDER
            .comment("Should an active foundry collect items dropped inside it")
            .define("foundryCollectItems", true);

    private static final ModConfigSpec.DoubleValue FOUNDRY_COOLING_MODIFIER = BUILDER
            .comment("The cooling time of cast items is the number of mb used in the recipe divided by this number")
            .defineInRange("foundryCoolingModifier", 4f, 1, Integer.MAX_VALUE);


    static final ModConfigSpec SPEC = BUILDER.build();

    public static int foundryMaxVolume;
    public static int foundryMaxCircumference;
    public static int foundryMaxHeight;
    public static int foundryFluidCapacityPerBlockVolume;
    public static boolean foundryDamageEntities;
    public static boolean foundryCollectItems;
    public static double foundryCoolingModifier;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        foundryMaxVolume = FOUNDRY_MAX_VOLUME.get();
        foundryMaxCircumference = FOUNDRY_MAX_CIRCUMFERENCE.get();
        foundryMaxHeight = FOUNDRY_MAX_HEIGHT.get();
        foundryFluidCapacityPerBlockVolume = FOUNDRY_FLUID_CAPACITY_PER_BLOCK_VOLUME.get();
        foundryDamageEntities = FOUNDRY_DAMAGE_ENTITIES.get();
        foundryCollectItems = FOUNDRY_COLLECT_ITEMS.get();
        foundryCoolingModifier = FOUNDRY_COOLING_MODIFIER.get();
    }
}
