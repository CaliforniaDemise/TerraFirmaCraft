/*
 * Work under Copyright. Licensed under the EUPL.
 * See the project README.md and LICENSE.txt for more information.
 */

package net.dries007.tfc.objects.items.rock;

import mcp.MethodsReturnNonnullByDefault;
import net.dries007.tfc.api.types.Rock;
import net.dries007.tfc.api.types.RockCategory;
import net.dries007.tfc.api.util.IRockObject;
import net.dries007.tfc.objects.items.abs.ItemJavelin;
import net.dries007.tfc.util.OreDictionaryHelper;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ItemRockJavelin extends ItemJavelin implements IRockObject {
    private static final Map<RockCategory, ItemRockJavelin> MAP = new HashMap<>();

    public static ItemRockJavelin get(RockCategory category) {
        return MAP.get(category);
    }

    public final RockCategory category;

    public ItemRockJavelin(RockCategory category) {
        super(-0.3f * category.getToolMaterial().getAttackDamage(), -1.3f, category.getToolMaterial());
        this.category = category;
        if (MAP.put(category, this) != null) {
            throw new IllegalStateException("There can only be one.");
        }

        setMaxDamage((int) (category.getToolMaterial().getMaxUses() * 0.1f));

        OreDictionaryHelper.register(this, "javelin", "stone");
        OreDictionaryHelper.register(this, "javelin", "stone", category);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        tooltip.add("Rock type: " + OreDictionaryHelper.toString(category));
    }

    // IRockObject Implementation
    @Nullable
    @Override
    public Rock getRock(ItemStack stack) {
        return null;
    }

    @Nonnull
    @Override
    public RockCategory getRockCategory(ItemStack stack) {
        return category;
    }

    // ItemJavelin Implementation
    @Override
    protected double getVelocityScalar() {
        return category.getToolMaterial().getEfficiency() / 4f;
    }

    @Override
    protected double getInaccuracy() {
        return 0.5f;
    }

    @Override
    protected double getThrownDamage() {
        return 2.5f * category.getToolMaterial().getAttackDamage();
    }
}
