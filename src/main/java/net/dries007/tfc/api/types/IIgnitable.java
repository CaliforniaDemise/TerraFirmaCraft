package net.dries007.tfc.api.types;

import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.OreDictionaryHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IIgnitable {
    default boolean onIgnition(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (stack.isEmpty()) return false;

        boolean fireStarter = OreDictionaryHelper.doesStackMatchOre(stack, "fireStarter");
        boolean infiniteFire = OreDictionaryHelper.doesStackMatchOre(stack, "infiniteFire");

        if (fireStarter || infiniteFire) {
            if (fireStarter) {
                if (stack.getItem().isDamageable()) {
                    Helpers.damageItem(stack);
                } else stack.shrink(1);
            }
            return true;
        }
        return false;
    }
}
