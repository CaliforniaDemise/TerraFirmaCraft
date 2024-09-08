package net.dries007.tfc.objects.items.rock;

import net.dries007.tfc.api.capability.size.Size;
import net.dries007.tfc.api.capability.size.Weight;
import net.dries007.tfc.api.nbt.HandstoneHorseData;
import net.dries007.tfc.client.TFCSounds;
import net.dries007.tfc.objects.te.TEQuern;
import net.dries007.tfc.util.Helpers;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.passive.AbstractHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
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
        EntityCreature creature = Helpers.getLeashedEntity(world, player, player.getPosition());
        return creature != null;
    }

    @Override
    public void use(World world, BlockPos pos, TEQuern quern, EntityPlayer player, EnumHand hand, ItemStack stack, INBTSerializable<NBTTagCompound> handstoneNBT) {
        HandstoneHorseData data = (HandstoneHorseData) handstoneNBT;
        if (data.hasWorker()) {
            EntityCreature creature = data.getWorker(world);
            creature.setLeashHolder(player, true);
            creature.setHomePosAndDistance(BlockPos.ORIGIN, -1);
        }
        else {
            EntityCreature creature = Helpers.getLeashedEntity(world, player, pos);
            if (creature != null) {
                creature.clearLeashed(true, false);
                creature.setHomePosAndDistance(pos, 3);
                data.setWorkerUUID(creature.getUniqueID());
            }
        }
    }

    @Override
    public void update(World world, BlockPos pos, TEQuern quern, ItemStack stack, HandstoneHorseData handstoneNBT) {
        EntityCreature creature = handstoneNBT.getWorker(world);
        if (creature != null) {
            creature.setHomePosAndDistance(pos, 3);
            ItemStack input = quern.getStackInSlot(TEQuern.SLOT_INPUT);
            if (!input.isEmpty() && !quern.isSlotFull(TEQuern.SLOT_OUTPUT) && quern.getRotationTimer() == 0) {
                quern.setRotationTimer(90);
                quern.markForBlockUpdate();
                world.playSound(null, pos, TFCSounds.QUERN_USE, SoundCategory.BLOCKS, 1, 1 + ((world.rand.nextFloat() - world.rand.nextFloat()) / 16));
            }
            if (quern.isGrinding()) {
                if (creature instanceof AbstractHorse && ((AbstractHorse) creature).isEatingHaystack()) {
                    ((AbstractHorse) creature).setEatingHaystack(false);
                }
                PathNavigate navigator = creature.getNavigator();
                if (navigator.noPath()) {
                    this.tryToMove(navigator, creature, pos, handstoneNBT);
                }
            }
        }
    }

    @Override
    public void afterGrind(World world, BlockPos pos, TEQuern quern, HandstoneHorseData handstoneNBT) {
        EntityCreature creature = handstoneNBT.getWorker(world);
        ItemStack input = quern.getStackInSlot(TEQuern.SLOT_INPUT);
        if (creature != null && input.isEmpty()) {
            creature.getNavigator().clearPath();
        }
    }

    private void tryToMove(PathNavigate navigator, EntityCreature creature, BlockPos quernPos, HandstoneHorseData data) {
        if (navigator.noPath()) {
            Vec3d[] locations = data.getLocations(quernPos);
            Vec3d nextLoc = null;
            for (int i = 0; i < locations.length; i++) {
                Vec3d v = locations[i];
                if (this.intersects(creature, v)) {
                    int next = i + 1;
                    if (next == locations.length) next = 0;
                    nextLoc = locations[next];
                    break;
                }
            }

            if (nextLoc == null) {
                double xDiff = creature.posX - quernPos.getX(), zDiff = creature.posZ - quernPos.getZ();
                int loc = 0;
                if (xDiff >= 0) loc += 2;
                if (zDiff <= 0) loc += 1;
                nextLoc = locations[loc];
            }

            if (nextLoc != null) {
                navigator.tryMoveToXYZ(nextLoc.x, nextLoc.y, nextLoc.z, 1.5D);
            }
        }
    }

    private boolean intersects(Entity entity, Vec3d position) {
        double minX = position.x - 2.0D, maxX = position.x + 2.0D;
        double minZ = position.z - 2.0D, maxZ = position.z + 2.0D;
        return entity.posX > minX && entity.posX < maxX && entity.posZ > minZ && entity.posZ < maxZ;
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

    @Override
    public boolean hasData(ItemStack stack) {
       return true;
    }
}
