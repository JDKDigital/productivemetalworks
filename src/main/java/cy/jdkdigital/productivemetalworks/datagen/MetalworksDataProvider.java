package cy.jdkdigital.productivemetalworks.datagen;

import cy.jdkdigital.productivemetalworks.ProductiveMetalworks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = ProductiveMetalworks.MODID, bus = EventBusSubscriber.Bus.MOD)
public class MetalworksDataProvider
{
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        if (event.getModContainer().getModId().equals(ProductiveMetalworks.MODID)) {
            Data.gatherData(event);
        }
    }

    static class Data
    {
        private static void gatherData(GatherDataEvent event) {
            DataGenerator gen = event.getGenerator();
            PackOutput output = event.getGenerator().getPackOutput();
            CompletableFuture<HolderLookup.Provider> provider = event.getLookupProvider();
            ExistingFileHelper helper = event.getExistingFileHelper();

            gen.addProvider(event.includeClient(), new LanguageProvider(output, "en_us"));

            gen.addProvider(event.includeClient(), new BlockModelProvider(output));
            gen.addProvider(event.includeClient(), new ItemModelProvider(output, helper));

            gen.addProvider(event.includeServer(), new LootDataProvider(output, List.of(new LootTableProvider.SubProviderEntry(LootDataProvider.LootProvider::new, LootContextParamSets.BLOCK)), provider));
            gen.addProvider(event.includeServer(), new RecipeProvider(output, provider));

            BlockTagProvider blockTags = new BlockTagProvider(output, provider, helper);
            gen.addProvider(event.includeServer(), blockTags);
            gen.addProvider(event.includeServer(), new ItemTagProvider(output, provider, blockTags.contentsGetter(), helper));
            gen.addProvider(event.includeServer(), new FluidTagProvider(output, provider, helper));
            gen.addProvider(event.includeServer(), new LootModifierProvider(output, provider));
            gen.addProvider(event.includeServer(), new DataMapProvider(output, provider));

            gen.addProvider(true, new GuideBookProvider(output, "en_us", provider));
            gen.addProvider(event.includeClient(), new FusionBlockModelProvider(output));
            gen.addProvider(event.includeClient(), new FusionTextureMetadataProvider(output));
        }
    }
}
