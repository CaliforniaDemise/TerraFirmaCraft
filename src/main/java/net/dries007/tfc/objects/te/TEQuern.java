/*
 * Work under Copyright. Licensed under the EUPL.
 * See the project README.md and LICENSE.txt for more information.
 */

package net.dries007.tfc.objects.te;

import net.dries007.tfc.api.capability.food.CapabilityFood;
import net.dries007.tfc.api.recipes.quern.QuernRecipe;
import net.dries007.tfc.api.util.IHandstone;
import net.dries007.tfc.objects.items.ItemsTFC;
import net.dries007.tfc.util.Helpers;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.Explosion;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import static net.minecraft.init.SoundEvents.*;

@ParametersAreNonnullByDefault
public class TEQuern extends TEInventory implements ITickable {
    public static final int SLOT_HANDSTONE = 0;
    public static final int SLOT_INPUT = 1;
    public static final int SLOT_OUTPUT = 2;

    private int rotationTimer;
    private boolean hasHandstone;

    private INBTSerializable<NBTTagCompound> handstoneNBT = null;

    public TEQuern() {
        super(3);
        rotationTimer = 0;
    }

    public ItemStack insertOrSwapItem(int slot, ItemStack playerStack) {
        ItemStack quernStack = inventory.getStackInSlot(slot);
        if (!playerStack.isEmpty()) {
            if (!this.isItemValid(slot, playerStack)) return playerStack;
            if (quernStack.isEmpty()) {
                return this.inventory.insertItem(slot, playerStack, false);
            }
            else if (playerStack.isStackable() && ItemStack.areItemStacksEqual(quernStack, playerStack)) {
                int toAdd = Math.min(quernStack.getMaxStackSize() - quernStack.getCount(), playerStack.getCount());
                quernStack.setCount(quernStack.getCount() + toAdd);
                playerStack.shrink(toAdd);
                if (playerStack.getCount() == 0) playerStack = ItemStack.EMPTY;
                return playerStack;
            }
        }
        if (!quernStack.isEmpty()) {
            if (playerStack.isEmpty()) {
                this.inventory.setStackInSlot(slot, ItemStack.EMPTY);
                return quernStack;
            }
            else if (playerStack.isStackable() && ItemStack.areItemStacksEqual(quernStack, playerStack)) {
                int toAdd = Math.min(playerStack.getMaxStackSize() - playerStack.getCount(), quernStack.getCount());
                playerStack.setCount(playerStack.getCount() + toAdd);
                quernStack.shrink(toAdd);
                if (quernStack.getCount() == 0) this.inventory.setStackInSlot(slot, ItemStack.EMPTY);
                return playerStack;
            }
        }

        return playerStack;
    }

    public INBTSerializable<NBTTagCompound> getHandstoneNBT() {
        return handstoneNBT;
    }

    @Override
    public int getSlotLimit(int slot) {
        return slot == SLOT_HANDSTONE ? 1 : 64;
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        switch (slot) {
            case SLOT_HANDSTONE:
                return stack.getItem() instanceof IHandstone;
            case SLOT_INPUT:
                return QuernRecipe.get(stack) != null;
            default:
                return false;
        }
    }

    @Override
    public void setAndUpdateSlots(int slot) {
        markForBlockUpdate();
        if (slot == SLOT_HANDSTONE) {
            ItemStack handstoneStack = inventory.getStackInSlot(slot);
            hasHandstone = !handstoneStack.isEmpty();
            if (hasHandstone) {
                IHandstone<?> handstone = (IHandstone<?>) handstoneStack.getItem();
                if (handstone.hasData(this.world, this.pos, this, handstoneStack)) {
                    if (handstoneNBT == null) handstoneNBT = handstone.createNBT(this.world, this.pos, this, handstoneStack);
                }
                else handstoneNBT = null;
            }
            else this.handstoneNBT = null;
        }
        super.setAndUpdateSlots(slot);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        rotationTimer = nbt.getInteger("rotationTimer");
        super.readFromNBT(nbt);
        hasHandstone = !inventory.getStackInSlot(SLOT_HANDSTONE).isEmpty();
        if (hasHandstone) {
            ItemStack handstoneStack = inventory.getStackInSlot(SLOT_HANDSTONE);
            IHandstone<?> handstoneItem = (IHandstone<?>) handstoneStack.getItem();
            if (handstoneItem.hasData(this.world, this.pos, this, handstoneStack)) {
                this.handstoneNBT = handstoneItem.createNBT(this.world, this.pos, this, handstoneStack);
            }
            if (handstoneNBT != null) handstoneNBT.deserializeNBT(nbt.getCompoundTag("handstoneNBT"));
        }
    }

    @Override
    @Nonnull
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setInteger("rotationTimer", rotationTimer);
        if (handstoneNBT != null) nbt.setTag("handstoneNBT", handstoneNBT.serializeNBT());
        return super.writeToNBT(nbt);
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return super.canInteractWith(player) && rotationTimer == 0;
    }

    public int getRotationTimer() {
        return rotationTimer;
    }

    public void setRotationTimer(int rotationTimer) {
        this.rotationTimer = rotationTimer;
    }

    public boolean isGrinding() {
        return rotationTimer > 0;
    }

    public boolean hasHandstone() {
        return hasHandstone;
    }

    public boolean grind(EntityPlayer player, EnumHand hand) {
        ItemStack handstoneStack = inventory.getStackInSlot(SLOT_HANDSTONE);
        IHandstone<INBTSerializable<NBTTagCompound>> handstone = (IHandstone<INBTSerializable<NBTTagCompound>>) handstoneStack.getItem();
        if (handstone.canUse(this.world, this.pos, this, player, hand, handstoneStack, this.handstoneNBT)) {
            handstone.use(this.world, this.pos, this, player, hand, handstoneStack, this.handstoneNBT);
            return true;
        }
        return false;
    }

    @Override
    public void update() {
        if (rotationTimer > 0) {
            rotationTimer--;

            if (rotationTimer == 0) {
                grindItem();
                world.playSound(null, pos, ENTITY_ARMORSTAND_FALL, SoundCategory.BLOCKS, 1.0f, 0.8f);
                Helpers.damageItem(inventory.getStackInSlot(SLOT_HANDSTONE));

                if (inventory.getStackInSlot(SLOT_HANDSTONE).isEmpty()) {
                    for (int i = 0; i < 15; i++) {
                        world.spawnParticle(EnumParticleTypes.ITEM_CRACK, pos.getX() + 0.5D, pos.getY() + 0.875D, pos.getZ() + 0.5D, (world.rand.nextDouble() - world.rand.nextDouble()) / 4, world.rand.nextDouble() / 4, (world.rand.nextDouble() - world.rand.nextDouble()) / 4, Item.getIdFromItem(ItemsTFC.HANDSTONE));
                    }
                    world.playSound(null, pos, BLOCK_STONE_BREAK, SoundCategory.BLOCKS, 1.0f, 0.8f);
                    world.playSound(null, pos, ENTITY_ITEM_BREAK, SoundCategory.BLOCKS, 1.0f, 0.6f);
                }

                setAndUpdateSlots(SLOT_HANDSTONE);
            }
        }

        if (handstoneNBT != null) {
            ItemStack stack = inventory.getStackInSlot(SLOT_HANDSTONE);
            if (stack.isEmpty()) return;
            IHandstone handstone = (IHandstone) stack.getItem();
            handstone.update(this.world, this.pos, this, stack, handstoneNBT);
        }
    }

    @Nonnull
    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(getPos(), getPos().add(1, 2, 1));
    }

    public void onPlayerBreak(EntityPlayer player) {
        ItemStack handstoneStack = inventory.getStackInSlot(SLOT_HANDSTONE);
        if (!handstoneStack.isEmpty()) {
            IHandstone<INBTSerializable<NBTTagCompound>> handstone = (IHandstone<INBTSerializable<NBTTagCompound>>) handstoneStack.getItem();
            handstone.onPlayerBreak(this.world, this.pos, player, this, handstoneStack, this.handstoneNBT);
        }
    }

    public void onExplosionBreak(Explosion explosion) {
        ItemStack handstoneStack = inventory.getStackInSlot(SLOT_HANDSTONE);
        if (!handstoneStack.isEmpty()) {
            IHandstone<INBTSerializable<NBTTagCompound>> handstone = (IHandstone<INBTSerializable<NBTTagCompound>>) handstoneStack.getItem();
            handstone.onExplosionBreak(this.world, this.pos, explosion, this, handstoneStack, this.handstoneNBT);
        }
    }

    public boolean isSlotFull(int slot) {
        ItemStack stack = this.inventory.getStackInSlot(slot);
        if (stack.isEmpty()) return false;
        int stackSize = stack.getCount();
        return stackSize == stack.getMaxStackSize() || stackSize == this.inventory.getSlotLimit(slot);
    }

    public ItemStack getStackInSlot(int slot) {
        return inventory.getStackInSlot(slot);
    }

    private void grindItem() {
        ItemStack inputStack = inventory.getStackInSlot(SLOT_INPUT);
        if (inputStack.isEmpty()) return;
        QuernRecipe recipe = QuernRecipe.get(inputStack);
        if (recipe != null && !world.isRemote) {
            inputStack.shrink(recipe.getIngredients().get(0).getAmount());
            ItemStack outputStack = recipe.getOutputItem(inputStack);
            outputStack = inventory.insertItem(SLOT_OUTPUT, outputStack, false);
            inventory.setStackInSlot(SLOT_OUTPUT, CapabilityFood.mergeItemStacksIgnoreCreationDate(inventory.getStackInSlot(SLOT_OUTPUT), outputStack));
            if (!outputStack.isEmpty()) {
                // Still having leftover items, dumping in world
                InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY() + 1, pos.getZ(), outputStack);
            }
        }
        ItemStack handstoneStack = this.inventory.getStackInSlot(SLOT_HANDSTONE);
        if (!handstoneStack.isEmpty()) {
            IHandstone<INBTSerializable<NBTTagCompound>> handstone = (IHandstone<INBTSerializable<NBTTagCompound>>) handstoneStack.getItem();
            handstone.afterGrind(this.world, this.pos, this, handstoneStack, this.handstoneNBT);
        }
    }
}
