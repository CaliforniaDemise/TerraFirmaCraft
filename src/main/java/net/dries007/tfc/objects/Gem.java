/*
 * Work under Copyright. Licensed under the EUPL.
 * See the project README.md and LICENSE.txt for more information.
 */

package net.dries007.tfc.objects;

import net.dries007.tfc.objects.items.ItemGem;
import net.dries007.tfc.util.supplier.DualInputSupplier;
import net.dries007.tfc.util.collections.WeightedCollection;
import net.minecraft.init.Items;
import net.minecraft.item.Item;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Locale;
import java.util.Random;
import java.util.stream.Collectors;

public enum Gem {
    AGATE,
    AMETHYST,
    BERYL,
    DIAMOND((gem, grade) -> grade == Grade.NORMAL ? Items.DIAMOND : new ItemGem(gem, grade)),
    EMERALD((gem, grade) -> grade == Grade.NORMAL ? Items.EMERALD : new ItemGem(gem, grade)),
    GARNET,
    JADE,
    JASPER,
    OPAL,
    RUBY,
    SAPPHIRE,
    TOPAZ,
    TOURMALINE;

    /**
     * Returns a random gem type according to gem type availabilities
     *
     * @param random Random generator for rolling odds
     * @return a random drop gem type
     */
    public static Gem getRandomDropGem(Random random) {
        int index = random.nextInt(12);
        if (index > 2) index++;
        return values()[index];
    }

    // Items listed by grade ordinal
    private final Item[] items = new Item[5];

    // Item getter by grade, used for getting items that already exists in vanilla or generating one if needed.
    private final DualInputSupplier<Gem, Grade, Item> supplier;

    Gem() {
        this.supplier = ItemGem::new;
    }

    Gem(DualInputSupplier<Gem, Grade, Item> supplier) {
        this.supplier = supplier;
    }

    public Item register(Grade grade) {
        Item item = this.supplier.get(this, grade);
        if ((this != DIAMOND && this != EMERALD) || grade != Grade.NORMAL) {
            return item;
        }
        this.items[grade.ordinal()] = item;
        return Items.AIR;
    }

    public Item getItem(Grade grade) {
        return this.items[grade.ordinal()];
    }

    public enum Grade {
        CHIPPED(16),
        FLAWED(8),
        NORMAL(4),
        FLAWLESS(2),
        EXQUISITE(1);

        private static final WeightedCollection<Grade> GRADE_ODDS = new WeightedCollection<>(Arrays.stream(values()).collect(Collectors.toMap(k -> k, v -> v.dropWeight)));

        private final String name;
        private final double dropWeight;

        Grade(int dropWeight) {
            this.name = this.name().toLowerCase(Locale.US);
            this.dropWeight = dropWeight;
        }

        public String getName() {
            return name;
        }

        /**
         * Returns a random gem grade according to gem grade weights
         *
         * @param random Random generator for rolling the odds
         * @return a random drop gem grade
         */
        @Nonnull
        public static Grade randomGrade(Random random) {
            return GRADE_ODDS.getRandomEntry(random);
        }

        @Nullable
        public static Grade valueOf(int index) {
            switch (index) {
                case 0: return CHIPPED;
                case 1: return FLAWED;
                case 2: return NORMAL;
                case 3: return FLAWLESS;
                case 4: return EXQUISITE;
                default: return null;
            }
        }
    }
}
