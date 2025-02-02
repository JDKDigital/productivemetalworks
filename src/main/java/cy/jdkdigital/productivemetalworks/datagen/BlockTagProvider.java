package cy.jdkdigital.productivemetalworks.datagen;

import cy.jdkdigital.productivemetalworks.ProductiveMetalworks;
import cy.jdkdigital.productivemetalworks.registry.MetalworksRegistrator;
import cy.jdkdigital.productivemetalworks.registry.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

public class BlockTagProvider extends BlockTagsProvider
{
    public BlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider, ExistingFileHelper helper) {
        super(output, provider, ProductiveMetalworks.MODID, helper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        var MINEABLE_PICKAXE = tag(BlockTags.MINEABLE_WITH_PICKAXE);
        var WALL_BLOCKS = tag(ModTags.Blocks.FOUNDRY_WALL_BLOCKS);

        MetalworksRegistrator.FOUNDRY_CONTROLLERS.forEach((dyeColor, holder) -> tag(ModTags.Blocks.FOUNDRY_CONTROLLERS).add(holder.get()));
        MetalworksRegistrator.FOUNDRY_DRAINS.forEach((dyeColor, holder) -> tag(ModTags.Blocks.FOUNDRY_DRAINS).add(holder.get()));
        MetalworksRegistrator.FOUNDRY_TANKS.forEach((dyeColor, holder) -> tag(ModTags.Blocks.FOUNDRY_TANKS).add(holder.get()));
        MetalworksRegistrator.FOUNDRY_WINDOWS.forEach((dyeColor, holder) -> tag(ModTags.Blocks.FOUNDRY_WINDOWS).add(holder.get()));
        MetalworksRegistrator.FIRE_BRICKS.forEach((dyeColor, holder) -> tag(ModTags.Blocks.FIRE_BRICKS).add(holder.get()));

        WALL_BLOCKS
                .addTag(ModTags.Blocks.FOUNDRY_CONTROLLERS)
                .addTag(ModTags.Blocks.FOUNDRY_DRAINS)
                .addTag(ModTags.Blocks.FOUNDRY_TANKS)
                .addTag(ModTags.Blocks.FOUNDRY_WINDOWS)
                .addTag(ModTags.Blocks.FIRE_BRICKS)
                .add(MetalworksRegistrator.MEAT_BLOCK.get());

        tag(ModTags.Blocks.HEATING_COILS).add(
                MetalworksRegistrator.LIQUID_HEATING_COIL.get(),
                MetalworksRegistrator.POWERED_HEATING_COIL.get()
        );
        tag(ModTags.Blocks.FOUNDRY_BOTTOM_BLOCKS).addTag(ModTags.Blocks.HEATING_COILS);

        MINEABLE_PICKAXE.add(
                MetalworksRegistrator.FOUNDRY_TAP.get(),
                MetalworksRegistrator.CASTING_BASIN.get(),
                MetalworksRegistrator.CASTING_TABLE.get(),
                MetalworksRegistrator.LIQUID_HEATING_COIL.get(),
                MetalworksRegistrator.POWERED_HEATING_COIL.get()
        )
                .addTag(ModTags.Blocks.FOUNDRY_CONTROLLERS)
                .addTag(ModTags.Blocks.FOUNDRY_DRAINS)
                .addTag(ModTags.Blocks.FOUNDRY_TANKS)
                .addTag(ModTags.Blocks.FOUNDRY_WINDOWS)
                .addTag(ModTags.Blocks.FIRE_BRICKS);
    }

    @Override
    public String getName() {
        return "Productive Metalworks Block Tags Provider";
    }
}
