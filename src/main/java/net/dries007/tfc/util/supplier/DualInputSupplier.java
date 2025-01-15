package net.dries007.tfc.util.supplier;

@FunctionalInterface
public interface DualInputSupplier<I1, I2, O> {
    O get(I1 input1, I2 input2);
}
