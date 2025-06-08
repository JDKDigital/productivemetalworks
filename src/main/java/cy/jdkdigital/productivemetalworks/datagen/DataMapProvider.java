package cy.jdkdigital.productivemetalworks.datagen;

import cy.jdkdigital.productivemetalworks.ProductiveMetalworks;
import cy.jdkdigital.productivemetalworks.common.datamap.EntityMeltingMap;
import cy.jdkdigital.productivemetalworks.common.datamap.FuelMap;
import cy.jdkdigital.productivemetalworks.common.datamap.UnitMap;
import cy.jdkdigital.productivemetalworks.registry.MetalworksRegistrator;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.conditions.ModLoadedCondition;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class DataMapProvider extends net.neoforged.neoforge.common.data.DataMapProvider
{
    protected DataMapProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(packOutput, lookupProvider);
    }

    @Override
    protected void gather(HolderLookup.Provider provider) {
        final var fuels = builder(MetalworksRegistrator.FUEL_MAP);
        final var coils = builder(MetalworksRegistrator.POWER_COIL_MAP);
        final var entityMelting = builder(MetalworksRegistrator.ENTITY_MELTING_MAP);
        final var units = builder(MetalworksRegistrator.UNIT_MAP);

        fuels.add(Fluids.LAVA.builtInRegistryHolder(), new FuelMap(1500, 0.2f, 0.5f), false);
        fuels.add(ResourceLocation.fromNamespaceAndPath("allthemodium", "soul_lava"), new FuelMap(3000, 0.1f, 1.0f), false, new ModLoadedCondition("allthemodium"));

        coils.add(MetalworksRegistrator.POWERED_HEATING_COIL, new FuelMap(1500, 4000f, 0.5f), false);
        coils.add(MetalworksRegistrator.HIGH_POWERED_HEATING_COIL, new FuelMap(3000, 4000f, 1.0f), false);

        entityMelting.add(EntityType.PLAYER.builtInRegistryHolder(), new EntityMeltingMap(new FluidStack(MetalworksRegistrator.LIQUID_MEAT.get(), 10), 0.8f), false);

        var metalUnits = new UnitMap(List.of(
                new UnitMap.Unit(10, "nugget"),
                new UnitMap.Unit(90, "ingot"),
                new UnitMap.Unit(810, "block")
        ));

        ProductiveMetalworks.FLUIDS.getEntries().forEach(fluidHolder -> {
            if (fluidHolder.get().defaultFluidState().isSource()) {
                if (fluidHolder.is(MetalworksRegistrator.LIQUID_MEAT.getId())) {
                    units.add(fluidHolder, new UnitMap(List.of(new UnitMap.Unit(10, "nugget"), new UnitMap.Unit(90, "hunk"), new UnitMap.Unit(720, "block"))), false);
                } else if (fluidHolder.is(MetalworksRegistrator.MOLTEN_GLASS.getId())) {
                    units.add(fluidHolder, new UnitMap(List.of(new UnitMap.Unit(375, "pane"), new UnitMap.Unit(1000, "block"))), false);
                } else if (fluidHolder.is(MetalworksRegistrator.MOLTEN_WAX.getId())) {
                    units.add(fluidHolder, new UnitMap(List.of(new UnitMap.Unit(50, "pile"), new UnitMap.Unit(450, "block"))), false);
                } else if (fluidHolder.is(MetalworksRegistrator.MOLTEN_QUARTZ.getId()) || fluidHolder.is(MetalworksRegistrator.MOLTEN_AMETHYST.getId())) {
                    units.add(fluidHolder, new UnitMap(List.of(new UnitMap.Unit(100, "gem"), new UnitMap.Unit(400, "block"))), false);
                } else if (fluidHolder.is(MetalworksRegistrator.MOLTEN_GLOWSTONE.getId())) {
                    units.add(fluidHolder, new UnitMap(List.of(new UnitMap.Unit(100, "pile"), new UnitMap.Unit(400, "block"))), false);
                } else if (fluidHolder.is(MetalworksRegistrator.MOLTEN_ENDER.getId())) {
                    units.add(fluidHolder, new UnitMap(List.of(new UnitMap.Unit(100, "pearl"), new UnitMap.Unit(900, "block"))), false);
                } else if (fluidHolder.is(MetalworksRegistrator.MOLTEN_MAGMA_CREAM.getId())) {
                    units.add(fluidHolder, new UnitMap(List.of(new UnitMap.Unit(100, "ball"), new UnitMap.Unit(400, "block"))), false);
                } else if (fluidHolder.is(MetalworksRegistrator.MOLTEN_SLIME.getId())) {
                    units.add(fluidHolder, new UnitMap(List.of(new UnitMap.Unit(100, "ball"), new UnitMap.Unit(900, "block"))), false);
                } else if (fluidHolder.is(MetalworksRegistrator.MOLTEN_SHULKER_SHELL.getId())) {
                    units.add(fluidHolder, new UnitMap(List.of(new UnitMap.Unit(100, "shell"))), false);
                } else if (fluidHolder.is(MetalworksRegistrator.MOLTEN_ANCIENT_DEBRIS.getId())) {
                    units.add(fluidHolder, new UnitMap(List.of(new UnitMap.Unit(100, "scrap"))), false);
                } else if (fluidHolder.is(MetalworksRegistrator.MOLTEN_OBSIDIAN.getId())) {
                    units.add(fluidHolder, new UnitMap(List.of(new UnitMap.Unit(1000, "block"))), false);
                } else if (fluidHolder.is(MetalworksRegistrator.MOLTEN_BLAZE.getId())) {
                    units.add(fluidHolder, new UnitMap(List.of(new UnitMap.Unit(100, "pile"), new UnitMap.Unit(400, "rod"))), false);
                } else if (fluidHolder.is(MetalworksRegistrator.MOLTEN_CARBON.getId())) {
                    units.add(fluidHolder, new UnitMap(List.of(new UnitMap.Unit(100, "chunk"), new UnitMap.Unit(900, "block"))), false);
                } else if (fluidHolder.is(MetalworksRegistrator.MOLTEN_REDSTONE.getId())) {
                    units.add(fluidHolder, new UnitMap(List.of(new UnitMap.Unit(100, "pile"), new UnitMap.Unit(900, "block"))), false);
                } else if (fluidHolder.is(MetalworksRegistrator.MOLTEN_DIAMOND.getId()) || fluidHolder.is(MetalworksRegistrator.MOLTEN_EMERALD.getId()) || fluidHolder.is(MetalworksRegistrator.MOLTEN_LAPIS.getId())) {
                    units.add(fluidHolder, new UnitMap(List.of(new UnitMap.Unit(100, "gem"), new UnitMap.Unit(900, "block"))), false);
                } else {
                    units.add(fluidHolder, metalUnits, false);
                }
            }
        });
        units.add(ResourceLocation.parse("allthemodium:molten_allthemodium"), metalUnits, false, new ModLoadedCondition("allthemodium"));
        units.add(ResourceLocation.parse("allthemodium:molten_vibranium"), metalUnits, false, new ModLoadedCondition("allthemodium"));
        units.add(ResourceLocation.parse("allthemodium:molten_unobtainium"), metalUnits, false, new ModLoadedCondition("allthemodium"));
        units.add(ResourceLocation.parse("integrateddynamics:menril_resin"), new UnitMap(List.of(new UnitMap.Unit(100, "chunk"), new UnitMap.Unit(1000, "block"))), false, new ModLoadedCondition("integrateddynamics"));
    }
}
