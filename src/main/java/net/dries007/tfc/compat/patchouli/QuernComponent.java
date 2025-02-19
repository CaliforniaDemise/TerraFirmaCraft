/*
 * Work under Copyright. Licensed under the EUPL.
 * See the project README.md and LICENSE.txt for more information.
 */

package net.dries007.tfc.compat.patchouli;

import com.google.gson.annotations.SerializedName;
import net.dries007.tfc.api.recipes.quern.QuernRecipe;
import net.dries007.tfc.api.registries.TFCRegistries;
import net.minecraft.util.ResourceLocation;
import vazkii.patchouli.api.VariableHolder;

import java.util.Objects;

@SuppressWarnings("unused")
public class QuernComponent extends SimpleRecipeComponent<QuernRecipe> {
    @VariableHolder
    @SerializedName("recipe")
    public String recipeName;

    @Override
    public void build(int componentX, int componentY, int pageNum) {
        Objects.requireNonNull(recipeName, "Recipe name is null?");
        recipe = TFCRegistries.QUERN.getValue(new ResourceLocation(recipeName));
        super.build(componentX, componentY, pageNum);
    }
}
