package net.dries007.tfc.api.nbt;

import net.minecraft.entity.EntityLiving;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.UUID;

public class HandstoneHorseData implements INBTSerializable<NBTTagCompound> {

    private final BlockPos pos;
    private UUID workerUUID = null;

    private EntityLiving worker = null;

    public HandstoneHorseData(BlockPos pos) {
        this.pos = pos;
    }

    public BlockPos getPos() {
        return pos;
    }

    public EntityLiving getWorker(World world) {
        BlockPos leashPos = this.pos;
        if (worker == null && this.workerUUID != null) {
            int i = leashPos.getX();
            int j = leashPos.getY();
            int k = leashPos.getZ();
            for (EntityLiving entityLiving : world.getEntitiesWithinAABB(EntityLiving.class, new AxisAlignedBB((double)i - 7.0D, (double)j - 7.0D, (double)k - 7.0D, (double)i + 7.0D, (double)j + 7.0D, (double)k + 7.0D))) {
                if (entityLiving.getUniqueID().equals(this.workerUUID)) {
                    this.worker = entityLiving;
                    break;
                }
            }
        }
        if (worker != null && worker.isDead) {
            this.workerUUID = null;
            this.worker = null;
        }
        return worker;
    }

    public void setWorkerUUID(UUID workerUUID) {
        this.workerUUID = workerUUID;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        if (this.workerUUID != null) tag.setUniqueId("uuid", workerUUID);
        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        if (nbt.hasKey("uuid")) this.workerUUID = nbt.getUniqueId("uuid");
    }
}
