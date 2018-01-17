package jtps;

import java.util.ArrayList;

/**
 * @author McKillaGorilla
 * @author Ritwik Banerjee
 */
public class jTPS {
    private ArrayList<jTPS_Transaction> transactions          = new ArrayList<>();
    private int                         mostRecentTransaction = -1;
    private boolean canUndo = false;
    private boolean canRedo = false;

    public jTPS() {}

    @SuppressWarnings("unused")
    public void clearAllTransactions() {
        transactions.clear();
        mostRecentTransaction = -1;
    }

    public boolean hasTransactionToUndo() {
        return mostRecentTransaction >= 0;
    }

    public boolean hasTransactionToRedo() {
        return mostRecentTransaction < (transactions.size() - 1);
    }

    public void addTransaction(jTPS_Transaction transaction) {
        // IS THIS THE FIRST TRANSACTION?
        if (mostRecentTransaction < 0) {
            // DO WE HAVE TO CHOP THE LIST?
            if (transactions.size() > 0) {
                transactions = new ArrayList<>();
            }
            transactions.add(transaction);
        }
        // ARE WE ERASING ALL THE REDO TRANSACTIONS?
        // Del
        else if (mostRecentTransaction < (transactions.size()-1)) {
            transactions.set(mostRecentTransaction+1, transaction); // Set the new transaction to be the most recent one
            transactions = new ArrayList<>(transactions.subList(0, mostRecentTransaction+2)); // Not exactly sure on +2, maybe to account for newly added state
        }
        // IS IT JUST A TRANSACTION TO APPEND TO THE END?
        else {
            transactions.add(transaction);
        }
        doTransaction();
        canRedo = false;
        //System.out.println(transaction.getClass().toString());
    }

    public void doTransaction() {
        if (mostRecentTransaction < (transactions.size()-1)) {
            jTPS_Transaction transaction = transactions.get(mostRecentTransaction+1);
            transaction.doTransaction();
            mostRecentTransaction++;

            if (mostRecentTransaction >= (transactions.size()-1)) {
                canRedo = false;
            }
        } else {
            canRedo = false;
        }
        canUndo = true;
    }

    public void undoTransaction() {
        if (mostRecentTransaction >= 0) {
            jTPS_Transaction transaction = transactions.get(mostRecentTransaction);
            transaction.undoTransaction();
            mostRecentTransaction--;
            if (mostRecentTransaction < 0) {
                canUndo = false;
            }
        } else {
            canUndo = false;
        }
        canRedo = true;
    }

    public String toString() {
        String text = "--Number of Transactions: " + transactions.size() + "\n";
        text += "--Current Index on Stack: " + mostRecentTransaction + "\n";
        text += "--Current Transaction Stack:\n";
        for (int i = 0; i <= mostRecentTransaction; i++) {
            jTPS_Transaction jT = transactions.get(i);
            text += "----" + jT.toString() + "\n";
        }
        return text;
    }

    public boolean canUndo() {
        return canUndo;
    }

    public boolean canRedo() {
        return canRedo;
    }
}