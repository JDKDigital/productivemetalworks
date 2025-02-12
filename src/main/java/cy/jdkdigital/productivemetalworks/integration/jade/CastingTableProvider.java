package cy.jdkdigital.productivemetalworks.integration.jade;

import cy.jdkdigital.productivemetalworks.ProductiveMetalworks;
import cy.jdkdigital.productivemetalworks.common.block.entity.CastingBlockEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.StreamServerDataProvider;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElementHelper;

public class CastingTableProvider implements IBlockComponentProvider, StreamServerDataProvider<BlockAccessor, CastingTableProvider.Data>
{
    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting");
    static final CastingTableProvider INSTANCE = new CastingTableProvider();

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        Data data = decodeFromData(accessor).orElse(null);
        if (data == null) {
            return;
        }
        IElementHelper helper = IElementHelper.get();
        if (data.isCooling()) {
            tooltip.add(Component.translatable("jade." + ProductiveMetalworks.MODID + ".cooling"));
        }
    }

    @Override
    public Data streamData(BlockAccessor accessor) {
        CastingBlockEntity access = (CastingBlockEntity) accessor.getBlockEntity();
        return new Data(access.isCooling());
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, Data> streamCodec() {
        return Data.STREAM_CODEC;
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    public record Data(boolean isCooling) {
        public static final StreamCodec<RegistryFriendlyByteBuf, Data> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.BOOL,
                Data::isCooling,
                Data::new);
    }
}