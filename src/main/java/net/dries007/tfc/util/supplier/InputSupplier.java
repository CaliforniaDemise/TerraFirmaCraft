package net.dries007.tfc.util.supplier;

@FunctionalInterface
public interface InputSupplier<I, O> {
    O get(I input);
}
