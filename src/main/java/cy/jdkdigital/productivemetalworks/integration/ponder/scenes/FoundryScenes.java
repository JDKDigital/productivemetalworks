package cy.jdkdigital.productivemetalworks.integration.ponder.scenes;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.registries.DeferredHolder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;

import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;

import com.simibubi.create.content.processing.basin.BasinBlockEntity;

import cy.jdkdigital.productivemetalworks.ProductiveMetalworks;
import cy.jdkdigital.productivemetalworks.common.block.CastingBasinBlock;
import cy.jdkdigital.productivemetalworks.common.block.CastingTableBlock;
import cy.jdkdigital.productivemetalworks.common.block.entity.CastingBlockEntity;
import cy.jdkdigital.productivemetalworks.common.block.entity.FoundryCapacitorBlockEntity;
import cy.jdkdigital.productivemetalworks.common.block.entity.FoundryControllerBlockEntity;
import cy.jdkdigital.productivemetalworks.common.block.entity.FoundryTankBlockEntity;
import cy.jdkdigital.productivemetalworks.common.block.entity.FoundryTapBlockEntity;
import cy.jdkdigital.productivemetalworks.registry.MetalworksRegistrator;
import cy.jdkdigital.productivemetalworks.util.TickingSlotInventoryHandler;

import java.util.ArrayList;
import java.util.List;

public class FoundryScenes {

    public static void building(SceneBuilder scene, SceneBuildingUtil util) {

        scene.title("foundry_building", "Building the Foundry");

        for (int x = 1; x < 6; x++) {
            for (int z = 1; z < 6; z++) {
                scene.world().showSection(util.select().position(x, 0, z), Direction.DOWN);
                scene.idle(1);
            }

            if(x == 1) {
                scene.overlay()
                        .showText(80)
                        .text("The minimum size for the foundry is 3x3x2, as it requires at least a single air block within the structure. Corners are optional");
            }
        }

        scene.idle(0);

        scene.overlay()
                .showText(50)
                .pointAt(util.vector().of(3.5, 1, 3.5))
                .text("The bottom of the foundry has to be made up of Heating Coils")
                .placeNearTarget();

        for (int x = 2; x < 5; x++) {
            for (int z = 2; z < 5; z++) {
                scene.world().replaceBlocks(util.select().position(x, 0, z), MetalworksRegistrator.LIQUID_HEATING_COIL.get().defaultBlockState(), true);
                scene.idle(1);
            }
        }

        scene.idle(60);
        scene.addKeyframe();

        for(BlockPos pos : generateRingCoordinates(new BlockPos(1, 1, 1), new BlockPos(5, 1, 5))) {
            scene.world().showSection(util.select().position(pos), Direction.DOWN);
            scene.idle(1);
        }

        scene.idle(10);

        scene.overlay()
                .showText(40)
                .pointAt(util.vector().of(3, 2, 1))
                .text("Additionally, the foundry must have a Foundry Controller...")
                .placeNearTarget();

        scene.idle(5);

        scene.world().replaceBlocks(util.select().position(3,1,1), MetalworksRegistrator.FOUNDRY_CONTROLLERS.get(DyeColor.BLACK).get().defaultBlockState(), true);

        scene.idle(50);

        scene.overlay()
                .showText(40)
                .pointAt(util.vector().of(2, 2, 1))
                .text("...as well as a Foundry Tank...")
                .placeNearTarget();

        scene.idle(5);

        scene.world().replaceBlocks(util.select().position(2,1,1), MetalworksRegistrator.FOUNDRY_TANKS.get(DyeColor.BLACK).get().defaultBlockState(), true);

        scene.idle(50);

        scene.overlay()
                .showText(40)
                .pointAt(util.vector().of(4, 2, 1))
                .text("...and at least one Foundry Drain")
                .placeNearTarget();

        scene.idle(5);

        scene.world().replaceBlocks(util.select().position(4,1,1), MetalworksRegistrator.FOUNDRY_DRAINS.get(DyeColor.BLACK).get().defaultBlockState(), true);

        scene.idle(50);

        scene.world().modifyBlock(util.grid().at(3,1,1), state -> state.setValue(BlockStateProperties.ATTACHED, true), false);

        for (int x = 2; x < 5; x++) {
            for (int z = 2; z < 5; z++) {
                scene.world().modifyBlock(util.grid().at(x, 0, z), state -> state.setValue(BlockStateProperties.ATTACHED, true), false);
            }
        }
    }

    public static void fueling(SceneBuilder scene, SceneBuildingUtil util) {

        scene.title("foundry_fueling", "Fueling the Foundry");

        for (int x = 1; x < 6; x++) {
            for (int z = 1; z < 6; z++) {
                scene.world().showSection(util.select().position(x, 0, z), Direction.DOWN);
                scene.idle(1);
            }
        }

        for(BlockPos pos : generateRingCoordinates(new BlockPos(1, 1, 1), new BlockPos(5, 1, 5))) {
            scene.world().showSection(util.select().position(pos), Direction.DOWN);
            scene.idle(1);
        }

        scene.idle(15);

        scene.overlay()
                .showText(50)
                .pointAt(util.vector().of(3, 1, 2))
                .text("For the foundry to function, it requires fuel inside a Foundry Tank")
                .placeNearTarget();

        scene.idle(60);

        scene.overlay()
                .showText(50)
                .pointAt(util.vector().of(3, 1, 2))
                .text("You can either right-click a bucket into the tank or supply fuel via a pipe")
                .placeNearTarget();

        scene.idle(50);

        scene.overlay()
                .showControls(util.vector().of(2.5, 2, 1.5), Pointing.DOWN, 30)
                .rightClick()
                .withItem(new ItemStack(Items.LAVA_BUCKET));

        scene.idle(10);

        for (int i = 0; i < 10; i++) {
            scene.world().modifyBlockEntity(util.grid().at(2, 1, 1), FoundryTankBlockEntity.class, tank -> tank.getFluidHandler().fill(new FluidStack(Fluids.LAVA, 400), IFluidHandler.FluidAction.EXECUTE));
            scene.idle(1);
        }

        scene.idle(50);
        scene.addKeyframe();

        for(BlockPos pos : generateRingCoordinates(new BlockPos(1, 1, 1), new BlockPos(5, 1, 5))) {
            scene.world().hideSection(util.select().position(pos), Direction.DOWN);
            scene.idle(1);
        }

        scene.idle(30);

        scene.overlay()
                .showText(80)
                .pointAt(util.vector().of(3.5, 1, 3.5))
                .text("You can replace the Fluid Heating Coils with Powered Heating Coils to use energy instead of liquid fuel...")
                .placeNearTarget();

        for (int x = 2; x < 5; x++) {
            for (int z = 2; z < 5; z++) {
                scene.world().replaceBlocks(util.select().position(x, 0, z), MetalworksRegistrator.POWERED_HEATING_COIL.get().defaultBlockState(), true);
                scene.idle(1);
            }
        }

        scene.idle(20);

        for (int x = 2; x < 5; x++) {
            for (int z = 2; z < 5; z++) {
                scene.world().modifyBlocks(util.select().position(x, 0, z), state -> state.setValue(BlockStateProperties.ATTACHED, true), false);
            }
        }

        scene.idle(70);

        scene.overlay()
                .showText(60)
                .pointAt(util.vector().of(3.5, 1, 3.5))
                .text("... or use High Powered Heating Coils to create even more heat")
                .placeNearTarget();

        for (int x = 2; x < 5; x++) {
            for (int z = 2; z < 5; z++) {
                scene.world().replaceBlocks(util.select().position(x, 0, z), MetalworksRegistrator.HIGH_POWERED_HEATING_COIL.get().defaultBlockState(), true);
                scene.idle(1);
            }
        }

        scene.idle(20);

        for (int x = 2; x < 5; x++) {
            for (int z = 2; z < 5; z++) {
                scene.world().modifyBlocks(util.select().position(x, 0, z), state -> state.setValue(BlockStateProperties.ATTACHED, true), false);
            }
        }

        scene.idle(60);

        for(BlockPos pos : generateRingCoordinates(new BlockPos(1, 1, 1), new BlockPos(5, 1, 5))) {
            scene.world().showSection(util.select().position(pos), Direction.DOWN);
            scene.idle(1);
        }

        scene.idle(20);

        scene.overlay()
                .showText(60)
                .pointAt(util.vector().of(3, 1, 2))
                .text("If you choose to use a Powered Heating Coils, you must replace your Foundry Tank with a Foundry Capacitor")
                .placeNearTarget();

        scene.idle(10);

        scene.world().replaceBlocks(util.select().position(2,1,1), MetalworksRegistrator.FOUNDRY_CAPACITORS.get(DyeColor.BLACK).get().defaultBlockState(), true);

        scene.idle(30);

        for (int i = 0; i < 10; i++) {
            scene.world().modifyBlockEntity(util.grid().at(2, 1, 1), FoundryCapacitorBlockEntity.class, tank -> tank.getEnergyHandler().receiveEnergy(4000, false));
            scene.idle(1);
        }

        scene.idle(30);

        scene.overlay()
                .showText(50)
                .pointAt(util.vector().of(3, 1, 2))
                .text("You can supply energy via a cable attached to the capacitor")
                .placeNearTarget();
    }

    public static void smelting(SceneBuilder scene, SceneBuildingUtil util) {

        scene.title("foundry_smelting", "Smelting Items with the Foundry");

        for (int x = 1; x < 6; x++) {
            for (int z = 1; z < 6; z++) {
                scene.world().showSection(util.select().position(x, 0, z), Direction.DOWN);
                scene.idle(1);
            }
        }

        scene.world().modifyBlockEntity(util.grid().at(2, 1, 1), FoundryTankBlockEntity.class, tank -> tank.getFluidHandler().setFluid(new FluidStack(Fluids.LAVA, 4000)));
        for(BlockPos pos : generateRingCoordinates(new BlockPos(1, 1, 1), new BlockPos(5, 1, 5))) {
            scene.world().showSection(util.select().position(pos), Direction.DOWN);
            scene.idle(1);
        }

        scene.idle(15);

        scene.overlay()
                .showText(60)
                .pointAt(util.vector().of(3, 2, 1))
                .text("To add items to the foundry, you can either add them via the controller gui...")
                .placeNearTarget();

        scene.idle(50);

        scene.overlay()
                .showControls(util.vector().of(3.5, 2, 1.5), Pointing.DOWN, 30)
                .withItem(new ItemStack(Items.RAW_IRON_BLOCK))
                .rightClick();

        scene.idle(40);

        scene.overlay()
                .showText(50)
                .pointAt(util.vector().of(3, 2, 1))
                .text("... or drop them right into the foundry")
                .placeNearTarget();

        scene.world().createItemEntity(util.vector().of(3.5, 2, 3.5), util.vector().of(0, 0.1, 0), new ItemStack(Items.RAW_IRON_BLOCK, 2));

        scene.idle(40);

        scene.world().modifyEntitiesInside(ItemEntity.class, util.select().fromTo(3, 1, 3, 4, 2, 4), item -> item.remove(Entity.RemovalReason.DISCARDED));

        for (int i = 0; i < 6; i++) {
            final int x = i;
            scene.world().modifyBlockEntity(util.grid().at(3, 1, 1), FoundryControllerBlockEntity.class, controller -> controller.getItemHandler().insertItem(x, new ItemStack(Items.RAW_IRON_BLOCK), false));
            scene.idle(2);
        }

        scene.idle(10);

        scene.overlay()
                .showText(50)
                .pointAt(util.vector().of(3, 2, 1))
                .text("After some time the items inside the foundry will turn into liquid")
                .placeNearTarget();

        scene.idle(20);

        for (int i = 0; i < 6; i++) {
            final int x = i;
            scene.world().modifyBlockEntity(util.grid().at(3, 1, 1), FoundryControllerBlockEntity.class, controller -> ((TickingSlotInventoryHandler) controller.getItemHandler()).setStackInSlot(x, ItemStack.EMPTY));
            scene.idle(2);
            scene.world().modifyBlockEntity(util.grid().at(3, 1, 1), FoundryControllerBlockEntity.class, controller -> controller.getFluidHandler().fill(new FluidStack(MetalworksRegistrator.MOLTEN_IRON, 810), IFluidHandler.FluidAction.EXECUTE));
            scene.idle(2);
        }


        scene.idle(40);
        scene.addKeyframe();

        scene.overlay()
                .showText(50)
                .pointAt(util.vector().of(3, 2, 1))
                .text("If you add a second liquid, some may undergo an alloying reaction. Adding a stability upgrade will prevent this")
                .placeNearTarget();

        scene.idle(60);

        scene.overlay()
                .showText(40)
                .pointAt(util.vector().of(4, 2, 1))
                .text("Pro tip: You can manually add fluids by right-clicking a drain with a bucket")
                .placeNearTarget();

        scene.idle(10);

        scene.overlay()
                .showControls(util.vector().of(4.5, 2, 1.5), Pointing.DOWN, 20)
                .withItem(new ItemStack(DeferredHolder.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "molten_nickel_bucket"))))
                .rightClick();

        scene.idle(10);

        for (int i = 0; i < 3; i++) {
            scene.world().modifyBlockEntity(util.grid().at(3, 1, 1), FoundryControllerBlockEntity.class, controller -> controller.getFluidHandler().fill(new FluidStack(MetalworksRegistrator.MOLTEN_NICKEL, 810), IFluidHandler.FluidAction.EXECUTE));
            scene.idle(2);
        }

        scene.idle(40);

        for (int i = 0; i < 45; i++) {
            scene.world().modifyBlockEntity(util.grid().at(3, 1, 1), FoundryControllerBlockEntity.class, controller -> controller.getFluidHandler().fill(new FluidStack(MetalworksRegistrator.MOLTEN_NICKEL, 6), IFluidHandler.FluidAction.EXECUTE));
            scene.world().modifyBlockEntity(util.grid().at(3, 1, 1), FoundryControllerBlockEntity.class, controller -> controller.getFluidHandler().fill(new FluidStack(MetalworksRegistrator.MOLTEN_IRON, 12), IFluidHandler.FluidAction.EXECUTE));
            scene.world().modifyBlockEntity(util.grid().at(3, 1, 1), FoundryControllerBlockEntity.class, controller -> controller.getFluidHandler().fill(new FluidStack(MetalworksRegistrator.MOLTEN_INVAR, 18), IFluidHandler.FluidAction.EXECUTE));
            scene.idle(2);
        }

        scene.idle(20);

        scene.overlay()
                .showText(50)
                .pointAt(util.vector().of(3.5, 1, 3.5))
                .text("For example, 2/3th Iron and 1/3th Nickel will turn into Invar")
                .placeNearTarget();

        scene.idle(60);
    }

    public static void casting(SceneBuilder scene, SceneBuildingUtil util) {

        scene.title("foundry_casting", "Casting Items with the Foundry");

        for (int x = 1; x < 6; x++) {
            for (int z = 1; z < 6; z++) {
                scene.world().showSection(util.select().position(x, 0, z), Direction.DOWN);
                scene.idle(1);
            }
        }

        for(BlockPos pos : generateRingCoordinates(new BlockPos(1, 1, 1), new BlockPos(5, 1, 5))) {
            scene.world().showSection(util.select().position(pos), Direction.DOWN);
            scene.idle(1);
        }

        scene.world().setBlock(util.grid().at(4, 0, 0), MetalworksRegistrator.CASTING_BASIN.get().defaultBlockState(), false);
        scene.world().showSection(util.select().position(4,0,0), Direction.DOWN);

        scene.idle(1);

        scene.world().setBlock(util.grid().at(4, 1, 0), MetalworksRegistrator.FOUNDRY_TAP.get().defaultBlockState(), false);
        scene.world().showSection(util.select().position(4,1,0), Direction.DOWN);

        scene.idle(20);

        scene.overlay()
                .showText(50)
                .pointAt(util.vector().of(4, 1, 0))
                .text("To turn the fluid into blocks you need to use a Casting Basin")
                .placeNearTarget();

        scene.idle(60);

        scene.overlay()
                .showText(50)
                .pointAt(util.vector().of(4, 2, 0))
                .text("Right click a Foundry Tap attached to a drain to extract it")
                .placeNearTarget();

        scene.idle(40);

        scene.overlay()
                .showControls(util.vector().of(4.5, 2, 0.5), Pointing.DOWN, 30)
                .rightClick();

        scene.idle(10);

        scene.world().modifyBlockEntity(util.grid().at(4, 1, 0), FoundryTapBlockEntity.class, tap -> {
            tap.fluidId = BuiltInRegistries.FLUID.getId(MetalworksRegistrator.MOLTEN_GOLD.get());
            tap.isActive = true;
        });

        for (int i = 0; i < 45; i++) {
            scene.world().modifyBlockEntity(util.grid().at(4, 0, 0), CastingBlockEntity.class, cast -> cast.getFluidHandler().fill(new FluidStack(MetalworksRegistrator.MOLTEN_GOLD, 18), IFluidHandler.FluidAction.EXECUTE));
            scene.idle(1);
            scene.world().modifyBlockEntity(util.grid().at(3, 1, 1), FoundryControllerBlockEntity.class, controller -> controller.getFluidHandler().drain(18, IFluidHandler.FluidAction.EXECUTE));
        }

        scene.world().modifyBlockEntity(util.grid().at(4, 1, 0), FoundryTapBlockEntity.class, tap -> tap.isActive = false);

        scene.overlay()
                .showText(50)
                .pointAt(util.vector().of(4, 1, 0))
                .text("The Casting Basin will slowly cool down the fluid and turn it into a block")
                .placeNearTarget();

        scene.world().modifyBlockEntity(util.grid().at(4, 0, 0), CastingBlockEntity.class, cast -> {
            ((ItemStackHandler) cast.getItemHandler()).setStackInSlot(0, new ItemStack(Items.GOLD_BLOCK));
            cast.coolingTime = 250;
        });

        for (int i = 0; i < 60; i++) {
            scene.world().modifyBlockEntity(util.grid().at(4, 0, 0), CastingBlockEntity.class, cast -> cast.coolingTime -= 3);
            scene.idle(1);
        }

        scene.world().modifyBlockEntity(util.grid().at(4, 0, 0), CastingBlockEntity.class, cast -> {
            cast.getFluidHandler().setFluid(FluidStack.EMPTY);
            cast.coolingTime = 0;
        });

        scene.idle(20);

        scene.overlay()
                .showText(40)
                .pointAt(util.vector().of(4, 1, 0))
                .text("Right click the Basin to take out your item. You can also use a pipe or a hopper")
                .placeNearTarget();

        scene.idle(20);

        scene.overlay()
                .showControls(util.vector().of(4.5, 1, 0.5), Pointing.DOWN, 30)
                .rightClick();

        scene.idle(10);

        scene.world().modifyBlockEntity(util.grid().at(4, 0, 0), CastingBlockEntity.class, cast -> {
            ((ItemStackHandler) cast.getItemHandler()).setStackInSlot(0, ItemStack.EMPTY);
            cast.castInv.setStackInSlot(0, ItemStack.EMPTY);
        });

        scene.idle(50);
        scene.addKeyframe();

        scene.world().replaceBlocks(util.select().position(4, 0, 0), MetalworksRegistrator.CASTING_TABLE.get().defaultBlockState(), true);

        scene.overlay()
                .showText(40)
                .pointAt(util.vector().of(4, 1, 0))
                .text("You can also use a Casting Table to turn fluids into different casts")
                .placeNearTarget();

        scene.idle(50);

        scene.overlay()
                .showText(40)
                .pointAt(util.vector().of(4, 1, 0))
                .text("Add a cast by right-clicking the with the item in hand")
                .placeNearTarget();

        scene.idle(20);

        scene.overlay()
                .showControls(util.vector().of(4.5, 1, 0.5), Pointing.DOWN, 20)
                .withItem(new ItemStack(MetalworksRegistrator.CAST_INGOT.get()))
                .rightClick();

        scene.idle(10);

        scene.world().modifyBlockEntity(util.grid().at(4, 0, 0), CastingBlockEntity.class, cast -> {
            cast.castInv.setStackInSlot(0, new ItemStack(MetalworksRegistrator.CAST_INGOT.get()));
        });

        scene.idle(40);

        scene.overlay()
                .showControls(util.vector().of(4.5, 2, 0.5), Pointing.DOWN, 30)
                .rightClick();

        scene.idle(10);

        scene.world().modifyBlockEntity(util.grid().at(4, 1, 0), FoundryTapBlockEntity.class, tap -> {
            tap.fluidId = BuiltInRegistries.FLUID.getId(MetalworksRegistrator.MOLTEN_GOLD.get());
            tap.isActive = true;
        });

        for (int i = 0; i < 30; i++) {
            scene.world().modifyBlockEntity(util.grid().at(4, 0, 0), CastingBlockEntity.class, cast -> cast.getFluidHandler().fill(new FluidStack(MetalworksRegistrator.MOLTEN_GOLD, 3), IFluidHandler.FluidAction.EXECUTE));
            scene.idle(1);
            scene.world().modifyBlockEntity(util.grid().at(3, 1, 1), FoundryControllerBlockEntity.class, controller -> controller.getFluidHandler().drain(3, IFluidHandler.FluidAction.EXECUTE));
        }

        scene.world().modifyBlockEntity(util.grid().at(4, 1, 0), FoundryTapBlockEntity.class, tap -> tap.isActive = false);

        scene.world().modifyBlockEntity(util.grid().at(4, 0, 0), CastingBlockEntity.class, cast -> {
            ((ItemStackHandler) cast.getItemHandler()).setStackInSlot(0, new ItemStack(Items.GOLD_INGOT));
            cast.coolingTime = 100;
        });

        for (int i = 0; i < 20; i++) {
            scene.world().modifyBlockEntity(util.grid().at(4, 0, 0), CastingBlockEntity.class, cast -> cast.coolingTime -= 3);
            scene.idle(1);
        }

        scene.world().modifyBlockEntity(util.grid().at(4, 0, 0), CastingBlockEntity.class, cast -> {
            cast.getFluidHandler().setFluid(FluidStack.EMPTY);
            cast.coolingTime = 0;
        });

        scene.idle(20);

        scene.overlay()
                .showControls(util.vector().of(4.5, 1, 0.5), Pointing.DOWN, 20)
                .rightClick();

        scene.idle(10);

        scene.world().modifyBlockEntity(util.grid().at(4, 0, 0), CastingBlockEntity.class, cast -> {
            ((ItemStackHandler) cast.getItemHandler()).setStackInSlot(0, ItemStack.EMPTY);
        });

        scene.idle(30);

        scene.world().modifyBlockEntity(util.grid().at(4, 0, 0), CastingBlockEntity.class, cast -> {
            cast.castInv.setStackInSlot(0, ItemStack.EMPTY);
        });

        scene.idle(50);
        scene.addKeyframe();

        scene.overlay()
                .showText(60)
                .pointAt(util.vector().of(4, 1, 0))
                .text("Additionally, you can create NEW Items by pouring liquids over items placed in the Casting Table or Basin")
                .placeNearTarget();

        scene.idle(70);

        scene.overlay()
                .showControls(util.vector().of(4.5, 1, 0.5), Pointing.DOWN, 20)
                .withItem(new ItemStack(Items.APPLE))
                .rightClick();

        scene.idle(10);

        scene.world().modifyBlockEntity(util.grid().at(4, 0, 0), CastingBlockEntity.class, cast -> {
            cast.castInv.setStackInSlot(0, new ItemStack(Items.APPLE));
        });

        scene.idle(40);

        scene.overlay()
                .showControls(util.vector().of(4.5, 2, 0.5), Pointing.DOWN, 30)
                .rightClick();

        scene.idle(10);

        scene.world().modifyBlockEntity(util.grid().at(4, 1, 0), FoundryTapBlockEntity.class, tap -> {
            tap.fluidId = BuiltInRegistries.FLUID.getId(MetalworksRegistrator.MOLTEN_GOLD.get());
            tap.isActive = true;
        });

        for (int i = 0; i < 40; i++) {
            scene.world().modifyBlockEntity(util.grid().at(4, 0, 0), CastingBlockEntity.class, cast -> cast.getFluidHandler().fill(new FluidStack(MetalworksRegistrator.MOLTEN_GOLD, 18), IFluidHandler.FluidAction.EXECUTE));
            scene.idle(1);
            scene.world().modifyBlockEntity(util.grid().at(3, 1, 1), FoundryControllerBlockEntity.class, controller -> controller.getFluidHandler().drain(18, IFluidHandler.FluidAction.EXECUTE));
        }

        scene.world().modifyBlockEntity(util.grid().at(4, 1, 0), FoundryTapBlockEntity.class, tap -> tap.isActive = false);

        scene.world().modifyBlockEntity(util.grid().at(4, 0, 0), CastingBlockEntity.class, cast -> {
            ((ItemStackHandler) cast.getItemHandler()).setStackInSlot(0, new ItemStack(Items.GOLDEN_APPLE));
            cast.coolingTime = 250;
        });

        for (int i = 0; i < 60; i++) {
            scene.world().modifyBlockEntity(util.grid().at(4, 0, 0), CastingBlockEntity.class, cast -> cast.coolingTime -= 3);
            scene.idle(1);
        }

        scene.world().modifyBlockEntity(util.grid().at(4, 0, 0), CastingBlockEntity.class, cast -> {
            cast.castInv.setStackInSlot(0, ItemStack.EMPTY);
            cast.getFluidHandler().setFluid(FluidStack.EMPTY);
            cast.coolingTime = 0;
        });

        scene.idle(20);

        scene.overlay()
                .showControls(util.vector().of(4.5, 1, 0.5), Pointing.DOWN, 20)
                .rightClick();

        scene.idle(10);

        scene.world().modifyBlockEntity(util.grid().at(4, 0, 0), CastingBlockEntity.class, cast -> {
            ((ItemStackHandler) cast.getItemHandler()).setStackInSlot(0, ItemStack.EMPTY);
        });

        scene.idle(30);
    }

    private static List<BlockPos> generateRingCoordinates(BlockPos bottomLeft, BlockPos topRight) {
        List<BlockPos> coords = new ArrayList<>();

        int x0 = bottomLeft.getX(),  z0 = bottomLeft.getZ();
        int x1 = topRight.getX(),    z1 = topRight.getZ();
        int y  = bottomLeft.getY();

        for (int z = z0; z <= z1; z++) {
            coords.add(new BlockPos(x0, y, z));
        }

        for (int x = x0 + 1; x <= x1; x++) {
            coords.add(new BlockPos(x, y, z1));
        }

        for (int z = z1 - 1; z >= z0; z--) {
            coords.add(new BlockPos(x1, y, z));
        }

        for (int x = x1 - 1; x > x0; x--) {
            coords.add(new BlockPos(x, y, z0));
        }

        return coords;
    }
}
