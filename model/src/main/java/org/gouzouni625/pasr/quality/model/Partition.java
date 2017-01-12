package org.gouzouni625.pasr.quality.model;

import lombok.Getter;


@Getter
public class Partition {

    public Partition(float train) {
        if (train < 0) {
            throw new IllegalArgumentException("train should not be less or equal to zero.");
        }
        else if (train >= 1) {
            throw new IllegalArgumentException("train should not be greater or equal to one.");
        }

        this.train = train;
        this.test = 1 - train;
    }

    private final float train;
    private final float test;

}
