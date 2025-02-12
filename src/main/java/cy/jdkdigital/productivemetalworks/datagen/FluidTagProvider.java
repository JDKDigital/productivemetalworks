package cy.jdkdigital.productivemetalworks.datagen;

import cy.jdkdigital.productivemetalworks.ProductiveMetalworks;
import cy.jdkdigital.productivemetalworks.registry.MetalworksRegistrator;
import cy.jdkdigital.productivemetalworks.registry.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.FluidTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

public class FluidTagProvider extends FluidTagsProvider
{
    public FluidTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> future, ExistingFileHelper helper) {
        super(output, future, ProductiveMetalworks.MODID, helper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(ModTags.Fluids.MEAT).add(MetalworksRegistrator.LIQUID_MEAT.get());
        tag(ModTags.Fluids.MOLTEN_WAX).add(MetalworksRegistrator.MOLTEN_WAX.get());
        tag(ModTags.Fluids.MOLTEN_ENDER).add(MetalworksRegistrator.MOLTEN_ENDER.get());

        tag(ModTags.Fluids.MOLTEN_AMETHYST).add(MetalworksRegistrator.MOLTEN_AMETHYST.get());
        tag(ModTags.Fluids.MOLTEN_GLOWSTONE).add(MetalworksRegistrator.MOLTEN_GLOWSTONE.get());
        tag(ModTags.Fluids.MOLTEN_REDSTONE).add(MetalworksRegistrator.MOLTEN_REDSTONE.get());
        tag(ModTags.Fluids.MOLTEN_OBSIDIAN).add(MetalworksRegistrator.MOLTEN_OBSIDIAN.get());
        tag(ModTags.Fluids.MOLTEN_GLASS).add(MetalworksRegistrator.MOLTEN_GLASS.get());
        tag(ModTags.Fluids.MOLTEN_EMERALD).add(MetalworksRegistrator.MOLTEN_EMERALD.get());
        tag(ModTags.Fluids.MOLTEN_DIAMOND).add(MetalworksRegistrator.MOLTEN_DIAMOND.get());
        tag(ModTags.Fluids.MOLTEN_LAPIS).add(MetalworksRegistrator.MOLTEN_LAPIS.get());
        tag(ModTags.Fluids.MOLTEN_QUARTZ).add(MetalworksRegistrator.MOLTEN_QUARTZ.get());
        tag(ModTags.Fluids.MOLTEN_CARBON).add(MetalworksRegistrator.MOLTEN_CARBON.get());
        tag(ModTags.Fluids.MOLTEN_ENDER).add(MetalworksRegistrator.MOLTEN_ENDER.get());
        tag(ModTags.Fluids.MOLTEN_ANCIENT_DEBRIS).add(MetalworksRegistrator.MOLTEN_ANCIENT_DEBRIS.get());
        tag(ModTags.Fluids.MOLTEN_SHULKER_SHELL).add(MetalworksRegistrator.MOLTEN_SHULKER_SHELL.get());
        tag(ModTags.Fluids.MOLTEN_BLAZE).add(MetalworksRegistrator.MOLTEN_BLAZE.get());
        tag(ModTags.Fluids.MOLTEN_SLIME).add(MetalworksRegistrator.MOLTEN_SLIME.get());
        tag(ModTags.Fluids.MOLTEN_MAGMA_CREAM).add(MetalworksRegistrator.MOLTEN_MAGMA_CREAM.get());

        tag(ModTags.Fluids.MOLTEN_IRON).add(MetalworksRegistrator.MOLTEN_IRON.get());
        tag(ModTags.Fluids.MOLTEN_COPPER).add(MetalworksRegistrator.MOLTEN_COPPER.get());
        tag(ModTags.Fluids.MOLTEN_GOLD).add(MetalworksRegistrator.MOLTEN_GOLD.get());
        tag(ModTags.Fluids.MOLTEN_NETHERITE).add(MetalworksRegistrator.MOLTEN_NETHERITE.get());
        
        tag(ModTags.Fluids.MOLTEN_ALUMINUM).add(MetalworksRegistrator.MOLTEN_ALUMINUM.get());
        tag(ModTags.Fluids.MOLTEN_LEAD).add(MetalworksRegistrator.MOLTEN_LEAD.get());
        tag(ModTags.Fluids.MOLTEN_NICKEL).add(MetalworksRegistrator.MOLTEN_NICKEL.get());
        tag(ModTags.Fluids.MOLTEN_OSMIUM).add(MetalworksRegistrator.MOLTEN_OSMIUM.get());
        tag(ModTags.Fluids.MOLTEN_PLATINUM).add(MetalworksRegistrator.MOLTEN_PLATINUM.get());
        tag(ModTags.Fluids.MOLTEN_SILVER).add(MetalworksRegistrator.MOLTEN_SILVER.get());
        tag(ModTags.Fluids.MOLTEN_TIN).add(MetalworksRegistrator.MOLTEN_TIN.get());
        tag(ModTags.Fluids.MOLTEN_URANIUM).add(MetalworksRegistrator.MOLTEN_URANIUM.get());
        tag(ModTags.Fluids.MOLTEN_ZINC).add(MetalworksRegistrator.MOLTEN_ZINC.get());
        tag(ModTags.Fluids.MOLTEN_IRIDIUM).add(MetalworksRegistrator.MOLTEN_IRIDIUM.get());

        tag(ModTags.Fluids.MOLTEN_STEEL).add(MetalworksRegistrator.MOLTEN_STEEL.get());
        tag(ModTags.Fluids.MOLTEN_INVAR).add(MetalworksRegistrator.MOLTEN_INVAR.get());
        tag(ModTags.Fluids.MOLTEN_ELECTRUM).add(MetalworksRegistrator.MOLTEN_ELECTRUM.get());
        tag(ModTags.Fluids.MOLTEN_BRONZE).add(MetalworksRegistrator.MOLTEN_BRONZE.get());
        tag(ModTags.Fluids.MOLTEN_BRASS).add(MetalworksRegistrator.MOLTEN_BRASS.get());
        tag(ModTags.Fluids.MOLTEN_ENDERIUM).add(MetalworksRegistrator.MOLTEN_ENDERIUM.get());
        tag(ModTags.Fluids.MOLTEN_LUMIUM).add(MetalworksRegistrator.MOLTEN_LUMIUM.get());
        tag(ModTags.Fluids.MOLTEN_SIGNALUM).add(MetalworksRegistrator.MOLTEN_SIGNALUM.get());
        tag(ModTags.Fluids.MOLTEN_CONSTANTAN).add(MetalworksRegistrator.MOLTEN_CONSTANTAN.get());
        tag(ModTags.Fluids.MOLTEN_REFINED_GLOWSTONE).add(MetalworksRegistrator.MOLTEN_REFINED_GLOWSTONE.get());
//        tag(ModTags.Fluids.MOLTEN_REFINED_OBSIDIAN).add(MetalworksRegistrator.MOLTEN_REFINED_OBSIDIAN.get());
    }

    @Override
    public String getName() {
        return "Productive Metalworks Fluid Tags Provider";
    }
}
