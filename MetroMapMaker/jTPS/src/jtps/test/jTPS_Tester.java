package jtps.test;

import java.io.PrintStream;
import java.util.Scanner;
import jtps.jTPS;
import jtps.jTPS_Transaction;

/**
 *
 * @author McKillaGorilla
 */
public class jTPS_Tester {
    static jTPS jTPS = new jTPS();
    static PrintStream out = System.out;
    static Scanner input = new Scanner(System.in);
    static Num num = new Num();
    
    public static void main(String[] args) {
        boolean keepGoing = true;
        while (keepGoing) {
            out.println("CURRENT jTPS:");
            out.println(jTPS);
            out.println();
            out.println("num is " + num.getNum());
            out.println();
            out.println("ENTER A SELECTION");
            out.println("1) Add a Transaction");
            out.println("2) Undo a Transaction");
            out.println("3) Redo a Transaction");
            out.print("-");
            
            String entry = input.nextLine();
            if (entry.startsWith("1")) {
                System.out.print("\nEnter an amount to add: ");
                entry = input.nextLine();
                int amountToAdd = Integer.parseInt(entry);
                jTPS_Transaction transaction = new AddToNum_Transaction(num, amountToAdd);
                jTPS.addTransaction(transaction);
            }
            else if (entry.startsWith("2")) {
                jTPS.undoTransaction();
            }
            else if (entry.startsWith("3")) {
                jTPS.doTransaction();
            }
            else if (entry.startsWith("Q")) {
                keepGoing = false;
            }
        }
        System.out.println("GOODBYE");
    }
}
