package com.minimammoth.ironoak.helper;

import com.minimammoth.ironoak.BootstrappedGame;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The defaults every container in this mod inherits.
 *
 * <p>{@code FireBowlEntity} overrides four of these purely to add the client sync, so what
 * is left underneath is the behaviour those overrides delegate to. Two of the defaults are
 * surprising enough to be worth pinning: {@code removeItem} marks dirty only when it
 * actually removed something, and {@code setItem} clamps <em>after</em> it has already
 * stored the stack, which means it mutates the caller's instance rather than a copy.
 */
@ExtendWith(BootstrappedGame.class)
class ImplementedInventoryTest {

    @Test
    void aFreshInventoryIsEmpty() {
        ImplementedInventory inventory = ImplementedInventory.ofSize(2);

        assertEquals(2, inventory.getContainerSize());
        assertTrue(inventory.isEmpty());
    }

    @Test
    void oneOccupiedSlotIsEnoughToNotBeEmpty() {
        ImplementedInventory inventory = ImplementedInventory.ofSize(2);
        inventory.setItem(1, new ItemStack(Items.STICK));

        assertFalse(inventory.isEmpty());
    }

    @Test
    void removingPartOfAStackLeavesTheRest() {
        ImplementedInventory inventory = ImplementedInventory.ofSize(2);
        inventory.setItem(0, new ItemStack(Items.STICK, 5));

        ItemStack removed = inventory.removeItem(0, 2);

        assertEquals(2, removed.getCount());
        assertEquals(3, inventory.getItem(0).getCount());
    }

    @Test
    void removingFromAnEmptySlotYieldsNothing() {
        assertTrue(ImplementedInventory.ofSize(2).removeItem(0, 1).isEmpty());
    }

    @Test
    void clearingEmptiesEverySlot() {
        ImplementedInventory inventory = ImplementedInventory.ofSize(2);
        inventory.setItem(0, new ItemStack(Items.STICK));
        inventory.setItem(1, new ItemStack(Items.COAL));

        inventory.clearContent();

        assertTrue(inventory.isEmpty());
    }

    /**
     * Written down because it is a trap, not because it is right: the stack is stored first
     * and clamped second, so the clamp lands on the instance the caller still holds.
     * Anything that hands the same stack to two containers gets the second clamp too. If
     * this ever changes to clamp a copy, this test is the thing that will notice.
     */
    @Test
    void oversizedStacksAreClampedInPlace() {
        ImplementedInventory inventory = ImplementedInventory.ofSize(2);
        ItemStack oversized = new ItemStack(Items.STICK, 200);

        inventory.setItem(0, oversized);

        assertSame(oversized, inventory.getItem(0));
        assertEquals(inventory.getMaxStackSize(), inventory.getItem(0).getCount());
        assertEquals(inventory.getMaxStackSize(), oversized.getCount(), "the caller's own stack was clamped too");
    }
}
