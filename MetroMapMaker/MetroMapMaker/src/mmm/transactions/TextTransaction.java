package mmm.transactions;

import jtps.jTPS_Transaction;
import mmm.controls.DraggableText;

public class TextTransaction implements jTPS_Transaction {

    private DraggableText textView;
    private String oldText;
    private String newText;

    public TextTransaction(DraggableText textView, String oldText, String newText) {
        this.textView = textView;
        this.oldText = oldText;
        this.newText = newText;
    }

    @Override
    public void doTransaction() {
        textView.setText(newText);
    }

    @Override
    public void undoTransaction() {
        textView.setText(oldText);
    }
}

