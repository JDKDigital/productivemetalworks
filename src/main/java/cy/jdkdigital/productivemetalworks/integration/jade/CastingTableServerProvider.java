package cy.jdkdigital.productivemetalworks.integration.jade;

import cy.jdkdigital.productivemetalworks.ProductiveMetalworks;
import cy.jdkdigital.productivemetalworks.common.block.entity.CastingBlockEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.StreamServerDataProvider;

// 26.1 (MC 1.21.6+): Jade forbids a data provider from also implementing IComponentProvider. The server-side
// data streaming lives here; the client tooltip lives in CastingTableClientProvider.
public class CastingTableServerProvider implements StreamServerDataProvider<BlockAccessor, CastingTableServerProvider.Data>
{
    public static final Identifier UID = Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, "casting");
    public static final CastingTableServerProvider INSTANCE = new CastingTableServerProvider();

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
    public Identifier getUid() {
        return UID;
    }

    public record Data(boolean isCooling) {
        public static final StreamCodec<RegistryFriendlyByteBuf, Data> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.BOOL,
                Data::isCooling,
                Data::new);
    }
}
