package mmm.transactions.element;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.shape.Shape;
import jtps.jTPS_Transaction;
import mmm.data.MetroData;

public class InsertTransaction implements jTPS_Transaction {

    private Shape shape;
    private ObservableList<Node> shapes;

    public InsertTransaction(Shape shape, ObservableList<Node> shapes) {
        this.shape = shape; // Shape that we are inserting
        this.shapes = shapes;
    }

    @Override
    public void doTransaction() {
        this.shapes.add(this.shape);
    }

    @Override
    public void undoTransaction() {
        this.shapes.remove(shape);
    }

}
