package net.dries007.tfc.objects.items.rock;

import net.dries007.tfc.api.capability.size.Size;
import net.dries007.tfc.api.capability.size.Weight;
import net.dries007.tfc.api.util.IHandstone;
import net.dries007.tfc.client.TFCSounds;
import net.dries007.tfc.objects.items.ItemCraftingTool;
import net.dries007.tfc.objects.te.TEQuern;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

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
    public void use(World world, BlockPos pos, TEQuern quern, EntityPlayer player, EnumHand hand, ItemStack stack, @Nullable T handstoneNBT) {
        quern.setRotationTimer(90);
        quern.markForBlockUpdate();
        world.playSound(null, pos, TFCSounds.QUERN_USE, SoundCategory.BLOCKS, 1, 1 + ((world.rand.nextFloat() - world.rand.nextFloat()) / 16));
    }

    @SideOnly(Side.CLIENT)
    public void render(ItemStack handstone, TEQuern te, double x, double y, double z, float partialTicks, int destroyStage, float alpha, T handstoneNBT) {
        int rotationTicks = te.getRotationTimer();
        double center = (rotationTicks > 0) ? 0.497 + (te.getWorld().rand.nextDouble() * 0.006) : 0.5;

        GlStateManager.enableRescaleNormal();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1f);
        GlStateManager.enableBlend();
        RenderHelper.enableStandardItemLighting();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + center, y + 0.75, z + center);

        if (rotationTicks > 0) {
            GlStateManager.rotate((rotationTicks - partialTicks) * 4, 0, 1, 0);
        }

        IBakedModel handstoneModel = Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(handstone, te.getWorld(), null);
        handstoneModel = ForgeHooksClient.handleCameraTransforms(handstoneModel, ItemCameraTransforms.TransformType.FIXED, false);

        GlStateManager.scale(1.25, 1.25, 1.25);
        Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        Minecraft.getMinecraft().getRenderItem().renderItem(handstone, handstoneModel);

        GlStateManager.popMatrix();
        GlStateManager.disableRescaleNormal();
        GlStateManager.disableBlend();
    }
}
