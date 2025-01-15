/*
 * Work under Copyright. Licensed under the EUPL.
 * See the project README.md and LICENSE.txt for more information.
 */

package net.dries007.tfc.objects.items;

import mcp.MethodsReturnNonnullByDefault;
import net.dries007.tfc.api.capability.size.Size;
import net.dries007.tfc.api.capability.size.Weight;
import net.dries007.tfc.objects.Gem;
import net.dries007.tfc.util.OreDictionaryHelper;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ItemGem extends ItemTFC {

    public static ItemStack get(Gem gem, Gem.Grade grade, int amount) {
        return new ItemStack(gem.getItem(grade), amount);
    }

    private final Gem gem;
    private final Gem.Grade grade;

    public ItemGem(Gem gem, Gem.Grade grade) {
        this.gem = gem;
        this.grade = grade;
        setMaxDamage(0);
        setHasSubtypes(true);
        if (grade == Gem.Grade.NORMAL) OreDictionaryHelper.register(this, "gem", gem);
        else OreDictionaryHelper.register(this, "gem", grade, gem);
        OreDictionaryHelper.register(this, "gem", grade);
    }

    public Gem getGem() {
        return gem;
    }

    public Gem.Grade getGrade() {
        return grade;
    }

    @Nonnull
    @Override
    public Size getSize(ItemStack stack) {
        return Size.SMALL; // Stored anywhere
    }

    @Nonnull
    @Override
    public Weight getWeight(ItemStack stack) {
        return Weight.VERY_LIGHT; // Stacksize = 64
    }
}
