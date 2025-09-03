package cy.jdkdigital.productivemetalworks.util;

import com.mojang.datafixers.util.Pair;
import cy.jdkdigital.productivelib.common.block.entity.InventoryHandlerHelper;
import cy.jdkdigital.productivemetalworks.ProductiveMetalworks;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

abstract public class TickingSlotInventoryHandler extends InventoryHandlerHelper.BlockEntityItemStackHandler
{
    // index = slot, Pair<timeLeft, maxTime>
    List<Pair<Integer, Integer>> tickers;

    public TickingSlotInventoryHandler(int size, @Nullable BlockEntity blockEntity) {
        super(size, blockEntity);
        initTickers();
    }

    protected abstract int getTimeInSlot(ItemStack stack);

    @Override
    public void setSize(int size) {
        super.setSize(size);
        if (size != this.size()) {
            initTickers();
            recalculate();
        }
    }

    protected void initTickers() {
        tickers = new ArrayList<>();
        for (int i = 0; i < size(); i++) {
            tickers.add(Pair.of(0, 0));
        }
    }

    public void resetTicker(int slot) {
        if (slot < tickers.size()) {
            tickers.set(slot, Pair.of(0, 0));
        }
    }

    @NotNull
    @Override
    public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate, boolean fromAutomation) {
        var returnStack = super.insertItem(slot, stack, simulate, fromAutomation);
        if (!simulate && returnStack.getCount() != stack.getCount() && slot < tickers.size()) {
            int time = getTimeInSlot(stack);
            tickers.set(slot, Pair.of(time, time));
        }
        return returnStack;
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        if (!stack.isEmpty() && slot < tickers.size() && this.blockEntity.hasLevel() && !this.blockEntity.getLevel().isClientSide) {
            int time = getTimeInSlot(stack);
            tickers.set(slot, Pair.of(time, time));
        }
        super.setStackInSlot(slot, stack);
        this.onContentsChanged(slot);
    }

    @NotNull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate, boolean fromAutomation) {
        var returnStack = super.extractItem(slot, amount, simulate, fromAutomation);
        if (!simulate && !returnStack.isEmpty()) {
            resetTicker(slot);
        }
        return returnStack;
    }

    public void tick(int t) {
        for (int slot = 0; slot < tickers.size(); slot++) {
            Pair<Integer, Integer> pair = tickers.get(slot);
            if (pair.getSecond() > 0 && pair.getFirst() > 0) {
                tickers.set(slot, Pair.of(Math.max(pair.getFirst() - t, 0), pair.getSecond()));
            }

            if (pair.getSecond() == 0 && getStackInSlot(slot).isEmpty()) {
                // if the stack is not empty and there's no valid ticker, reset it
                recalculate(slot);
            }
        }
    }

    public void recalculate() {
        for (int slot = 0; slot < tickers.size(); slot++) {
            recalculate(slot);
        }
    }

    public void recalculate(int slot) {
        Pair<Integer, Integer> pair = tickers.get(slot);
        int time = getTimeInSlot(getStackInSlot(slot));
        tickers.set(slot, Pair.of(Math.max(time - (pair.getSecond() - pair.getFirst()), 0), time));
    }

    public Pair<Integer, Integer> getTicker(int slot) {
        return tickers.size() > slot ? tickers.get(slot) : Pair.of(0, 0);
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag nbt = super.serializeNBT(provider);
        for (int slot = 0; slot < Math.min(size(), tickers.size()); slot++) {
            // Save the amount of time that has passed
            if (tickers.get(slot).getSecond() > 0) {
                nbt.putInt("t" + slot + "_passed", tickers.get(slot).getFirst());
                nbt.putInt("t" + slot + "_total", tickers.get(slot).getSecond());
            }
        }
        return nbt;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        super.deserializeNBT(provider, nbt);
        initTickers();
        for (int slot = 0; slot < size(); slot++) {
            if (nbt.contains("t" + slot + "_total") && slot < tickers.size()) {
                int time = nbt.getInt("t" + slot + "_total");
                tickers.set(slot, Pair.of(nbt.getInt("t" + slot + "_passed"), time));
            }
        }
    }
}
