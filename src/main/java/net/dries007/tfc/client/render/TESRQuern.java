/*
 * Work under Copyright. Licensed under the EUPL.
 * See the project README.md and LICENSE.txt for more information.
 */

package net.dries007.tfc.client.render;

import net.dries007.tfc.objects.items.rock.ItemHandstone;
import net.dries007.tfc.objects.te.TEQuern;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.lwjgl.opengl.GL11;

public class TESRQuern extends TileEntitySpecialRenderer<TEQuern> {
    @Override
    public void render(TEQuern te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        super.render(te, x, y, z, partialTicks, destroyStage, alpha);

        IItemHandler cap = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        if (cap != null) {
            ItemStack input = cap.getStackInSlot(TEQuern.SLOT_INPUT);
            ItemStack output = cap.getStackInSlot(TEQuern.SLOT_OUTPUT);
            ItemStack handstone = cap.getStackInSlot(TEQuern.SLOT_HANDSTONE);

            if (!output.isEmpty()) {
                for (int i = 0; i < output.getCount(); i++) {
                    double yPos = y + 0.625;
                    GlStateManager.enableRescaleNormal();
                    GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1f);
                    GlStateManager.enableBlend();
                    RenderHelper.enableStandardItemLighting();
                    GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
                    GlStateManager.pushMatrix();

                    switch (Math.floorDiv(i, 16)) {
                        case 0: {
                            GlStateManager.translate(x + 0.125, yPos, z + 0.125 + (0.046875 * i));
                            GlStateManager.rotate(75, 1, 0, 0);
                            break;
                        }
                        case 1: {
                            GlStateManager.translate(x + 0.125 + (0.046875 * (i - 16)), yPos, z + 0.875);
                            GlStateManager.rotate(90, 0, 1, 0);
                            GlStateManager.rotate(75, 1, 0, 0);
                            break;
                        }
                        case 2: {
                            GlStateManager.translate(x + 0.875, yPos, z + 0.875 - (0.046875 * (i - 32)));
                            GlStateManager.rotate(180, 0, 1, 0);
                            GlStateManager.rotate(75, 1, 0, 0);
                            break;
                        }
                        case 3: {
                            GlStateManager.translate(x + 0.875 - (0.046875 * (i - 48)), yPos, z + 0.125);
                            GlStateManager.rotate(270, 0, 1, 0);
                            GlStateManager.rotate(75, 1, 0, 0);
                            break;
                        }
                        default: {
                            GlStateManager.translate(x + 0.5, y + 1.0, z + 0.5);
                            GlStateManager.rotate((te.getWorld().getTotalWorldTime() + partialTicks) * 4, 0, 1, 0);
                        }
                    }

                    GlStateManager.scale(0.125, 0.125, 0.125);

                    IBakedModel outputModel = Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(output, te.getWorld(), null);
                    outputModel = ForgeHooksClient.handleCameraTransforms(outputModel, ItemCameraTransforms.TransformType.FIXED, false);

                    Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
                    Minecraft.getMinecraft().getRenderItem().renderItem(output, outputModel);

                    GlStateManager.popMatrix();
                    GlStateManager.disableRescaleNormal();
                    GlStateManager.disableBlend();
                }
            }

            if (!handstone.isEmpty()) {
                ItemHandstone<INBTSerializable<NBTTagCompound>> handstoneItem = (ItemHandstone<INBTSerializable<NBTTagCompound>>) handstone.getItem();
                handstoneItem.render(handstone, te, x, y, z, partialTicks, destroyStage, alpha, te.getHandstoneNBT());
            }

            if (!input.isEmpty()) {
                double height = (handstone.isEmpty()) ? 0.65 : 0.8;
                GlStateManager.enableRescaleNormal();
                GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1f);
                GlStateManager.enableBlend();
                RenderHelper.enableStandardItemLighting();
                GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
                GlStateManager.pushMatrix();
                GlStateManager.translate(x + 0.5, y + height, z + 0.5);
                GlStateManager.rotate(45, 0, 1, 0);
                GlStateManager.scale(0.5, 0.5, 0.5);

                IBakedModel inputModel = Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(input, te.getWorld(), null);
                inputModel = ForgeHooksClient.handleCameraTransforms(inputModel, ItemCameraTransforms.TransformType.GROUND, false);

                Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
                Minecraft.getMinecraft().getRenderItem().renderItem(input, inputModel);

                GlStateManager.popMatrix();
                GlStateManager.disableRescaleNormal();
                GlStateManager.disableBlend();
            }
        }
    }
}
