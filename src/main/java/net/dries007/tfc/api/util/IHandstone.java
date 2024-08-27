package net.dries007.tfc.api.util;

import net.dries007.tfc.objects.te.TEQuern;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IHandstone {
    default boolean canUse(World world, BlockPos pos, TEQuern quern, EntityPlayer player, ItemStack stack) {
        return true;
    }

    default void use(World world, BlockPos pos, TEQuern quern, EntityPlayer player, ItemStack stack) {}
}
