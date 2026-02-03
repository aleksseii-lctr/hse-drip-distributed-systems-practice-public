package ru.hse.drip.systems.distributed.seminar.i17;

import java.util.Objects;
import java.util.UUID;

public final class TxId {
    private final String value;

    private TxId(String value) {
        this.value = value;
    }

    public static TxId newId() {
        return new TxId(UUID.randomUUID().toString());
    }

    public static TxId of(String value) {
        return new TxId(value);
    }

    public String value() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TxId)) return false;
        TxId txId = (TxId) o;
        return Objects.equals(value, txId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
