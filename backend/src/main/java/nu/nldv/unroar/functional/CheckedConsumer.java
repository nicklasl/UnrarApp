package nu.nldv.unroar.functional;

@FunctionalInterface
public interface CheckedConsumer<T> {
    void accept(T t) throws Exception;
}

