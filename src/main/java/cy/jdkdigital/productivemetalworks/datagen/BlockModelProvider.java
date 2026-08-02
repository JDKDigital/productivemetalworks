package cy.jdkdigital.productivemetalworks.datagen;

import com.mojang.math.Quadrant;
import cy.jdkdigital.productivemetalworks.ProductiveMetalworks;
import cy.jdkdigital.productivemetalworks.common.block.HotLiquidBlock;
import cy.jdkdigital.productivemetalworks.common.block.MeatBlock;
import cy.jdkdigital.productivemetalworks.registry.MetalworksRegistrator;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.*;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.client.renderer.block.dispatch.VariantMutator;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.client.model.item.DynamicFluidContainerModel;

import java.util.*;
import java.util.stream.Stream;

public class BlockModelProvider extends ModelProvider
{
    private static final ModelTemplate CONTROLLER_BASE = baseModel("block/foundry_controller_base");
    private static final ModelTemplate DRAIN_BASE = baseModel("block/foundry_drain_base");
    private static final ModelTemplate TANK_BASE = baseModel("block/foundry_tank_base");
    private static final ModelTemplate CAPACITOR_BASE = baseModel("block/foundry_capacitor_base");
    private static final ModelTemplate WINDOW_BASE = baseModel("block/foundry_window_base");

    public BlockModelProvider(PackOutput packOutput) {
        super(packOutput, ProductiveMetalworks.MODID);
    }

    @Override
    protected Stream<? extends Holder<Block>> getKnownBlocks() {
        List<Holder<Block>> known = new ArrayList<>();
        addBlocks(known, MetalworksRegistrator.FOUNDRY_CONTROLLERS.values());
        addBlocks(known, MetalworksRegistrator.FOUNDRY_DRAINS.values());
        addBlocks(known, MetalworksRegistrator.FOUNDRY_TANKS.values());
        addBlocks(known, MetalworksRegistrator.FOUNDRY_CAPACITORS.values());
        addBlocks(known, MetalworksRegistrator.FOUNDRY_WINDOWS.values());
        addBlocks(known, MetalworksRegistrator.FIRE_BRICKS.values());
        known.add(MetalworksRegistrator.FOUNDRY_TAP.get().builtInRegistryHolder());
        known.add(MetalworksRegistrator.CASTING_BASIN.get().builtInRegistryHolder());
        known.add(MetalworksRegistrator.CASTING_TABLE.get().builtInRegistryHolder());
        known.add(MetalworksRegistrator.LIQUID_HEATING_COIL.get().builtInRegistryHolder());
        known.add(MetalworksRegistrator.POWERED_HEATING_COIL.get().builtInRegistryHolder());
        known.add(MetalworksRegistrator.HIGH_POWERED_HEATING_COIL.get().builtInRegistryHolder());
        known.add(MetalworksRegistrator.FIRE_CLAY.get().builtInRegistryHolder());
        known.add(MetalworksRegistrator.MEAT_BLOCK.get().builtInRegistryHolder());
        ProductiveMetalworks.BLOCKS.getEntries().stream().filter(h -> h.get() instanceof HotLiquidBlock).forEach(h -> known.add(h.get().builtInRegistryHolder()));
        return known.stream();
    }

    @Override
    protected Stream<? extends Holder<Item>> getKnownItems() {
        List<Holder<Item>> known = new ArrayList<>();
        ProductiveMetalworks.ITEMS.getEntries().forEach(h -> known.add(h.get().builtInRegistryHolder()));
        return known.stream();
    }

    private static void addBlocks(List<Holder<Block>> list, Iterable<? extends net.neoforged.neoforge.registries.DeferredHolder<Block, Block>> holders) {
        holders.forEach(h -> list.add(h.get().builtInRegistryHolder()));
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        // ===== Blockstates + block models =====
        MetalworksRegistrator.FOUNDRY_CONTROLLERS.forEach((color, holder) ->
                basedBlockOnOff(blockModels, holder.get(), CONTROLLER_BASE, color, "block/foundry_controller_front_on", "block/foundry_controller_front_off"));
        MetalworksRegistrator.FOUNDRY_DRAINS.forEach((color, holder) ->
                basedBlock(blockModels, holder.get(), DRAIN_BASE, color, "block/foundry_drain_front"));
        MetalworksRegistrator.FOUNDRY_TANKS.forEach((color, holder) ->
                basedBlock(blockModels, holder.get(), TANK_BASE, color, "block/" + color.getSerializedName() + "_foundry_tank_front"));
        MetalworksRegistrator.FOUNDRY_CAPACITORS.forEach((color, holder) ->
                basedBlock(blockModels, holder.get(), CAPACITOR_BASE, color, "block/" + color.getSerializedName() + "_foundry_capacitor_front"));
        MetalworksRegistrator.FOUNDRY_WINDOWS.forEach((color, holder) ->
                basedBlock(blockModels, holder.get(), WINDOW_BASE, color, "block/" + color.getSerializedName() + "_foundry_window_front"));
        MetalworksRegistrator.FIRE_BRICKS.forEach((color, holder) -> {
            // Fire bricks are a HorizontalDirectionalBlock but visually a single all-faces texture (e.g. black_fire_bricks.png),
            // so model as CUBE_ALL; the facing variants below just keep the blockstate complete (rotation is a no-op visually).
            Identifier model = ModelTemplates.CUBE_ALL.create(holder.get(), TextureMapping.cube(holder.get()), blockModels.modelOutput);
            horizontalFacing(blockModels, holder.get(), model);
        });

        horizontalFacing(blockModels, MetalworksRegistrator.FOUNDRY_TAP.get(), pmwId("block/foundry_tap_base"));
        horizontalFacing(blockModels, MetalworksRegistrator.CASTING_BASIN.get(), pmwId("block/casting_basin_base"));
        horizontalFacing(blockModels, MetalworksRegistrator.CASTING_TABLE.get(), pmwId("block/casting_table_base"));

        heatingCoil(blockModels, MetalworksRegistrator.LIQUID_HEATING_COIL.get());
        heatingCoil(blockModels, MetalworksRegistrator.POWERED_HEATING_COIL.get());
        heatingCoil(blockModels, MetalworksRegistrator.HIGH_POWERED_HEATING_COIL.get());

        Identifier fireClayModel = ModelTemplates.CUBE_ALL.create(MetalworksRegistrator.FIRE_CLAY.get(), TextureMapping.cube(MetalworksRegistrator.FIRE_CLAY.get()), blockModels.modelOutput);
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(MetalworksRegistrator.FIRE_CLAY.get(), plainVariant(fireClayModel)));

        slicedPillar(blockModels, MetalworksRegistrator.MEAT_BLOCK.get());

        ProductiveMetalworks.BLOCKS.getEntries().stream().filter(h -> h.get() instanceof HotLiquidBlock).forEach(holder -> {
            Identifier model = ModelTemplates.CUBE_ALL.create(holder.get(), TextureMapping.cube(new Material(pmwId("block/fluid/molten_metal"))), blockModels.modelOutput);
            blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(holder.get(), plainVariant(model)));
        });

        // ===== Item models =====
        Set<Item> handled = new HashSet<>();

        // Flat items.
        flatItem(itemModels, handled, MetalworksRegistrator.CAST_INGOT.get());
        flatItem(itemModels, handled, MetalworksRegistrator.CAST_NUGGET.get());
        flatItem(itemModels, handled, MetalworksRegistrator.CAST_GEM.get());
        flatItem(itemModels, handled, MetalworksRegistrator.CAST_GEAR.get());
        flatItem(itemModels, handled, MetalworksRegistrator.CAST_ROD.get());
        flatItem(itemModels, handled, MetalworksRegistrator.CAST_PLATE.get());
        flatItem(itemModels, handled, MetalworksRegistrator.FIRE_BRICK.get());
        flatItem(itemModels, handled, MetalworksRegistrator.MEAT_NUGGET.get());
        flatItem(itemModels, handled, MetalworksRegistrator.MEAT_INGOT.get());
        flatItem(itemModels, handled, MetalworksRegistrator.SHINY_MEAT_INGOT.get());

        // Block items that parent a specific block model.
        MetalworksRegistrator.FOUNDRY_CONTROLLERS.forEach((c, h) -> blockItemParent(blockModels, itemModels, handled, h.get(), "_off"));
        MetalworksRegistrator.FOUNDRY_DRAINS.forEach((c, h) -> blockItemParent(blockModels, itemModels, handled, h.get(), ""));
        MetalworksRegistrator.FOUNDRY_TANKS.forEach((c, h) -> blockItemParent(blockModels, itemModels, handled, h.get(), ""));
        MetalworksRegistrator.FOUNDRY_CAPACITORS.forEach((c, h) -> blockItemParent(blockModels, itemModels, handled, h.get(), ""));
        MetalworksRegistrator.FOUNDRY_WINDOWS.forEach((c, h) -> blockItemParent(blockModels, itemModels, handled, h.get(), ""));
        MetalworksRegistrator.FIRE_BRICKS.forEach((c, h) -> blockItemParent(blockModels, itemModels, handled, h.get(), ""));
        blockItemModel(blockModels, itemModels, handled, MetalworksRegistrator.FOUNDRY_TAP.get(), "foundry_tap_base");
        blockItemModel(blockModels, itemModels, handled, MetalworksRegistrator.CASTING_BASIN.get(), "casting_basin_base");
        blockItemModel(blockModels, itemModels, handled, MetalworksRegistrator.CASTING_TABLE.get(), "casting_table_base");
        blockItemParent(blockModels, itemModels, handled, MetalworksRegistrator.FIRE_CLAY.get(), "");
        blockItemParent(blockModels, itemModels, handled, MetalworksRegistrator.LIQUID_HEATING_COIL.get(), "_off");
        blockItemParent(blockModels, itemModels, handled, MetalworksRegistrator.POWERED_HEATING_COIL.get(), "_off");
        blockItemParent(blockModels, itemModels, handled, MetalworksRegistrator.HIGH_POWERED_HEATING_COIL.get(), "_off");
        blockItemParent(blockModels, itemModels, handled, MetalworksRegistrator.MEAT_BLOCK.get(), "");

        // Fluid buckets use NeoForge's dynamic fluid-container item model (neoforge:fluid_container): a plain bucket base
        // plus a fluid-mask layer that is re-textured/tinted at runtime to the contained fluid. Keyed off each source
        // fluid's bucket so molten_* and meat are all covered.
        for (var fluidHolder : ProductiveMetalworks.FLUIDS.getEntries()) {
            Fluid fluid = fluidHolder.get();
            if (fluid.defaultFluidState().isSource()) {
                Item bucket = fluid.getBucket();
                if (bucket instanceof BucketItem && handled.add(bucket)) {
                    itemModels.itemModelOutput.accept(bucket, new DynamicFluidContainerModel.Unbaked(
                            new DynamicFluidContainerModel.Textures(
                                    Optional.empty(),
                                    Optional.of(new Material(Identifier.withDefaultNamespace("item/bucket"))),
                                    Optional.of(new Material(Identifier.fromNamespaceAndPath("neoforge", "item/mask/bucket_fluid_drip"))),
                                    Optional.empty()),
                            fluid, false, true, true));
                }
            }
        }

        // Everything else (any unhandled item) points at the shipped models/item/<id>.json.
        Set<Item> seen = new HashSet<>();
        for (var holder : ProductiveMetalworks.ITEMS.getEntries()) {
            Item item = holder.get();
            if (!handled.contains(item) && seen.add(item)) {
                Identifier id = BuiltInRegistries.ITEM.getKey(item);
                itemModels.itemModelOutput.accept(item, ItemModelUtils.plainModel(Identifier.fromNamespaceAndPath(id.getNamespace(), "item/" + id.getPath())));
            }
        }
    }

    // ===== Blockstate helpers =====

    private void basedBlock(BlockModelGenerators blockModels, Block block, ModelTemplate baseTemplate, DyeColor color, String frontTexture) {
        Block brick = MetalworksRegistrator.FIRE_BRICKS.get(color).get();
        TextureMapping mapping = new TextureMapping()
                .put(TextureSlot.SIDE, TextureMapping.getBlockTexture(brick))
                .put(TextureSlot.TOP, TextureMapping.getBlockTexture(brick))
                .put(TextureSlot.FRONT, new Material(pmwId(frontTexture)));
        Identifier model = baseTemplate.create(block, mapping, blockModels.modelOutput);
        horizontalFacing(blockModels, block, model);
    }

    private void basedBlockOnOff(BlockModelGenerators blockModels, Block block, ModelTemplate baseTemplate, DyeColor color, String frontOn, String frontOff) {
        Block brick = MetalworksRegistrator.FIRE_BRICKS.get(color).get();
        TextureMapping mappingOn = new TextureMapping()
                .put(TextureSlot.SIDE, TextureMapping.getBlockTexture(brick))
                .put(TextureSlot.TOP, TextureMapping.getBlockTexture(brick))
                .put(TextureSlot.FRONT, new Material(pmwId(frontOn)));
        TextureMapping mappingOff = new TextureMapping()
                .put(TextureSlot.SIDE, TextureMapping.getBlockTexture(brick))
                .put(TextureSlot.TOP, TextureMapping.getBlockTexture(brick))
                .put(TextureSlot.FRONT, new Material(pmwId(frontOff)));
        Identifier modelOn = baseTemplate.createWithSuffix(block, "_on", mappingOn, blockModels.modelOutput);
        Identifier modelOff = baseTemplate.createWithSuffix(block, "_off", mappingOff, blockModels.modelOutput);
        blockModels.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(block)
                        .with(PropertyDispatch.initial(BlockStateProperties.ATTACHED).generate(on -> plainVariant(on ? modelOn : modelOff)))
                        .with(facingDispatch()));
    }

    private void horizontalFacing(BlockModelGenerators blockModels, Block block, Identifier model) {
        blockModels.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(block, plainVariant(model)).with(facingDispatch()));
    }

    private void heatingCoil(BlockModelGenerators blockModels, Block block) {
        TextureMapping mappingOn = new TextureMapping()
                .put(TextureSlot.BOTTOM, TextureMapping.getBlockTexture(block, "_top_on"))
                .put(TextureSlot.SIDE, TextureMapping.getBlockTexture(block, "_side_on"))
                .put(TextureSlot.TOP, TextureMapping.getBlockTexture(block, "_top_on"));
        TextureMapping mappingOff = new TextureMapping()
                .put(TextureSlot.BOTTOM, TextureMapping.getBlockTexture(block, "_top_off"))
                .put(TextureSlot.SIDE, TextureMapping.getBlockTexture(block, "_side_off"))
                .put(TextureSlot.TOP, TextureMapping.getBlockTexture(block, "_top_off"));
        Identifier onModel = ModelTemplates.CUBE_BOTTOM_TOP.createWithSuffix(block, "_on", mappingOn, blockModels.modelOutput);
        Identifier offModel = ModelTemplates.CUBE_BOTTOM_TOP.createWithSuffix(block, "_off", mappingOff, blockModels.modelOutput);
        blockModels.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(block)
                        .with(PropertyDispatch.initial(BlockStateProperties.ATTACHED).generate(on -> plainVariant(on ? onModel : offModel))));
    }

    private void slicedPillar(BlockModelGenerators blockModels, Block block) {
        Identifier base = BuiltInRegistries.BLOCK.getKey(block).withPrefix("block/");
        blockModels.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(block)
                        .with(PropertyDispatch.initial(MeatBlock.BITES).generate(bites ->
                                plainVariant(bites == 0 ? base : base.withSuffix("_" + bites))))
                        .with(PropertyDispatch.modify(BlockStateProperties.AXIS)
                                .select(Direction.Axis.Y, VariantMutator.X_ROT.withValue(Quadrant.R0))
                                .select(Direction.Axis.Z, VariantMutator.X_ROT.withValue(Quadrant.R90))
                                .select(Direction.Axis.X, VariantMutator.X_ROT.withValue(Quadrant.R90).then(VariantMutator.Y_ROT.withValue(Quadrant.R90)))));
    }

    private static PropertyDispatch<VariantMutator> facingDispatch() {
        return PropertyDispatch.modify(BlockStateProperties.HORIZONTAL_FACING)
                .select(Direction.NORTH, VariantMutator.Y_ROT.withValue(Quadrant.R0))
                .select(Direction.EAST, VariantMutator.Y_ROT.withValue(Quadrant.R90))
                .select(Direction.SOUTH, VariantMutator.Y_ROT.withValue(Quadrant.R180))
                .select(Direction.WEST, VariantMutator.Y_ROT.withValue(Quadrant.R270));
    }

    // ===== Item-model helpers =====

    private void flatItem(ItemModelGenerators itemModels, Set<Item> handled, Item item) {
        itemModels.generateFlatItem(item, ModelTemplates.FLAT_ITEM);
        handled.add(item);
    }

    private void blockItemParent(BlockModelGenerators blockModels, ItemModelGenerators itemModels, Set<Item> handled, Block block, String suffix) {
        Identifier blockId = BuiltInRegistries.BLOCK.getKey(block);
        blockItem(blockModels, itemModels, handled, block, Identifier.fromNamespaceAndPath(blockId.getNamespace(), "block/" + blockId.getPath() + suffix));
    }

    private void blockItemModel(BlockModelGenerators blockModels, ItemModelGenerators itemModels, Set<Item> handled, Block block, String baseModelPath) {
        blockItem(blockModels, itemModels, handled, block, pmwId("block/" + baseModelPath));
    }

    private void blockItem(BlockModelGenerators blockModels, ItemModelGenerators itemModels, Set<Item> handled, Block block, Identifier parentModel) {
        Item item = block.asItem();
        if (!(item instanceof BlockItem) || handled.contains(item)) {
            return;
        }
        Identifier itemModelId = BuiltInRegistries.ITEM.getKey(item).withPrefix("item/");
        itemTemplate(parentModel).create(itemModelId, new TextureMapping(), blockModels.modelOutput);
        itemModels.itemModelOutput.accept(item, ItemModelUtils.plainModel(itemModelId));
        handled.add(item);
    }

    // ===== Misc helpers =====

    private static ModelTemplate baseModel(String path) {
        return new ModelTemplate(Optional.of(Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, path)), Optional.empty(), TextureSlot.FRONT, TextureSlot.SIDE, TextureSlot.TOP);
    }

    private static ModelTemplate itemTemplate(Identifier parent) {
        return new ModelTemplate(Optional.of(parent), Optional.empty());
    }

    private static Identifier pmwId(String path) {
        return Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, path);
    }

    private static MultiVariant plainVariant(Identifier model) {
        return new MultiVariant(WeightedList.of(new Variant(model)));
    }
}
