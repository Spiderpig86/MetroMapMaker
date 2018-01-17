package mmm.transactions;

import javafx.scene.text.Font;
import jtps.jTPS_Transaction;
import mmm.controls.DraggableText;

public class TextFontTransaction implements jTPS_Transaction {
    private DraggableText textView;
    private Font oldFont;
    private Font newFont;

    public  TextFontTransaction(DraggableText textView, Font oldFont, Font newFont) {
        this.textView = textView;
        this.oldFont = oldFont;
        this.newFont = newFont;
    }

    @Override
    public void doTransaction() {
        textView.setFont(newFont);
        // Check if it is bolded or not
//        if (textView.isBolded()) textView.setBolded(true); // Bypasses bug where boldness resets on font change
//        if (textView.isItalicized()) textView.setItalicized(true);
    }

    @Override
    public void undoTransaction() {
        textView.setFont(oldFont);
        // Check if it is bolded or not
//        if (textView.isBolded()) textView.setBolded(true); // Bypasses bug where boldness resets on font change
//        if (textView.isItalicized()) textView.setItalicized(true);
    }

}