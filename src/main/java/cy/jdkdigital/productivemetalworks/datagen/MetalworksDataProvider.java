package cy.jdkdigital.productivemetalworks.datagen;

import cy.jdkdigital.productivemetalworks.ProductiveMetalworks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = ProductiveMetalworks.MODID)
public class MetalworksDataProvider
{
    // 26.1: GatherDataEvent split into .Client / .Server; ExistingFileHelper was removed. Everything runs from
    // the client data event (matching the productivebees 26.1 port). patchouli / fusion / clayworks compat was
    // dropped (no 26.1.2 builds), so the GuideBook + Fusion providers are gone.
    @SubscribeEvent
    public static void gatherData(GatherDataEvent.Client event) {
        DataGenerator gen = event.getGenerator();
        PackOutput output = gen.getPackOutput();
        CompletableFuture<HolderLookup.Provider> provider = event.getLookupProvider();

        gen.addProvider(true, new LanguageProvider(output, "en_us"));

        gen.addProvider(true, new BlockModelProvider(output));

        gen.addProvider(true, new LootDataProvider(output, List.of(new LootTableProvider.SubProviderEntry(LootDataProvider.LootProvider::new, LootContextParamSets.BLOCK)), provider));
        gen.addProvider(true, new RecipeProvider.Runner(output, provider));

        BlockTagProvider blockTags = new BlockTagProvider(output, provider);
        gen.addProvider(true, blockTags);
        gen.addProvider(true, new ItemTagProvider(output, provider, blockTags.contentsGetter()));
        gen.addProvider(true, new FluidTagProvider(output, provider));
        gen.addProvider(true, new LootModifierProvider(output, provider));
        gen.addProvider(true, new DataMapProvider(output, provider));

        // GameTest playground structure + one test_instance/test_environment JSON per registered test.
        gen.addProvider(true, new cy.jdkdigital.productivemetalworks.gametest.GameTestStructureProvider(output));
        gen.addProvider(true, new cy.jdkdigital.productivemetalworks.gametest.TestEntriesProvider(output));
    }
}
