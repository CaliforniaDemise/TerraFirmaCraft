package net.dries007.tfc.objects.items.rock;

import net.dries007.tfc.api.capability.size.Size;
import net.dries007.tfc.api.capability.size.Weight;
import net.dries007.tfc.api.util.IHandstone;
import net.dries007.tfc.objects.items.ItemCraftingTool;
import net.dries007.tfc.objects.te.TEQuern;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class ItemHandstone extends ItemCraftingTool implements IHandstone {

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
}
