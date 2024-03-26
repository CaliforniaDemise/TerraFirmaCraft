/*
 * Work under Copyright. Licensed under the EUPL.
 * See the project README.md and LICENSE.txt for more information.
 */

package net.dries007.tfc.objects.items.metal;

import net.dries007.tfc.api.types.Metal;
import net.dries007.tfc.client.TFCSounds;
import net.dries007.tfc.objects.entity.projectile.EntityThrownJavelin;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Enchantments;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class ItemMetalJavelin extends ItemMetalTool {
    public ItemMetalJavelin(Metal metal, Metal.ItemType type) {
        super(metal, type);

        ToolMaterial material = metal.getToolMetal();
        if (material != null) {
            setMaxDamage((int) (material.getMaxUses() * 0.1));
        }
    }

    @Override
    @Nonnull
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, @Nonnull EnumHand handIn) {
        if (type == Metal.ItemType.JAVELIN) {
            ItemStack itemstack = playerIn.getHeldItem(handIn);
            playerIn.setActiveHand(handIn);
            return new ActionResult<>(EnumActionResult.SUCCESS, itemstack);
        }
        return super.onItemRightClick(worldIn, playerIn, handIn);
    }

    @Override
    @Nonnull
    public EnumAction getItemUseAction(ItemStack stack) {
        return EnumAction.BOW;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 72000;
    }

    protected float getVelocity(int charge) {
        float velocity = (float) charge / 20f;
        velocity = (velocity * velocity + velocity * 2f) / 3f;

        if (velocity > 1) {
            velocity = 1;
        }

        return velocity;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World world, EntityLivingBase entityLiving, int timeLeft) {
        if (entityLiving instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) entityLiving;
            int i = this.getMaxItemUseDuration(stack) - timeLeft;

            if (i < 0) {
                return;
            }

            float velocity = getVelocity(timeLeft);

            if (velocity >= 0.1) {

                if (!world.isRemote) {

                    stack.damageItem(1, player);

                    EntityThrownJavelin javelin = new EntityThrownJavelin(world, player);
                    javelin.setDamage(this.metal.getToolMetal().getAttackDamage());
                    javelin.setWeapon(stack);

                    javelin.shoot(player, player.rotationPitch, player.rotationYaw, 0, (float) (velocity * this.metal.getToolMetal().getEfficiency() / 4f), 0.5f);

                    if (velocity == 1) {
                        javelin.setIsCritical(true);
                    }

                    int j = EnchantmentHelper.getEnchantmentLevel(Enchantments.POWER, stack);

                    if (j > 0) {
                        javelin.setDamage(javelin.getDamage() + (double) j * 0.5 + 0.5);
                    }

                    int k = EnchantmentHelper.getEnchantmentLevel(Enchantments.PUNCH, stack);

                    if (k > 0) {
                        javelin.setKnockbackStrength(k);
                    }

                    if (EnchantmentHelper.getEnchantmentLevel(Enchantments.FLAME, stack) > 0) {
                        javelin.setFire(100);
                    }

                    if (player.capabilities.isCreativeMode) {
                        javelin.pickupStatus = EntityArrow.PickupStatus.CREATIVE_ONLY;
                    }

                    world.spawnEntity(javelin);
                }

                world.playSound(null, player.posX, player.posY, player.posZ, TFCSounds.ITEM_THROW, SoundCategory.PLAYERS, 1.0F, 1.0F / (itemRand.nextFloat() * 0.4F + 1.2F) + velocity * 0.5F);
                player.inventory.deleteStack(stack);
                player.addStat(StatList.getObjectUseStats(this));
            }

        }
    }
}
