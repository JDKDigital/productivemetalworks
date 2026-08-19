package cy.jdkdigital.productivemetalworks.gametest;

import cy.jdkdigital.productivemetalworks.common.block.entity.CastingBlockEntity;
import cy.jdkdigital.productivemetalworks.common.block.entity.FoundryControllerBlockEntity;
import cy.jdkdigital.productivemetalworks.common.block.entity.FoundryTankBlockEntity;
import cy.jdkdigital.productivemetalworks.registry.MetalworksRegistrator;
import cy.jdkdigital.productivemetalworks.util.TickingSlotInventoryHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * In-game tests for the foundry multiblock.
 *
 * <p>Mirrors the productivebees gametest harness: test bodies are registered into
 * {@link TestFunctions} (published to {@code BuiltInRegistries.TEST_FUNCTION} from the mod
 * constructor), and {@link TestEntriesProvider} emits one {@code minecraft:function} test
 * instance per entry, each running inside the {@code productivemetalworks:empty_7x7} structure
 * from {@link GameTestStructureProvider}. The body places every block it needs.
 */
public final class MetalworksGameTests
{
    private MetalworksGameTests() {}

    private static final int DEFAULT_MAX_TICKS = 100;

    /** Per-test max-ticks budget; consumed by {@link TestEntriesProvider} when emitting JSONs. */
    public static final Map<String, Integer> MAX_TICKS = new LinkedHashMap<>();

    static {
        register("foundry_assembles_minimum_3x3x2", MetalworksGameTests::testFoundryAssembles);
        register("foundry_without_coil_floor_does_not_assemble", MetalworksGameTests::testFoundryRejectsMissingFloor);
        // Pour-and-cast is multi-tick: fill the casting block (10 mB/tick) + cooling (amount / foundryCoolingModifier=4)
        // + overhead. 600 covers the worst case (a full 810 mB block cast).
        register("foundry_tap_pours_molten_iron_into_basin_and_casts_block", MetalworksGameTests::testFoundryTapCastsIronBlock, 600);
        register("foundry_tap_pours_molten_iron_onto_table_and_casts_ingot", MetalworksGameTests::testFoundryTapCastsIronIngot, 600);
        register("foundry_tap_casts_productivebees_steel_bee_egg", MetalworksGameTests::testFoundryTapCastsBeeEgg, 600);
        register("foundry_basin_casts_capacitor_consuming_cast", MetalworksGameTests::testFoundryBasinCastsCapacitor, 600);
        register("casting_table_stores_cast_without_voiding", MetalworksGameTests::testCastingTableStoresCast);
        register("casting_table_cast_creation_leaves_empty_result_slot", MetalworksGameTests::testCastingTableCastCreationLeavesEmptyResult, 300);
        // Melting is slow: raw iron (180 mB) at lava's burn speed (0.5 mB/tick) ≈ 360 ticks + overhead.
        register("foundry_melts_raw_iron_into_molten_iron", MetalworksGameTests::testFoundryMeltsRawIron, 600);
        register("foundry_melt_progress_syncs_to_client", MetalworksGameTests::testFoundryMeltProgressSync);
        // Regression: item loaded before fuel must still melt once the foundry is fuelled.
        register("foundry_melts_raw_iron_added_before_fuel", MetalworksGameTests::testFoundryMeltsRawIronAddedBeforeFuel, 600);
    }

    private static void register(String name, Consumer<GameTestHelper> body) {
        register(name, body, DEFAULT_MAX_TICKS);
    }

    private static void register(String name, Consumer<GameTestHelper> body, int maxTicks) {
        TestFunctions.register(name, body);
        MAX_TICKS.put(name, maxTicks);
    }

    // The smallest valid foundry, per the in-game guide: 3×3×2. A 3×3 heating-coil floor (Y=1) with a
    // single wall ring above it (Y=2) enclosing one hollow interior air block. The controller sits on
    // the wall ring facing outward (NORTH). MultiBlockDetector.detectStructure walks the ring clockwise
    // from the controller and validates this to height=1, volume=1.
    private static final int FLOOR_Y = 1;
    private static final int WALL_Y = 2;
    private static final BlockPos CONTROLLER_POS = new BlockPos(1, WALL_Y, 0);
    private static final BlockPos TANK_POS = new BlockPos(1, WALL_Y, 2);     // south-wall middle, opposite the controller

    private static void testFoundryAssembles(GameTestHelper helper) {
        buildFoundry(helper, true);
        // Swap one wall block for a foundry tank (a peripheral) and fill it with lava as fuel.
        helper.setBlock(TANK_POS, MetalworksRegistrator.FOUNDRY_TANKS.get(DyeColor.BLACK).get().defaultBlockState());
        helper.getBlockEntity(TANK_POS, FoundryTankBlockEntity.class).getFluidHandler().fill(new FluidStack(Fluids.LAVA, 4000), true);

        // Right-click the controller with an empty hand — the real player path. useBlock spins up a
        // CREATIVE mock player and fires the interaction, so FoundryControllerBlock.useWithoutItem runs
        // detectMultiblock + setMultiBlockData (which flips the ATTACHED state).
        helper.useBlock(CONTROLLER_POS);

        BlockState controllerState = helper.getLevel().getBlockState(helper.absolutePos(CONTROLLER_POS));
        if (!controllerState.getValue(BlockStateProperties.ATTACHED)) {
            helper.fail("Controller is not ATTACHED after right-clicking a valid 3x3x2 foundry — it did not assemble", CONTROLLER_POS);
            return;
        }

        // Cross-check the backing multiblock data matches the expected minimum geometry.
        FoundryControllerBlockEntity be = helper.getBlockEntity(CONTROLLER_POS, FoundryControllerBlockEntity.class);
        var data = be.getMultiblockData();
        if (data == null) {
            helper.fail("Controller ATTACHED but has no multiblock data", CONTROLLER_POS);
            return;
        }
        if (data.height() != 1 || data.volume() != 1) {
            helper.fail("Unexpected foundry geometry: height=" + data.height() + " volume=" + data.volume()
                    + " (expected height=1, volume=1 for a 3x3x2)", CONTROLLER_POS);
            return;
        }

        // The tank should be wired in as a peripheral, so the controller reads its lava as fuel.
        FluidStack fuel = be.getFuel();
        if (!fuel.is(Fluids.LAVA) || fuel.getAmount() != 4000) {
            helper.fail("Controller did not see the tank's lava as fuel: "
                    + (fuel.isEmpty() ? "no fuel" : fuel.getAmount() + " mB of " + fuel.getFluid()), TANK_POS);
            return;
        }

        helper.succeed();
    }

    // Same structure but with the heating-coil floor omitted: right-clicking must NOT assemble it
    // (useWithoutItem swallows the InvalidStructureException), so ATTACHED stays false. Confirms the
    // assembly signal actually means something.
    private static void testFoundryRejectsMissingFloor(GameTestHelper helper) {
        buildFoundry(helper, false);

        helper.useBlock(CONTROLLER_POS);

        BlockState controllerState = helper.getLevel().getBlockState(helper.absolutePos(CONTROLLER_POS));
        if (controllerState.getValue(BlockStateProperties.ATTACHED)) {
            helper.fail("Controller became ATTACHED without a heating-coil floor — an invalid structure assembled", CONTROLLER_POS);
            return;
        }

        FoundryControllerBlockEntity be = helper.getBlockEntity(CONTROLLER_POS, FoundryControllerBlockEntity.class);
        if (be.getMultiblockData() != null) {
            helper.fail("Controller reports multiblock data for an invalid (floorless) structure", CONTROLLER_POS);
            return;
        }

        helper.succeed();
    }

    /**
     * Places a minimum foundry into the test area. {@code withFloor} controls whether the 3×3
     * heating-coil floor at {@link #FLOOR_Y} is laid down (false → an invalid, floorless shell).
     */
    private static void buildFoundry(GameTestHelper helper, boolean withFloor) {
        BlockState coil = MetalworksRegistrator.LIQUID_HEATING_COIL.get().defaultBlockState();
        BlockState wall = MetalworksRegistrator.FIRE_BRICKS.get(DyeColor.BLACK).get().defaultBlockState();
        BlockState controller = MetalworksRegistrator.FOUNDRY_CONTROLLERS.get(DyeColor.BLACK).get().defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH);

        if (withFloor) {
            for (int x = 0; x <= 2; x++) {
                for (int z = 0; z <= 2; z++) {
                    helper.setBlock(new BlockPos(x, FLOOR_Y, z), coil);
                }
            }
        }

        // Wall ring: the 3×3 perimeter at WALL_Y; the centre column (1, WALL_Y, 1) stays air (the interior).
        for (int x = 0; x <= 2; x++) {
            for (int z = 0; z <= 2; z++) {
                if (x == 1 && z == 1) {
                    continue;
                }
                BlockPos p = new BlockPos(x, WALL_Y, z);
                helper.setBlock(p, p.equals(CONTROLLER_POS) ? controller : wall);
            }
        }
    }

    // Functional foundry (lava-fuelled, FLUID coils) melts raw iron into molten iron.
    private static void testFoundryMeltsRawIron(GameTestHelper helper) {
        buildFoundry(helper, true);
        helper.setBlock(TANK_POS, MetalworksRegistrator.FOUNDRY_TANKS.get(DyeColor.BLACK).get().defaultBlockState());
        helper.getBlockEntity(TANK_POS, FoundryTankBlockEntity.class).getFluidHandler().fill(new FluidStack(Fluids.LAVA, 4000), true);

        helper.useBlock(CONTROLLER_POS);
        FoundryControllerBlockEntity controller = helper.getBlockEntity(CONTROLLER_POS, FoundryControllerBlockEntity.class);
        if (controller.getMultiblockData() == null) {
            helper.fail("Foundry did not assemble", CONTROLLER_POS);
            return;
        }
        // The melt timer is computed on insert from the active coil + fuel, so assemble and fuel first, then add the raw iron.
        ((TickingSlotInventoryHandler) controller.getItemHandler()).setStackInSlot(0, new ItemStack(Items.RAW_IRON));

        helper.succeedWhen(() -> {
            FoundryControllerBlockEntity c = helper.getBlockEntity(CONTROLLER_POS, FoundryControllerBlockEntity.class);
            int molten = moltenIron(c);
            if (molten < 180) {
                throw helper.assertionException(CONTROLLER_POS, "Raw iron has not melted yet (molten iron = " + molten + " mB, expected 180)");
            }
            if (!((TickingSlotInventoryHandler) c.getItemHandler()).getStackInSlot(0).isEmpty()) {
                throw helper.assertionException(CONTROLLER_POS, "Raw iron melted but the input item was not consumed");
            }
        });
    }

    // Right-clicking a casting table with a cast must store it in the cast slot and consume exactly one from
    // the held stack — never void the item (the generic handler used to reject the insert at slot 0).
    private static void testCastingTableStoresCast(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, MetalworksRegistrator.CASTING_TABLE.get().defaultBlockState());
        CastingBlockEntity be = helper.getBlockEntity(pos, CastingBlockEntity.class);

        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(MetalworksRegistrator.CAST_INGOT.get(), 2));
        helper.useBlock(pos, player);

        if (!be.castInv.getStackInSlot(0).is(MetalworksRegistrator.CAST_INGOT.get())) {
            helper.fail("Cast was not stored in the casting table's cast slot (it was voided)", pos);
            return;
        }
        if (player.getMainHandItem().getCount() != 1) {
            helper.fail("Expected exactly one cast consumed from the held stack (2 -> 1), got " + player.getMainHandItem().getCount(), pos);
            return;
        }
        helper.succeed();
    }

    private static void testCastingTableCastCreationLeavesEmptyResult(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, MetalworksRegistrator.CASTING_TABLE.get().defaultBlockState());
        CastingBlockEntity be = helper.getBlockEntity(pos, CastingBlockEntity.class);
        be.castInv.setStackInSlot(0, new ItemStack(Items.IRON_INGOT));

        int filled = be.getFluidHandler().fill(new FluidStack(MetalworksRegistrator.MOLTEN_STEEL.get(), 1000), true);
        if (filled != 360) {
            helper.fail("Expected the casting table to accept exactly the recipe's 360 mB of molten steel, accepted " + filled, pos);
            return;
        }

        helper.succeedWhen(() -> {
            CastingBlockEntity table = helper.getBlockEntity(pos, CastingBlockEntity.class);
            if (table.isCooling() || table.getFluidHandler().getFluidAmount() > 0) {
                throw helper.assertionException(pos, "Casting table is still cooling / holding fluid");
            }
            if (!table.castInv.getStackInSlot(0).is(MetalworksRegistrator.CAST_INGOT.get())) {
                throw helper.assertionException(pos, "Expected the finished ingot cast in the cast slot, found "
                        + (table.castInv.getStackInSlot(0).isEmpty() ? "nothing" : table.castInv.getStackInSlot(0).getItem()));
            }
            if (!table.getResultStack().isEmpty()) {
                throw helper.assertionException(pos, "The cast was duplicated: " + table.getResultStack().getCount() + "x "
                        + table.getResultStack().getItem() + " left in the result slot, which should be empty");
            }
        });
    }

    // Raw iron loaded into an assembled-but-unfuelled foundry has no melt timer yet; it must start melting
    // once lava is added (the timer is recalculated), not stay inert forever.
    private static void testFoundryMeltsRawIronAddedBeforeFuel(GameTestHelper helper) {
        buildFoundry(helper, true);
        helper.setBlock(TANK_POS, MetalworksRegistrator.FOUNDRY_TANKS.get(DyeColor.BLACK).get().defaultBlockState());

        helper.useBlock(CONTROLLER_POS);
        FoundryControllerBlockEntity controller = helper.getBlockEntity(CONTROLLER_POS, FoundryControllerBlockEntity.class);
        if (controller.getMultiblockData() == null) {
            helper.fail("Foundry did not assemble", CONTROLLER_POS);
            return;
        }
        // Insert raw iron while the tank is still empty — its melt timer initialises to 0 (no fuel).
        ((TickingSlotInventoryHandler) controller.getItemHandler()).setStackInSlot(0, new ItemStack(Items.RAW_IRON));
        // Now fuel the foundry; the raw iron must begin (and finish) melting.
        helper.getBlockEntity(TANK_POS, FoundryTankBlockEntity.class).getFluidHandler().fill(new FluidStack(Fluids.LAVA, 4000), true);

        helper.succeedWhen(() -> {
            FoundryControllerBlockEntity c = helper.getBlockEntity(CONTROLLER_POS, FoundryControllerBlockEntity.class);
            if (moltenIron(c) < 180) {
                throw helper.assertionException(CONTROLLER_POS, "Raw iron added before fuel never melted after the foundry was fuelled (molten iron = " + moltenIron(c) + " mB)");
            }
        });
    }

    // The melting indicator: progress lives in the per-slot ticker, which must (1) initialise on insert,
    // (2) ride along in the block-entity update packet the client GUI reads, and (3) animate via clientTick
    // between server syncs. This pins all three so a sync/animation regression is caught.
    private static void testFoundryMeltProgressSync(GameTestHelper helper) {
        buildFoundry(helper, true);
        helper.setBlock(TANK_POS, MetalworksRegistrator.FOUNDRY_TANKS.get(DyeColor.BLACK).get().defaultBlockState());
        helper.getBlockEntity(TANK_POS, FoundryTankBlockEntity.class).getFluidHandler().fill(new FluidStack(Fluids.LAVA, 4000), true);

        helper.useBlock(CONTROLLER_POS);
        FoundryControllerBlockEntity controller = helper.getBlockEntity(CONTROLLER_POS, FoundryControllerBlockEntity.class);
        if (controller.getMultiblockData() == null) {
            helper.fail("Foundry did not assemble", CONTROLLER_POS);
            return;
        }
        TickingSlotInventoryHandler items = (TickingSlotInventoryHandler) controller.getItemHandler();
        items.setStackInSlot(0, new ItemStack(Items.RAW_IRON));

        // 1. Inserting initialises the melt timer (total = the recipe's fluid amount).
        if (items.getTicker(0).getSecond() != 180) {
            helper.fail("Melt timer was not initialised on insert (total=" + items.getTicker(0).getSecond() + ", expected 180)", CONTROLLER_POS);
            return;
        }

        // 2. The progress is serialised into the block-entity update packet the client GUI reads.
        CompoundTag updateTag = controller.getUpdateTag(helper.getLevel().registryAccess());
        CompoundTag inv = updateTag.getCompound("inv").orElse(new CompoundTag());
        if (inv.getInt("t0_total").orElse(0) != 180) {
            helper.fail("Melt progress (t0_total) is missing from the sync packet — the GUI indicator can't read it", CONTROLLER_POS);
            return;
        }

        // 3. clientTick animates the bar locally between syncs; if it doesn't, the indicator freezes.
        int before = items.getTicker(0).getFirst();
        BlockState state = controller.getBlockState();
        BlockPos abs = helper.absolutePos(CONTROLLER_POS);
        for (int i = 0; i < 40; i++) {
            FoundryControllerBlockEntity.clientTick(helper.getLevel(), abs, state, controller);
        }
        if (items.getTicker(0).getFirst() >= before) {
            helper.fail("Client melt animation did not advance (ticker stuck at " + before + ") — clientTick isn't progressing the indicator", CONTROLLER_POS);
            return;
        }

        helper.succeed();
    }

    private static int moltenIron(FoundryControllerBlockEntity controller) {
        var tank = controller.getFluidHandler();
        int total = 0;
        for (int i = 0; i < tank.getTanks(); i++) {
            FluidStack fluid = tank.getFluidInTank(i);
            if (fluid.is(MetalworksRegistrator.MOLTEN_IRON.get())) {
                total += fluid.getAmount();
            }
        }
        return total;
    }

    // Foundry tap → casting block pour-and-cast. Asserts extraction gating across filling → cooling → done.
    private static final BlockPos DRAIN_POS = new BlockPos(2, WALL_Y, 1);          // east-wall middle
    private static final BlockPos TAP_POS = new BlockPos(3, WALL_Y, 1);            // attached east of the drain
    private static final BlockPos CAST_POS = new BlockPos(3, FLOOR_Y, 1);         // casting block directly under the tap

    // Basin (no cast): molten iron → iron block, 810 mB.
    private static void testFoundryTapCastsIronBlock(GameTestHelper helper) {
        runTapCastTest(helper, MetalworksRegistrator.CASTING_BASIN.get().defaultBlockState(), ItemStack.EMPTY, 810,
                MetalworksRegistrator.MOLTEN_IRON.get(), s -> s.is(Items.IRON_BLOCK), "iron block", false);
    }

    // Table with a reusable ingot cast: molten iron → iron ingot, 90 mB; the cast must survive the pour.
    private static void testFoundryTapCastsIronIngot(GameTestHelper helper) {
        runTapCastTest(helper, MetalworksRegistrator.CASTING_TABLE.get().defaultBlockState(),
                new ItemStack(MetalworksRegistrator.CAST_INGOT.get()), 90,
                MetalworksRegistrator.MOLTEN_IRON.get(), s -> s.is(Items.IRON_INGOT), "iron ingot", false);
    }

    // Table with a productivebees iron-bee egg as the (consumed) cast: molten steel transmutes it into a
    // steel-bee egg. Exercises a cross-mod ComponentIngredient cast and an NBT (entity_data) result.
    private static void testFoundryTapCastsBeeEgg(GameTestHelper helper) {
        Item eggItem = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("productivebees", "spawn_egg_configurable_bee"));
        // productivebees keys its bees by category path, so the egg's entity_data type is the full key.
        runTapCastTest(helper, MetalworksRegistrator.CASTING_TABLE.get().defaultBlockState(),
                beeEgg(eggItem, "productivebees:raw_materials/iron"), 810,
                MetalworksRegistrator.MOLTEN_STEEL.get(), s -> isBeeEgg(s, eggItem, "productivebees:alloys/steel"), "steel bee egg", true);
    }

    // Basin with fire bricks as the (consumed) cast: molten redstone makes a capacitor, not a redstone block.
    // Guards cast-vs-cast-less matching — the cast-less redstone-block recipe shares the fluid and must not win
    // while a cast is present.
    private static void testFoundryBasinCastsCapacitor(GameTestHelper helper) {
        Item bricks = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("productivemetalworks", "black_fire_bricks"));
        Item capacitor = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("productivemetalworks", "black_foundry_capacitor"));
        runTapCastTest(helper, MetalworksRegistrator.CASTING_BASIN.get().defaultBlockState(),
                new ItemStack(bricks), 1000,
                MetalworksRegistrator.MOLTEN_REDSTONE.get(), s -> s.is(capacitor), "foundry capacitor", true);
    }

    private static void runTapCastTest(GameTestHelper helper, BlockState castingBlock, ItemStack cast, int recipeAmount,
                                       net.minecraft.world.level.material.Fluid fuel,
                                       java.util.function.Predicate<ItemStack> resultMatches, String resultDesc, boolean castConsumed) {
        buildFoundry(helper, true);
        helper.setBlock(DRAIN_POS, MetalworksRegistrator.FOUNDRY_DRAINS.get(DyeColor.BLACK).get().defaultBlockState());
        helper.setBlock(TAP_POS, MetalworksRegistrator.FOUNDRY_TAP.get().defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST)); // faces out; opposite points at the drain
        helper.setBlock(CAST_POS, castingBlock);

        helper.startSequence()
                .thenExecute(() -> {
                    helper.useBlock(CONTROLLER_POS);
                    FoundryControllerBlockEntity controller = helper.getBlockEntity(CONTROLLER_POS, FoundryControllerBlockEntity.class);
                    if (controller.getMultiblockData() == null) {
                        helper.fail("Foundry did not assemble", CONTROLLER_POS);
                        return;
                    }
                    if (!cast.isEmpty()) {
                        helper.getBlockEntity(CAST_POS, CastingBlockEntity.class).castInv.setStackInSlot(0, cast.copy());
                    }
                    // Seed more than the recipe needs so the test proves the casting block caps the inflow.
                    int filled = controller.getFluidHandler().fill(new FluidStack(fuel, 1000), true);
                    if (filled < recipeAmount) {
                        helper.fail("Could not seed the controller with fuel (filled " + filled + " mB, need >= " + recipeAmount + ")", CONTROLLER_POS);
                        return;
                    }
                    helper.useBlock(TAP_POS);
                })
                // Filling: locked.
                .thenWaitUntil(() -> {
                    CastingBlockEntity cb = helper.getBlockEntity(CAST_POS, CastingBlockEntity.class);
                    if (cb.getFluidHandler().getFluidAmount() <= 0) {
                        throw helper.assertionException(CAST_POS, "Tap has not begun pouring into the casting block");
                    }
                })
                .thenExecute(() -> {
                    if (!hopperExtract(helper).isEmpty()) {
                        helper.fail("A hopper pulled from the casting block while it was still filling — output should be locked", CAST_POS);
                    }
                })
                // Cooling: capped at the recipe amount, result forming, still locked.
                .thenWaitUntil(() -> {
                    CastingBlockEntity cb = helper.getBlockEntity(CAST_POS, CastingBlockEntity.class);
                    if (!cb.isCooling()) {
                        throw helper.assertionException(CAST_POS, "Casting block has not started cooling yet");
                    }
                })
                .thenExecute(() -> {
                    CastingBlockEntity cb = helper.getBlockEntity(CAST_POS, CastingBlockEntity.class);
                    int amount = cb.getFluidHandler().getFluidAmount();
                    if (amount != recipeAmount) {
                        helper.fail("Casting block overshot the recipe amount: holding " + amount + " mB, expected exactly " + recipeAmount, CAST_POS);
                        return;
                    }
                    if (!resultMatches.test(cb.getResultStack())) {
                        helper.fail("Expected " + resultDesc + " forming during cooling, found "
                                + (cb.getResultStack().isEmpty() ? "nothing" : cb.getResultStack().getItem()), CAST_POS);
                        return;
                    }
                    if (!hopperExtract(helper).isEmpty()) {
                        helper.fail("A hopper pulled the result while it was still cooling — output should be locked", CAST_POS);
                    }
                })
                // Done: drained, extractable.
                .thenWaitUntil(() -> {
                    CastingBlockEntity cb = helper.getBlockEntity(CAST_POS, CastingBlockEntity.class);
                    if (cb.isCooling() || cb.getFluidHandler().getFluidAmount() > 0) {
                        throw helper.assertionException(CAST_POS, "Casting block is still cooling / holding fluid");
                    }
                })
                .thenExecute(() -> {
                    ItemStack output = hopperExtract(helper);
                    if (!resultMatches.test(output) || output.getCount() != 1) {
                        helper.fail("Expected a hopper to extract exactly 1 " + resultDesc + " once cast, got "
                                + (output.isEmpty() ? "nothing" : output.getCount() + "x " + output.getItem()), CAST_POS);
                        return;
                    }
                    if (!cast.isEmpty()) {
                        ItemStack remaining = helper.getBlockEntity(CAST_POS, CastingBlockEntity.class).castInv.getStackInSlot(0);
                        if (castConsumed && !remaining.isEmpty()) {
                            helper.fail("Cast should have been consumed but " + remaining.getItem() + " remains", CAST_POS);
                        } else if (!castConsumed && !remaining.is(cast.getItem())) {
                            helper.fail("Reusable cast was lost (cast slot now " + (remaining.isEmpty() ? "empty" : remaining.getItem()) + ")", CAST_POS);
                        }
                    }
                })
                .thenSucceed();
    }

    private static ItemStack beeEgg(Item eggItem, String beeType) {
        CompoundTag tag = new CompoundTag();
        tag.putString("type", beeType);
        tag.putString("id", "productivebees:configurable_bee");
        ItemStack stack = new ItemStack(eggItem);
        stack.set(DataComponents.ENTITY_DATA, TypedEntityData.of(EntityType.byString("productivebees:configurable_bee").orElseThrow(), tag));
        return stack;
    }

    private static boolean isBeeEgg(ItemStack stack, Item eggItem, String beeType) {
        if (!stack.is(eggItem)) {
            return false;
        }
        var data = stack.get(DataComponents.ENTITY_DATA);
        return data != null && beeType.equals(data.copyTagWithoutId().getString("type").orElse(""));
    }

    // Pull from the casting block the way a hopper underneath it would: Item capability on the DOWN face.
    private static ItemStack hopperExtract(GameTestHelper helper) {
        ResourceHandler<ItemResource> handler = helper.getLevel().getCapability(
                Capabilities.Item.BLOCK, helper.absolutePos(CAST_POS), Direction.DOWN);
        if (handler == null) {
            return ItemStack.EMPTY;
        }
        try (Transaction tx = Transaction.openRoot()) {
            ItemResource resource = handler.getResource(0);
            if (resource.isEmpty()) {
                return ItemStack.EMPTY;
            }
            int extracted = handler.extract(0, resource, 64, tx);
            if (extracted <= 0) {
                return ItemStack.EMPTY;
            }
            tx.commit();
            return resource.toStack(extracted);
        }
    }
}
