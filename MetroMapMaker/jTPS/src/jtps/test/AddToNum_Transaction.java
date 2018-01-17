package jtps.test;

import jtps.jTPS_Transaction;

/**
 *
 * @author McKillaGorilla
 */
public class AddToNum_Transaction implements jTPS_Transaction {
    private Num num;
    private int amountToAdd;
    
    public AddToNum_Transaction(Num initNum, int initAmountToAdd) {
        num = initNum;
        amountToAdd = initAmountToAdd;
    }

    @Override
    public void doTransaction() {
        int oldNum = num.getNum();
        int newNum = oldNum + amountToAdd;
        num.setNum(newNum);
    }

    @Override
    public void undoTransaction() {
        int oldNum = num.getNum();
        int newNum = oldNum - amountToAdd;
        num.setNum(newNum);
    }
    
    @Override
    public String toString() {
        return "Add " + amountToAdd;
    }
}
