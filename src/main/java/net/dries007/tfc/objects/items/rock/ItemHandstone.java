package net.dries007.tfc.objects.items.rock;

import net.dries007.tfc.api.capability.size.Size;
import net.dries007.tfc.api.capability.size.Weight;
import net.dries007.tfc.api.util.IHandstone;
import net.dries007.tfc.client.TFCSounds;
import net.dries007.tfc.objects.items.ItemCraftingTool;
import net.dries007.tfc.objects.te.TEQuern;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nullable;

public class ItemHandstone<T extends INBTSerializable<NBTTagCompound>> extends ItemCraftingTool implements IHandstone<T> {

    private static final AxisAlignedBB HANDSTONE_AABB = new AxisAlignedBB(0.1875D, 0.625D, 0.1875D, 0.8125D, 0.86D, 0.8125D);
    private static final AxisAlignedBB HANDLE_AABB = new AxisAlignedBB(0.27125D, 0.86D, 0.27125D, 0.335D, 1.015D, 0.335D);

    public ItemHandstone(int durability, Size size, Weight weight, Object... oreNameParts) {
        super(durability, size, weight, oreNameParts);
    }

    @Override
    public AxisAlignedBB getBoundingBox(World world, BlockPos pos, TEQuern quern, Entity entity, ItemStack stack) {
        return HANDSTONE_AABB;
    }

    @Nullable
    @Override
    public AxisAlignedBB getHandleBoundingBox(World world, BlockPos pos, TEQuern quern, Entity entity, ItemStack stack) {
        return HANDLE_AABB;
    }

    @Override
    public void use(World world, BlockPos pos, TEQuern quern, EntityPlayer player, EnumHand hand, ItemStack stack, @Nullable INBTSerializable<NBTTagCompound> handstoneNBT) {
        quern.setRotationTimer(90);
        quern.markForBlockUpdate();
        world.playSound(null, pos, TFCSounds.QUERN_USE, SoundCategory.BLOCKS, 1, 1 + ((world.rand.nextFloat() - world.rand.nextFloat()) / 16));
    }

    public void afterGrind(World world, BlockPos pos, TEQuern quern, T handstoneNBT) {}

    public boolean hasData(ItemStack stack) {
        return false;
    }
}
