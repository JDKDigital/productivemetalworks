package cy.jdkdigital.productivemetalworks.datagen;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import cy.jdkdigital.productivemetalworks.ProductiveMetalworks;
import cy.jdkdigital.productivemetalworks.common.block.HotLiquidBlock;
import cy.jdkdigital.productivemetalworks.common.block.MeatBlock;
import cy.jdkdigital.productivemetalworks.registry.MetalworksRegistrator;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.models.blockstates.*;
import net.minecraft.data.models.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class BlockModelProvider implements DataProvider
{
    protected final PackOutput packOutput;

    protected final Map<ResourceLocation, Supplier<JsonElement>> models = new HashMap<>();

    public BlockModelProvider(PackOutput packOutput) {
        this.packOutput = packOutput;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        Map<Block, BlockStateGenerator> blockModels = Maps.newHashMap();
        Consumer<BlockStateGenerator> blockStateOutput = (blockStateGenerator) -> {
            Block block = blockStateGenerator.getBlock();
            BlockStateGenerator blockstategenerator = blockModels.put(block, blockStateGenerator);
            if (blockstategenerator != null) {
                throw new IllegalStateException("Duplicate blockstate definition for " + block);
            }
        };
        Map<ResourceLocation, Supplier<JsonElement>> itemModels = Maps.newHashMap();
        BiConsumer<ResourceLocation, Supplier<JsonElement>> modelOutput = (resourceLocation, elementSupplier) -> {
            Supplier<JsonElement> supplier = itemModels.put(resourceLocation, elementSupplier);
            if (supplier != null) {
                throw new IllegalStateException("Duplicate model definition for " + resourceLocation);
            }
        };

        ModelGenerator generator = new ModelGenerator();
        try {
            generator.registerStatesAndModels(blockStateOutput, modelOutput);
        } catch (Exception e) {
            ProductiveMetalworks.LOGGER.error("Error registering states and models", e);
        }

        MetalworksRegistrator.FOUNDRY_CONTROLLERS.forEach((dyeColor, holder) -> {
            addBlockItemParentModel(holder.get(), "", "_off", itemModels);
        });
        MetalworksRegistrator.FOUNDRY_DRAINS.forEach((dyeColor, holder) -> {
            addBlockItemParentModel(holder.get(), "", "", itemModels);
        });
        MetalworksRegistrator.FOUNDRY_TANKS.forEach((dyeColor, holder) -> {
            addBlockItemParentModel(holder.get(), "", "", itemModels);
        });
        MetalworksRegistrator.FOUNDRY_WINDOWS.forEach((dyeColor, holder) -> {
            addBlockItemParentModel(holder.get(), "", "", itemModels);
        });
        MetalworksRegistrator.FIRE_BRICKS.forEach((dyeColor, holder) -> {
            addBlockItemParentModel(holder.get(), "", "", itemModels);
        });
        addBlockItemModel(MetalworksRegistrator.FOUNDRY_TAP.get(), "foundry_tap_base", itemModels);
        addBlockItemParentModel(MetalworksRegistrator.FIRE_CLAY.get(), "", "", itemModels);
        addBlockItemParentModel(MetalworksRegistrator.LIQUID_HEATING_COIL.get(), "", "_off", itemModels);
        addBlockItemParentModel(MetalworksRegistrator.POWERED_HEATING_COIL.get(), "", "_off", itemModels);
        addBlockItemModel(MetalworksRegistrator.CASTING_BASIN.get(), "casting_basin_base", itemModels);
        addBlockItemModel(MetalworksRegistrator.CASTING_TABLE.get(), "casting_table_base", itemModels);
        addBlockItemParentModel(MetalworksRegistrator.MEAT_BLOCK.get(), "", "", itemModels);

        PackOutput.PathProvider blockstatePathProvider = packOutput.createPathProvider(PackOutput.Target.RESOURCE_PACK, "blockstates");
        PackOutput.PathProvider modelPathProvider = packOutput.createPathProvider(PackOutput.Target.RESOURCE_PACK, "models");

        List<CompletableFuture<?>> output = new ArrayList<>();
        blockModels.forEach((block, supplier) -> {
            output.add(DataProvider.saveStable(cache, supplier.get(), blockstatePathProvider.json(BuiltInRegistries.BLOCK.getKey(block))));
        });
        itemModels.forEach((rLoc, supplier) -> {
            output.add(DataProvider.saveStable(cache, supplier.get(), modelPathProvider.json(rLoc)));
        });

        return CompletableFuture.allOf(output.toArray(CompletableFuture[]::new));
    }

    private void generateFlatItem(Item item, String prefix, BiConsumer<ResourceLocation, Supplier<JsonElement>> modelOutput) {
        ModelTemplates.FLAT_ITEM.create(ModelLocationUtils.getModelLocation(item), getFlatItemTextureMap(item, prefix), modelOutput);
    }

    private static TextureMapping getFlatItemTextureMap(Item item, String prefix) {
        return getFlatItemTextureMap(item, prefix, "");
    }

    private static TextureMapping getFlatItemTextureMap(Item item, String prefix, String suffix) {
        return getFlatItemTextureMap(BuiltInRegistries.ITEM.getKey(item), prefix, suffix);
    }

    private static TextureMapping getFlatItemTextureMap(ResourceLocation resourceLocation, String prefix, String suffix) {
        return (new TextureMapping()).put(TextureSlot.LAYER0, resourceLocation.withPrefix(prefix).withSuffix(suffix));
    }

    private void addItemModel(Item item, Supplier<JsonElement> supplier, Map<ResourceLocation, Supplier<JsonElement>> itemModels) {
        if (item != null) {
            ResourceLocation resourcelocation = ModelLocationUtils.getModelLocation(item);
            if (!itemModels.containsKey(resourcelocation)) {
                itemModels.put(resourcelocation, supplier);
            }
        }
    }

    private void addBlockItemModel(Block block, String base, Map<ResourceLocation, Supplier<JsonElement>> itemModels) {
        Item item = Item.BY_BLOCK.get(block);
        if (item != null) {
            addItemModel(item, new DelegatedModel(ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "block/" + base)), itemModels);
        }
    }

    private void addBlockItemParentModel(Block block, String prefix, String suffix, Map<ResourceLocation, Supplier<JsonElement>> itemModels) {
        Item item = Item.BY_BLOCK.get(block);
        if (item != null) {
            var rl = BuiltInRegistries.BLOCK.getKey(block);
            addItemParentModel(item, rl, "block/" + prefix, suffix, itemModels);
        }
    }

    private void addItemParentModel(Item item, ResourceLocation rl, String prefix, String suffix, Map<ResourceLocation, Supplier<JsonElement>> itemModels) {
        addItemModel(item, new DelegatedModel(ResourceLocation.fromNamespaceAndPath(rl.getNamespace(), prefix + rl.getPath() + suffix)), itemModels);
    }

    @Override
    public String getName() {
        return "Productive Metalworks Blockstate and Model generator";
    }

    static class ModelGenerator
    {
        Consumer<BlockStateGenerator> blockStateOutput;
        BiConsumer<ResourceLocation, Supplier<JsonElement>> modelOutput;

        static ModelTemplate controllerBaseModel = new ModelTemplate(Optional.of(ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "block/foundry_controller_base")), Optional.empty(), TextureSlot.FRONT, TextureSlot.SIDE, TextureSlot.TOP);
        static ModelTemplate drainBaseModel = new ModelTemplate(Optional.of(ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "block/foundry_drain_base")), Optional.empty(), TextureSlot.FRONT, TextureSlot.SIDE, TextureSlot.TOP);
        static ModelTemplate tankBaseModel = new ModelTemplate(Optional.of(ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "block/foundry_tank_base")), Optional.empty(), TextureSlot.FRONT, TextureSlot.SIDE, TextureSlot.TOP);
        static ModelTemplate windowBaseModel = new ModelTemplate(Optional.of(ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "block/foundry_window_base")), Optional.empty(), TextureSlot.FRONT, TextureSlot.SIDE, TextureSlot.TOP);

        protected void registerStatesAndModels(Consumer<BlockStateGenerator> blockStateOutput, BiConsumer<ResourceLocation, Supplier<JsonElement>> modelOutput) {
            this.blockStateOutput = blockStateOutput;
            this.modelOutput = modelOutput;

            MetalworksRegistrator.FOUNDRY_CONTROLLERS.forEach((dyeColor, holder) -> {
                this.blockStateOutput.accept(createBasedBlockOnOff(holder.get(), controllerBaseModel, dyeColor, "block/foundry_controller_front_on", "block/foundry_controller_front_off"));
            });
            MetalworksRegistrator.FOUNDRY_DRAINS.forEach((dyeColor, holder) -> {
                this.blockStateOutput.accept(createBasedBlock(holder.get(), drainBaseModel, dyeColor, "block/foundry_drain_front"));
            });
            MetalworksRegistrator.FOUNDRY_TANKS.forEach((dyeColor, holder) -> {
                this.blockStateOutput.accept(createBasedBlock(holder.get(), tankBaseModel, dyeColor, "block/" + dyeColor.getSerializedName() + "_foundry_tank_front"));
            });
            MetalworksRegistrator.FOUNDRY_WINDOWS.forEach((dyeColor, holder) -> {
                this.blockStateOutput.accept(createBasedBlock(holder.get(), windowBaseModel, dyeColor, "block/" + dyeColor.getSerializedName() + "_foundry_window_front"));
            });
            MetalworksRegistrator.FIRE_BRICKS.forEach((dyeColor, holder) -> {
                this.blockStateOutput.accept(createHorizontalFacingFullBlock(holder.get()));
            });
            this.blockStateOutput.accept(createHorizontalFacing(MetalworksRegistrator.FOUNDRY_TAP.get(), ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "block/foundry_tap_base")));
            this.blockStateOutput.accept(createHeatingCoil(MetalworksRegistrator.LIQUID_HEATING_COIL.get()));
            this.blockStateOutput.accept(createHeatingCoil(MetalworksRegistrator.POWERED_HEATING_COIL.get()));
            this.blockStateOutput.accept(createHorizontalFacing(MetalworksRegistrator.CASTING_BASIN.get(), ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "block/casting_basin_base")));
            this.blockStateOutput.accept(createHorizontalFacing(MetalworksRegistrator.CASTING_TABLE.get(), ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "block/casting_table_base")));
            this.blockStateOutput.accept(createFullBlock(MetalworksRegistrator.FIRE_CLAY.get()));
            this.blockStateOutput.accept(createSlicedPillarBlock(MetalworksRegistrator.MEAT_BLOCK.get()));

            ProductiveMetalworks.BLOCKS.getEntries().stream().filter(h -> h.get() instanceof HotLiquidBlock).forEach(block -> {
                this.blockStateOutput.accept(createSimpleBlock(block.get(), ModelTemplates.CUBE_ALL.create(block.get(), TextureMapping.cube(ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "block/fluid/molten_metal")), this.modelOutput)));
            });
        }

        MultiVariantGenerator createBasedBlock(Block block, ModelTemplate baseTemplate, DyeColor color, String frontTexture) {
            TextureMapping mapping = new TextureMapping()
                    .put(TextureSlot.SIDE, TextureMapping.getBlockTexture(MetalworksRegistrator.FIRE_BRICKS.get(color).get()))
                    .put(TextureSlot.TOP, TextureMapping.getBlockTexture(MetalworksRegistrator.FIRE_BRICKS.get(color).get()))
                    .put(TextureSlot.FRONT, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, frontTexture));
            ResourceLocation model = baseTemplate.create(block, mapping, this.modelOutput);
            return createHorizontalFacing(block, model);
        }

        MultiVariantGenerator createBasedBlockOnOff(Block block, ModelTemplate baseTemplate, DyeColor color, String frontTextureOn, String frontTextureOff) {
            TextureMapping mappingOn = new TextureMapping()
                    .put(TextureSlot.SIDE, TextureMapping.getBlockTexture(MetalworksRegistrator.FIRE_BRICKS.get(color).get()))
                    .put(TextureSlot.TOP, TextureMapping.getBlockTexture(MetalworksRegistrator.FIRE_BRICKS.get(color).get()))
                    .put(TextureSlot.FRONT, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, frontTextureOn));
            TextureMapping mappingOff = new TextureMapping()
                    .put(TextureSlot.SIDE, TextureMapping.getBlockTexture(MetalworksRegistrator.FIRE_BRICKS.get(color).get()))
                    .put(TextureSlot.TOP, TextureMapping.getBlockTexture(MetalworksRegistrator.FIRE_BRICKS.get(color).get()))
                    .put(TextureSlot.FRONT, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, frontTextureOff));
            ResourceLocation modelOn = baseTemplate.createWithSuffix(block, "_on", mappingOn, this.modelOutput);
            ResourceLocation modelOff = baseTemplate.createWithSuffix(block, "_off", mappingOff, this.modelOutput);
            return createHorizontalFacingOnOff(block, modelOn, modelOff);
        }

        MultiVariantGenerator createHorizontalFacingFullBlock(Block block) {
            ResourceLocation model = ModelTemplates.CUBE_ORIENTABLE.create(block, TextureMapping.cube(block), this.modelOutput);
            return createHorizontalFacing(block, model);
        }

        MultiVariantGenerator createHorizontalFacing(Block block, ResourceLocation modelLocation) {
            return MultiVariantGenerator.multiVariant(block, Variant.variant().with(VariantProperties.MODEL, modelLocation)).with(
                    PropertyDispatch.property(BlockStateProperties.HORIZONTAL_FACING).
                            select(Direction.EAST, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90).with(VariantProperties.MODEL, modelLocation)).
                            select(Direction.SOUTH, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)).
                            select(Direction.WEST, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)).
                            select(Direction.NORTH, Variant.variant())
            );
        }

        MultiVariantGenerator createHorizontalFacingOnOff(Block block, ResourceLocation onModel, ResourceLocation offModel) {
            return MultiVariantGenerator.multiVariant(block, Variant.variant())
                .with(
                        PropertyDispatch.property(BlockStateProperties.HORIZONTAL_FACING).
                                select(Direction.EAST, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)).
                                select(Direction.SOUTH, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)).
                                select(Direction.WEST, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)).
                                select(Direction.NORTH, Variant.variant())
                ).with(
                        PropertyDispatch.property(BlockStateProperties.ATTACHED).generate((on) -> Variant.variant().with(VariantProperties.MODEL, on ? onModel : offModel)));
        }

        MultiVariantGenerator createHeatingCoil(Block block) {
            TextureMapping mappingOn = new TextureMapping()
                    .put(TextureSlot.BOTTOM, TextureMapping.getBlockTexture(block, "_top_on"))
                    .put(TextureSlot.SIDE, TextureMapping.getBlockTexture(block, "_side_on"))
                    .put(TextureSlot.TOP, TextureMapping.getBlockTexture(block, "_top_on"));
            TextureMapping mappingOff = new TextureMapping()
                    .put(TextureSlot.BOTTOM, TextureMapping.getBlockTexture(block, "_top_off"))
                    .put(TextureSlot.SIDE, TextureMapping.getBlockTexture(block, "_side_off"))
                    .put(TextureSlot.TOP, TextureMapping.getBlockTexture(block, "_top_off"));
            ResourceLocation onModel = ModelTemplates.CUBE_BOTTOM_TOP.createWithSuffix(block, "_on", mappingOn, this.modelOutput);
            ResourceLocation offModel = ModelTemplates.CUBE_BOTTOM_TOP.createWithSuffix(block, "_off", mappingOff, this.modelOutput);
            return MultiVariantGenerator.multiVariant(block, Variant.variant())
                    .with(PropertyDispatch.property(BlockStateProperties.ATTACHED).generate((on) -> Variant.variant().with(VariantProperties.MODEL, on ? onModel : offModel)));
        }

        MultiVariantGenerator createAxisAlignedPillarBlock(Block axisAlignedPillarBlock, TexturedModel.Provider provider) {
            ResourceLocation resourcelocation = provider.create(axisAlignedPillarBlock, this.modelOutput);
            return MultiVariantGenerator.multiVariant(axisAlignedPillarBlock, Variant.variant().with(VariantProperties.MODEL, resourcelocation)).with(createRotatedPillar());
        }

        MultiVariantGenerator createFullBlock(Block block) {
            TextureMapping mapping = new TextureMapping().put(TextureSlot.ALL, TextureMapping.getBlockTexture(block));
            ResourceLocation model = ModelTemplates.CUBE_ALL.create(block, mapping, this.modelOutput);
            return MultiVariantGenerator.multiVariant(block, Variant.variant().with(VariantProperties.MODEL, model));
        }

        MultiVariantGenerator createSimpleBlock(Block block, ResourceLocation modelLocation) {
            return MultiVariantGenerator.multiVariant(block, Variant.variant().with(VariantProperties.MODEL, modelLocation));
        }

        PropertyDispatch createRotatedPillar() {
            return PropertyDispatch.property(BlockStateProperties.AXIS)
                    .select(Direction.Axis.Y, Variant.variant())
                    .select(Direction.Axis.Z, Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R90))
                    .select(
                            Direction.Axis.X,
                            Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R90).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                    );
        }

        private MultiVariantGenerator createSlicedPillarBlock(Block block) {
            return MultiVariantGenerator.multiVariant(block)
                    .with(createRotatedPillar())
                    .with(
                            PropertyDispatch.property(MeatBlock.BITES)
                                    .select(0, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(block)))
                                    .select(1, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(block, "_1")))
                                    .select(2, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(block, "_2")))
                                    .select(3, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(block, "_3")))
                                    .select(4, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(block, "_4")))
                                    .select(5, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(block, "_5")))
                                    .select(6, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(block, "_6")))
                                    .select(7, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(block, "_7")))
                    );
        }
    }
}
