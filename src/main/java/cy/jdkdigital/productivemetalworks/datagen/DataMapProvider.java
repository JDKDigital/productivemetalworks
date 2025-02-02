package cy.jdkdigital.productivemetalworks.datagen;

import cy.jdkdigital.productivemetalworks.common.datamap.FuelMap;
import cy.jdkdigital.productivemetalworks.registry.MetalworksRegistrator;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.conditions.ModLoadedCondition;

import java.util.concurrent.CompletableFuture;

public class DataMapProvider extends net.neoforged.neoforge.common.data.DataMapProvider
{
    protected DataMapProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(packOutput, lookupProvider);
    }

    @Override
    protected void gather() {
        final var fuels = builder(MetalworksRegistrator.FUEL_MAP);

        fuels.add(Fluids.LAVA.builtInRegistryHolder(), new FuelMap(1500, 0.2f, 1.0f), false);
        fuels.add(ResourceLocation.fromNamespaceAndPath("allthemodium", "soul_lava"), new FuelMap(3000, 0.1f, 2.0f), false, new ModLoadedCondition("allthemodium"));
    }
}
