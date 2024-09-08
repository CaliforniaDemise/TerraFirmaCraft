package net.dries007.tfc.objects.items.rock;

import net.dries007.tfc.api.capability.size.Size;
import net.dries007.tfc.api.capability.size.Weight;
import net.dries007.tfc.api.nbt.HandstoneHorseData;
import net.dries007.tfc.objects.te.TEQuern;
import net.dries007.tfc.util.Helpers;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLeashKnot;
import net.minecraft.entity.EntityLiving;
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

public class ItemHandstoneHorse extends ItemHandstone<HandstoneHorseData> {

    private static final AxisAlignedBB HANDLE_AABB = new AxisAlignedBB(0.4225D, 0.785D, 0.4225D, 0.582D, 1.095D, 0.582D);

    public ItemHandstoneHorse(int durability, Size size, Weight weight, Object... oreNameParts) {
        super(durability, size, weight, oreNameParts);
    }

    @Override
    public boolean canUse(World world, BlockPos pos, TEQuern quern, EntityPlayer player, EnumHand hand, ItemStack stack, INBTSerializable<NBTTagCompound> handstoneNBT) {
        EntityLiving entityLiving = Helpers.getLeashedEntity(world, player, player.getPosition());
        return entityLiving != null;
    }

    @Override
    public void use(World world, BlockPos pos, TEQuern quern, EntityPlayer player, EnumHand hand, ItemStack stack, INBTSerializable<NBTTagCompound> handstoneNBT) {
        if (world.isRemote) return;
        EntityLiving entityLiving = Helpers.getLeashedEntity(world, player, pos);
        if (entityLiving != null) {
            entityLiving.clearLeashed(true, false);
            HandstoneHorseData data = (HandstoneHorseData) handstoneNBT;
            data.setWorkerUUID(entityLiving.getUniqueID());
        }
    }

    @Override
    public void update(World world, BlockPos pos, TEQuern quern, ItemStack stack, HandstoneHorseData handstoneNBT) {
        if (world.isRemote) return;
//        if (quern.getRotationTimer() != 0) {
//            EntityLiving entityLiving = handstoneNBT.getWorker(world);
//            if (entityLiving != null) {
//            }
//        }
    }

    @Override
    public HandstoneHorseData createNBT(World world, BlockPos pos, TEQuern quern) {
        return new HandstoneHorseData(pos.add(0.0D, 0.35D, 0.0D));
    }

    @Nonnull
    @Override
    public AxisAlignedBB[] getCollisionBoxes(World world, BlockPos pos, TEQuern quern, @Nullable Entity entity, ItemStack stack) {
        return new AxisAlignedBB[] { getBoundingBox(world, pos, quern, entity, stack), HANDLE_AABB };
    }

    @Nullable
    @Override
    public AxisAlignedBB getHandleBoundingBox(World world, BlockPos pos, TEQuern quern, Entity entity, ItemStack stack) {
        return HANDLE_AABB;
    }
}
