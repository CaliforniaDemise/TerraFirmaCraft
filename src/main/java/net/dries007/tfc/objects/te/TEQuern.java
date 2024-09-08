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
import net.minecraftforge.common.util.INBTSerializable;

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
        if (quernStack.isEmpty() || (playerStack.isStackable() && quernStack.isStackable() && quernStack.getItem() == playerStack.getItem() && (!playerStack.getHasSubtypes() || playerStack.getMetadata() == quernStack.getMetadata()) && ItemStack.areItemStackTagsEqual(playerStack, quernStack))) {
            return inventory.insertItem(slot, playerStack, false);
        }
        inventory.setStackInSlot(slot, playerStack);
        return quernStack;
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
            if (hasHandstone) handstoneNBT = ((IHandstone) handstoneStack.getItem()).createNBT(this.world, this.pos, this);
            else handstoneNBT = null;
        }
        super.setAndUpdateSlots(slot);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        rotationTimer = nbt.getInteger("rotationTimer");
        super.readFromNBT(nbt);
        hasHandstone = !inventory.getStackInSlot(SLOT_HANDSTONE).isEmpty();
        if (hasHandstone) {
            ItemStack handstone = inventory.getStackInSlot(SLOT_HANDSTONE);
            handstoneNBT = ((IHandstone) handstone.getItem()).createNBT(this.world, this.pos, this);
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
        IHandstone handstone = (IHandstone) handstoneStack.getItem();
        if (handstone.canUse(this.world, this.pos, this, player, hand, handstoneStack, handstoneNBT)) {
            handstone.use(this.world, this.pos, this, player, hand, handstoneStack, handstoneNBT);
            return true;
        }
        return false;
    }

    @Override
    public void update() {
        if (handstoneNBT != null) {
            ItemStack stack = inventory.getStackInSlot(SLOT_HANDSTONE);
            IHandstone handstone = (IHandstone) stack.getItem();
            handstone.update(this.world, this.pos, this, stack, handstoneNBT);
        }

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
    }

    @Nonnull
    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(getPos(), getPos().add(1, 2, 1));
    }

    private void grindItem() {
        ItemStack inputStack = inventory.getStackInSlot(SLOT_INPUT);
        if (!inputStack.isEmpty()) {
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
        }
    }
}
