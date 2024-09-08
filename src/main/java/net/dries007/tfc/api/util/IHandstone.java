package net.dries007.tfc.api.util;

import net.dries007.tfc.objects.te.TEQuern;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IHandstone<T extends INBTSerializable<NBTTagCompound>> {

    default boolean canUse(World world, BlockPos pos, TEQuern quern, EntityPlayer player, EnumHand hand, ItemStack stack, @Nullable INBTSerializable<NBTTagCompound> handstoneNBT) {
        return true;
    }

    default void use(World world, BlockPos pos, TEQuern quern, EntityPlayer player, EnumHand hand, ItemStack stack, @Nullable INBTSerializable<NBTTagCompound> handstoneNBT) {}

    default void update(World world, BlockPos pos, TEQuern quern, ItemStack stack, @Nullable T handstoneNBT) {}

    default T createNBT(World world, BlockPos pos, TEQuern quern) {
        return null;
    }

    @Nonnull
    default AxisAlignedBB[] getCollisionBoxes(World world, BlockPos pos, TEQuern quern, @Nullable Entity entity, ItemStack stack) {
        AxisAlignedBB HANDSTONE_BB = getBoundingBox(world, pos, quern, entity, stack);
        if (HANDSTONE_BB != null) return new AxisAlignedBB[] { HANDSTONE_BB };
        return new AxisAlignedBB[0];
    }

    @Nullable
    AxisAlignedBB getBoundingBox(World world, BlockPos pos, TEQuern quern, @Nullable Entity entity, ItemStack stack);

    @Nullable
    AxisAlignedBB getHandleBoundingBox(World world, BlockPos pos, TEQuern quern, @Nullable Entity entity, ItemStack stack);
}
