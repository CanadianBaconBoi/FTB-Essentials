package dev.ftb.mods.ftbessentials.util.fabric;

import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

public class InventoryUtilImpl {
    @SuppressWarnings("UnstableApiUsage")
    public static NonNullList<ItemStack> getItemsInInventory(Level level, BlockPos pos, Direction side) {
        NonNullList<ItemStack> items = NonNullList.create();

        Storage<ItemVariant> storage = ItemStorage.SIDED.find(level, pos, side);
        if (storage != null) {
            storage.forEach(storageView -> {
                if (!storageView.isResourceBlank()) {
                    items.add(storageView.getResource().toStack((int) storageView.getAmount()));
                }
            });
        }

        return items;
    }

    @SuppressWarnings("UnstableApiUsage")
    public static boolean putItemsInInventory(List<ItemStack> items, Level level, BlockPos pos, Direction side) {
        Storage<ItemVariant> storage = ItemStorage.SIDED.find(level, pos, side);
        if (storage == null || !storage.supportsInsertion()) {
            throw new IllegalArgumentException("No item storage found");
        }

        try (Transaction tx = Transaction.openOuter()) {
            int ok = 0;
            for (ItemStack stack : items) {
                if (storage.insert(ItemVariant.of(stack), Integer.MAX_VALUE, tx) == stack.getCount()) {
                    ok++;
                } else {
                    break;
                }
            }
            if (ok == items.size()) {
                tx.commit();
                return true;
            } else {
                tx.abort();
                return false;
            }
        }
    }
}
