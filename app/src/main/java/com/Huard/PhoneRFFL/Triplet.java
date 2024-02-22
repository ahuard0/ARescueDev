package com.Huard.PhoneRFFL;

public class Triplet<A, B, C> {
    public final A first;
    public final B second;
    /** @noinspection unused*/
    public final C third;

    public Triplet(A first, B second, C third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }
}
