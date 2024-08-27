package net.dries007.tfc.objects.items.rock;

import net.dries007.tfc.api.capability.size.Size;
import net.dries007.tfc.api.capability.size.Weight;
import net.dries007.tfc.api.util.IHandstone;
import net.dries007.tfc.objects.items.ItemCraftingTool;

public class ItemHandstone extends ItemCraftingTool implements IHandstone {
    public ItemHandstone(int durability, Size size, Weight weight, Object... oreNameParts) {
        super(durability, size, weight, oreNameParts);
    }
}
