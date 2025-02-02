package cy.jdkdigital.productivemetalworks.network;

import cy.jdkdigital.productivemetalworks.ProductiveMetalworks;
import cy.jdkdigital.productivemetalworks.common.block.entity.FoundryControllerBlockEntity;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record MoveFoundryFluidData(BlockPos pos, int tank) implements CustomPacketPayload
{
    public static final Type<MoveFoundryFluidData> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "move_foundry_fluid_data"));

    public static final StreamCodec<ByteBuf, MoveFoundryFluidData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.fromCodec(BlockPos.CODEC),
            MoveFoundryFluidData::pos,
            ByteBufCodecs.VAR_INT,
            MoveFoundryFluidData::tank,
            MoveFoundryFluidData::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void clientHandle(final MoveFoundryFluidData data, final IPayloadContext context) {

    }
    public static void serverHandle(final MoveFoundryFluidData data, final IPayloadContext context) {
        if (context.player().level().getBlockEntity(data.pos()) instanceof FoundryControllerBlockEntity blockEntity) {
            ProductiveMetalworks.LOGGER.info("move fluid in tank " + data.tank);
            blockEntity.moveTankFirst(data.tank);
        }
    }
}
