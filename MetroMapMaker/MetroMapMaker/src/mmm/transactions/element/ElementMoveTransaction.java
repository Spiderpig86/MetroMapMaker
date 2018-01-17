package mmm.transactions.element;

import javafx.geometry.Point2D;
import jtps.jTPS_Transaction;
import mmm.controls.Draggable;
import mmm.controls.MetroStation;
import mmm.controls.MetroStationLabel;

public class ElementMoveTransaction implements jTPS_Transaction {
    private Draggable shape;
    private Point2D oldLocation;
    private Point2D newLocation;

    public ElementMoveTransaction(Draggable s, Point2D oldLocation, Point2D newLocation) {
        this.shape = s;
        this.oldLocation = oldLocation;
        this.newLocation = newLocation;
    }

    @Override
    public void doTransaction() {
        shape.setLocationAndSize(newLocation.getX(), newLocation.getY(), shape.getWidth(), shape.getHeight());
        // Update label location if it is a metro station
        if (shape instanceof MetroStation) {
            MetroStationLabel label = ((MetroStation) shape)
                    .getAssociatedLabel();
            label.setLocationAndSize(((MetroStation) shape).getCenterX(),
                    ((MetroStation) shape).getCenterY());
        }
//        System.out.println(oldLocation.toString() + " " + newLocation.toString());
    }

    @Override
    public void undoTransaction() {
        shape.setLocationAndSize(oldLocation.getX(), oldLocation.getY(), shape.getWidth(), shape.getHeight());
        if (shape instanceof MetroStation) {
            MetroStationLabel label = ((MetroStation) shape)
                    .getAssociatedLabel();
            label.setLocationAndSize(((MetroStation) shape).getCenterX(),
                    ((MetroStation) shape).getCenterY());
        }
//        System.out.println(oldLocation.toString() + " " + newLocation.toString());
    }

}
