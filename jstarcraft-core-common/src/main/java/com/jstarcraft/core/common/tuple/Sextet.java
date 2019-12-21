package com.jstarcraft.core.common.tuple;

/**
 * 六元
 * 
 * @author Birdy
 *
 */
public class Sextet<A, B, C, D, E, F> extends Quintet<A, B, C, D, E> {

    public Sextet(Object... datas) {
        if (datas.length != 6) {
            throw new IllegalArgumentException();
        }
        this.datas = datas;
    }

    public F getF() {
        return (F) datas[5];
    }

    public void setF(F data) {
        datas[5] = data;
    }

}