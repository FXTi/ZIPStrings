package xyz.fxti.zipstrings;

import java.math.BigInteger;

public class Record {
    final String string;
    final BigInteger count;

    public Record(String string, BigInteger count) {
        this.string = string;
        this.count = count;
    }
}
