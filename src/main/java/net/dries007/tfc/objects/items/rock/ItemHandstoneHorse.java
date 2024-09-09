package net.dries007.tfc.objects.items.rock;

import net.dries007.tfc.api.capability.size.Size;
import net.dries007.tfc.api.capability.size.Weight;
import net.dries007.tfc.api.nbt.HandstoneHorseData;
import net.dries007.tfc.client.TFCSounds;
import net.dries007.tfc.objects.te.TEQuern;
import net.dries007.tfc.util.Helpers;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.AbstractHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemHandstoneHorse extends ItemHandstone<HandstoneHorseData> {

    private static final AxisAlignedBB HANDLE_AABB = new AxisAlignedBB(0.462375D, 1.0D, 0.462375D, 0.54D, 1.175D, 0.54D);

    public ItemHandstoneHorse(int durability, Size size, Weight weight, Object... oreNameParts) {
        super(durability, size, weight, oreNameParts);
    }

    @Override
    public boolean canUse(World world, BlockPos pos, TEQuern quern, EntityPlayer player, EnumHand hand, ItemStack stack, HandstoneHorseData handstoneNBT) {
        if (handstoneNBT == null) return false;
        if (!handstoneNBT.hasWorker()) {
            EntityCreature creature = Helpers.getLeashedEntity(world, player, player.getPosition());
            return creature != null;
        }
        else {
            return true;
        }
    }

    @Override
    public void use(World world, BlockPos pos, TEQuern quern, EntityPlayer player, EnumHand hand, ItemStack stack, HandstoneHorseData handstoneNBT) {
        if (handstoneNBT == null) return;
        if (handstoneNBT.hasWorker()) {
            EntityCreature creature = handstoneNBT.getWorker(world);
            creature.setLeashHolder(player, true);
            creature.setHomePosAndDistance(BlockPos.ORIGIN, -1);
            handstoneNBT.setWorkerUUID(null);
        }
        else {
            EntityCreature creature = Helpers.getLeashedEntity(world, player, pos);
            if (creature != null) {
                creature.clearLeashed(true, false);
                creature.setHomePosAndDistance(pos, 3);
                handstoneNBT.setWorkerUUID(creature.getUniqueID());
            }
        }
        quern.markForBlockUpdate();
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
    public void afterGrind(World world, BlockPos pos, TEQuern quern, ItemStack stack, HandstoneHorseData handstoneNBT) {
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
    public void onBreak(World world, BlockPos pos, TEQuern quern, ItemStack stack, HandstoneHorseData handstoneNBT) {
        if (handstoneNBT == null) return;
        if (handstoneNBT.hasWorker()) {
            EntityCreature creature = handstoneNBT.getWorker(world);
            creature.setHomePosAndDistance(BlockPos.ORIGIN, -1);
            if (!world.isRemote) {
                EntityItem lead = new EntityItem(world, creature.posX, creature.posY, creature.posZ, new ItemStack(Items.LEAD));
                world.spawnEntity(lead);
            }
        }
    }

    @Override
    public boolean hasData(World world, BlockPos pos, TEQuern quern, ItemStack stack) {
       return true;
    }

    @Override
    public HandstoneHorseData createNBT(World world, BlockPos pos, TEQuern quern, ItemStack stack) {
        return new HandstoneHorseData(pos.add(0.0D, 0.35D, 0.0D));
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void render(ItemStack handstone, TEQuern te, double x, double y, double z, float partialTicks, int destroyStage, float alpha, HandstoneHorseData handstoneNBT) {
        super.render(handstone, te, x, y, z, partialTicks, destroyStage, alpha, handstoneNBT);
        if (handstoneNBT.hasWorker()) {
            EntityCreature creature = handstoneNBT.getWorker(te.getWorld());
            BlockPos pos = te.getPos();
            this.renderLeash(creature, x, y + 0.35D, z, 0D, 0D, 0D, partialTicks, new Vec3d(pos.getX(), pos.getY(), pos.getZ()));
        }
    }

    protected void renderLeash(EntityCreature entity, double ox, double oy, double oz, double x, double y, double z, float partialTicks, Vec3d pos) {
        if (entity != null) {
            oy = oy - 0.7D;
            double d2;
            double d3;
            double d4 = -1.0D;

            double d9 = this.interpolateValue(entity.prevRenderYawOffset, entity.renderYawOffset, partialTicks) * 0.01745329238474369D + (Math.PI / 2D);
            d2 = Math.cos(d9) * (double)entity.width * 0.4D;
            d3 = Math.sin(d9) * (double)entity.width * 0.4D;
            double d6 = (this.interpolateValue(entity.prevPosX, entity.posX, partialTicks)) + d2;
            double d7 = this.interpolateValue(entity.prevPosY + entity.getEyeHeight() * 1.1D, entity.posY + entity.getEyeHeight() * 1.1D, partialTicks) - d4 * 0.5D - 0.25D - y;
            double d8 = (this.interpolateValue(entity.prevPosZ, entity.posZ, partialTicks)) + d3;

            d2 = 0.5D;
            d3 = 0.5D;
            double d10 = pos.x + d2;
            double d11 = pos.y;
            double d12 = pos.z + d3;
            ox += d2 + x;
            oz += d3 + z;
            oy += y;

            renderLeach(d6, d7, d8, ox, oy, oz, d10, d11, d12);
        }
    }

    protected void renderLeach(double x1, double y1, double z1, double ox, double oy, double oz, double x2, double y2, double z2) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder vertexbuffer = tessellator.getBuffer();

        double d13 = ((float)(x1 - x2));
        double d14 = ((float)(y1 - y2));
        double d15 = ((float)(z1 - z2));

        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();

        vertexbuffer.begin(5, DefaultVertexFormats.POSITION_COLOR);

        for (int j = 0; j <= 24; ++j) {
            float f = 0.5F;
            float f1 = 0.4F;
            float f2 = 0.3F;

            if (j % 2 == 0) {
                f *= 0.7F;
                f1 *= 0.7F;
                f2 *= 0.7F;
            }

            float f3 = (float)j / 24.0F;
            vertexbuffer.pos(ox + d13 * (double)f3 + 0.0D, oy + d14 * (double)(f3 * f3 + f3) * 0.5D + (double)((24.0F - (float)j) / 18.0F + 0.125F), oz + d15 * (double)f3).color(f, f1, f2, 1.0F).endVertex();
            vertexbuffer.pos(ox + d13 * (double)f3 + 0.025D, oy + d14 * (double)(f3 * f3 + f3) * 0.5D + (double)((24.0F - (float)j) / 18.0F + 0.125F) + 0.025D, oz + d15 * (double)f3).color(f, f1, f2, 1.0F).endVertex();
        }

        tessellator.draw();
        vertexbuffer.begin(5, DefaultVertexFormats.POSITION_COLOR);

        for (int k = 0; k <= 24; ++k) {
            float f4 = 0.5F;
            float f5 = 0.4F;
            float f6 = 0.3F;

            if (k % 2 == 0) {
                f4 *= 0.7F;
                f5 *= 0.7F;
                f6 *= 0.7F;
            }

            float f7 = (float)k / 24.0F;
            vertexbuffer.pos(ox + d13 * (double)f7 + 0.0D, oy + d14 * (double)(f7 * f7 + f7) * 0.5D + (double)((24.0F - (float)k) / 18.0F + 0.125F) + 0.025D, oz + d15 * (double)f7).color(f4, f5, f6, 1.0F).endVertex();
            vertexbuffer.pos(ox + d13 * (double)f7 + 0.025D, oy + d14 * (double)(f7 * f7 + f7) * 0.5D + (double)((24.0F - (float)k) / 18.0F + 0.125F), oz + d15 * (double)f7 + 0.025D).color(f4, f5, f6, 1.0F).endVertex();
        }

        tessellator.draw();
        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.enableCull();
    }

    private double interpolateValue(double start, double end, double pct) {
        return start + (end - start) * pct;
    }
}
