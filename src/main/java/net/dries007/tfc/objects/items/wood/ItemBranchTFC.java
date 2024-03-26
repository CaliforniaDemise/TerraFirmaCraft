package net.dries007.tfc.objects.items.wood;

import mcp.MethodsReturnNonnullByDefault;
import net.dries007.tfc.api.capability.size.Size;
import net.dries007.tfc.api.capability.size.Weight;
import net.dries007.tfc.api.types.Tree;
import net.dries007.tfc.objects.items.ItemTFC;
import net.dries007.tfc.util.OreDictionaryHelper;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ItemBranchTFC extends ItemTFC {

    private static final Map<Tree, ItemBranchTFC> MAP = new HashMap<>();

    public static ItemBranchTFC get(Tree tree) {
        return MAP.get(tree);
    }

    public static ItemStack get(Tree tree, int amount) {
        return new ItemStack(MAP.get(tree), amount);
    }

    public final Tree wood;

    public ItemBranchTFC(Tree wood) {
        this.wood = wood;
        if (MAP.put(wood, this) != null) throw new IllegalStateException("There can only be one.");
        setMaxDamage(0);
        OreDictionaryHelper.register(this, "branch");
        //noinspection ConstantConditions
        OreDictionaryHelper.register(this, "branch", wood.getRegistryName().getPath());
    }

    @Nonnull
    @Override
    public Size getSize(@Nonnull ItemStack stack) {
        return Size.SMALL;
    }

    @Nonnull
    @Override
    public Weight getWeight(@Nonnull ItemStack stack) {
        return Weight.VERY_LIGHT;
    }
}
