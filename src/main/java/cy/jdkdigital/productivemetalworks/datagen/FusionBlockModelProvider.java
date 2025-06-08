package cy.jdkdigital.productivemetalworks.datagen;

import com.supermartijn642.fusion.api.model.DefaultModelTypes;
import com.supermartijn642.fusion.api.model.ModelInstance;
import com.supermartijn642.fusion.api.model.data.ConnectingModelDataBuilder;
import com.supermartijn642.fusion.api.predicate.DefaultConnectionPredicates;
import com.supermartijn642.fusion.api.provider.FusionModelProvider;
import cy.jdkdigital.productivemetalworks.ProductiveMetalworks;
import cy.jdkdigital.productivemetalworks.registry.MetalworksRegistrator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.models.model.ModelLocationUtils;
import net.minecraft.data.models.model.TextureMapping;
import net.minecraft.data.models.model.TextureSlot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;

import java.nio.file.Path;

public class FusionBlockModelProvider extends FusionModelProvider
{
    public FusionBlockModelProvider(PackOutput output) {
        super(ProductiveMetalworks.MODID, new PackOutput(Path.of(output.getOutputFolder().toAbsolutePath().toString(), "fusion-overrides")));
    }

    @Override
    protected void generate() {
        for (DyeColor dyeColor : DyeColor.values()) {
            var windowBuilder = ConnectingModelDataBuilder.builder()
                    .parent(ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "block/foundry_window_base"))
                    .texture(TextureSlot.SIDE.getId(), TextureMapping.getBlockTexture(MetalworksRegistrator.FIRE_BRICKS.get(dyeColor).get()))
                    .texture(TextureSlot.TOP.getId(), TextureMapping.getBlockTexture(MetalworksRegistrator.FIRE_BRICKS.get(dyeColor).get()))
                    .texture(TextureSlot.FRONT.getId(), ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "block/" + dyeColor.getSerializedName() + "_foundry_window_front"));

            for (DyeColor dyeColor1 : DyeColor.values()) {
                windowBuilder = windowBuilder.connection(DefaultConnectionPredicates.matchBlock(MetalworksRegistrator.FOUNDRY_WINDOWS.get(dyeColor1).get()));
            }

            var windowModel = ModelInstance.of(DefaultModelTypes.CONNECTING, windowBuilder.build());
            this.addModel(ModelLocationUtils.getModelLocation(MetalworksRegistrator.FOUNDRY_WINDOWS.get(dyeColor).get(), ""), windowModel);


            var tankBuilder = ConnectingModelDataBuilder.builder()
                    .parent(ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "block/foundry_tank_base"))
                    .texture(TextureSlot.SIDE.getId(), TextureMapping.getBlockTexture(MetalworksRegistrator.FIRE_BRICKS.get(dyeColor).get()))
                    .texture(TextureSlot.TOP.getId(), TextureMapping.getBlockTexture(MetalworksRegistrator.FIRE_BRICKS.get(dyeColor).get()))
                    .texture(TextureSlot.FRONT.getId(), ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "block/" + dyeColor.getSerializedName() + "_foundry_tank_front"));

            for (DyeColor dyeColor1 : DyeColor.values()) {
                tankBuilder = tankBuilder.connection(DefaultConnectionPredicates.matchBlock(MetalworksRegistrator.FOUNDRY_TANKS.get(dyeColor1).get()));
            }

            var tankModel = ModelInstance.of(DefaultModelTypes.CONNECTING, tankBuilder.build());
            this.addModel(ModelLocationUtils.getModelLocation(MetalworksRegistrator.FOUNDRY_TANKS.get(dyeColor).get(), ""), tankModel);
        }
    }
}
