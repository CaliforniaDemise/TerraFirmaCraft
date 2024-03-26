package net.dries007.tfc.objects.items.abs;

import com.google.common.collect.ImmutableSet;
import net.dries007.tfc.api.capability.damage.DamageType;
import net.dries007.tfc.api.capability.size.IItemSize;
import net.dries007.tfc.api.capability.size.Size;
import net.dries007.tfc.api.capability.size.Weight;
import net.dries007.tfc.client.TFCSounds;
import net.dries007.tfc.objects.entity.projectile.EntityThrownJavelin;
import net.dries007.tfc.util.OreDictionaryHelper;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Enchantments;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

public abstract class ItemJavelin extends ItemTool implements IItemSize {

    public ItemJavelin(float attackDamage, float attackSpeed, ToolMaterial tool) {
        super(attackDamage, attackSpeed, tool, ImmutableSet.of());
        this.setMaxStackSize(1);
        OreDictionaryHelper.registerDamageType(this, DamageType.PIERCING);
        OreDictionaryHelper.register(this, "javelin");
    }

    protected abstract double getVelocityScalar();
    protected abstract double getInaccuracy();
    protected abstract double getThrownDamage();

    @Nonnull
    @Override
    public Size getSize(ItemStack stack) {
        return Size.LARGE; // Stored only in chests
    }

    @Nonnull
    @Override
    public Weight getWeight(ItemStack stack) {
        return Weight.MEDIUM;
    }

    @Override
    public boolean canStack(@Nonnull ItemStack stack) {
        return false;
    }

    @Override
    public int getMaxItemUseDuration(@Nonnull ItemStack itemStack) {
        return 72000;
    }

    @Nonnull
    @Override
    public EnumAction getItemUseAction(@Nonnull ItemStack itemStack) {
        return EnumAction.BOW;
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(@Nonnull World world, EntityPlayer player, @Nonnull EnumHand hand) {
        ItemStack itemStack = player.getHeldItem(hand);
        player.setActiveHand(hand);
        return new ActionResult<>(EnumActionResult.SUCCESS, itemStack);
    }

    protected float getVelocity(int charge) {
        float velocity = (float) charge / 20f;
        velocity = (velocity * velocity + velocity * 2f) / 3f;

        if (velocity > 1) {
            velocity = 1;
        }

        return velocity;
    }

    @ParametersAreNonnullByDefault
    @Override
    public void onPlayerStoppedUsing(ItemStack itemStack, World world, EntityLivingBase entityLiving, int timeLeft) {
        if (entityLiving instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) entityLiving;
            int i = this.getMaxItemUseDuration(itemStack) - timeLeft;

            if (i < 0) {
                return;
            }

            float velocity = this.getVelocity(i);

            if (velocity >= 0.1) {

                if (!world.isRemote) {

                    itemStack.damageItem(1, player);

                    EntityThrownJavelin javelin = new EntityThrownJavelin(world, player);
                    javelin.setDamage(this.getThrownDamage());
                    javelin.setWeapon(itemStack);

                    javelin.shoot(player, player.rotationPitch, player.rotationYaw, 0, (float) (velocity * this.getVelocityScalar()), (float) this.getInaccuracy());

                    if (velocity == 1) {
                        javelin.setIsCritical(true);
                    }

                    int j = EnchantmentHelper.getEnchantmentLevel(Enchantments.POWER, itemStack);

                    if (j > 0) {
                        javelin.setDamage(javelin.getDamage() + (double) j * 0.5 + 0.5);
                    }

                    int k = EnchantmentHelper.getEnchantmentLevel(Enchantments.PUNCH, itemStack);

                    if (k > 0) {
                        javelin.setKnockbackStrength(k);
                    }

                    if (EnchantmentHelper.getEnchantmentLevel(Enchantments.FLAME, itemStack) > 0) {
                        javelin.setFire(100);
                    }

                    if (player.capabilities.isCreativeMode) {
                        javelin.pickupStatus = EntityArrow.PickupStatus.CREATIVE_ONLY;
                    }

                    world.spawnEntity(javelin);
                }

                world.playSound(null, player.posX, player.posY, player.posZ, TFCSounds.ITEM_THROW, SoundCategory.PLAYERS, 1.0F, 1.0F / (itemRand.nextFloat() * 0.4F + 1.2F) + velocity * 0.5F);
                player.inventory.deleteStack(itemStack);
                player.addStat(StatList.getObjectUseStats(this));
            }

        }
    }

    @Override
    public boolean canDestroyBlockInCreative(World world, BlockPos pos, ItemStack stack, EntityPlayer player) {
        return false;
    }
}
