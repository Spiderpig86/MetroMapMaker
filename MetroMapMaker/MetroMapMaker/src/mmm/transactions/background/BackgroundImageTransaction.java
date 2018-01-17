package mmm.transactions.background;

import djf.AppTemplate;
import jtps.jTPS_Transaction;
import mmm.data.ExtendedImage;
import mmm.data.MetroData;
import mmm.gui.MetroWorkspace;

public class BackgroundImageTransaction implements jTPS_Transaction {
    private ExtendedImage oldImage;
    private ExtendedImage newImage;
    private AppTemplate app;

    public BackgroundImageTransaction(ExtendedImage oldImage, ExtendedImage
            newImage, AppTemplate app) {
        this.oldImage = oldImage;
        this.newImage = newImage;
        this.app = app;
    }

    @Override
    public void doTransaction() {
        MetroData dataManager = (MetroData) app.getDataComponent();
        if (newImage != null)
            dataManager.setBackgroundImage(newImage);
        else
            dataManager.clearBackgroundImage();
    }

    @Override
    public void undoTransaction() {
        MetroData dataManager = (MetroData) app.getDataComponent();
        if (oldImage != null)
            dataManager.setBackgroundImage(oldImage);
        else
            dataManager.clearBackgroundImage();
    }
}
