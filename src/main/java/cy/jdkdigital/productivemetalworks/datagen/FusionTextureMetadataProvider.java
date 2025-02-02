package cy.jdkdigital.productivemetalworks.datagen;

import com.supermartijn642.fusion.api.texture.DefaultTextureTypes;
import com.supermartijn642.fusion.api.texture.data.BaseTextureData;
import com.supermartijn642.fusion.api.texture.data.ConnectingTextureData;
import com.supermartijn642.fusion.api.texture.data.ConnectingTextureLayout;
import cy.jdkdigital.productivemetalworks.ProductiveMetalworks;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;

import java.nio.file.Path;

public class FusionTextureMetadataProvider extends com.supermartijn642.fusion.api.provider.FusionTextureMetadataProvider
{
    public FusionTextureMetadataProvider(PackOutput output) {
        super(ProductiveMetalworks.MODID, new PackOutput(Path.of(output.getOutputFolder().toAbsolutePath().toString(), "fusion-overrides")));
    }

    @Override
    protected void generate() {
        for (DyeColor dyeColor : DyeColor.values()) {
            var windowTextureData = ConnectingTextureData.builder()
                    .renderType(BaseTextureData.RenderType.CUTOUT)
                    .build();

            this.addTextureMetadata(
                    ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "block/" + dyeColor.getSerializedName() + "_foundry_window_front"),
                    DefaultTextureTypes.CONNECTING,
                    windowTextureData
            );

            var tankTextureData = ConnectingTextureData.builder()
                    .layout(ConnectingTextureLayout.VERTICAL)
                    .renderType(BaseTextureData.RenderType.CUTOUT)
                    .build();

            this.addTextureMetadata(
                    ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "block/" + dyeColor.getSerializedName() + "_foundry_tank_front"),
                    DefaultTextureTypes.CONNECTING,
                    tankTextureData
            );
        }
    }
}
