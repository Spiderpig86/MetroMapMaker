package mmm.transactions.element;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.shape.Shape;
import jtps.jTPS_Transaction;
import mmm.data.MetroData;

public class DeleteTransaction implements jTPS_Transaction {

    private Shape shape; // Shape to delete
    private ObservableList<Node> shapes;
    private MetroData dataManager;

    public DeleteTransaction(MetroData dataManager, Shape shape, ObservableList<Node> shapes) {
        this.dataManager = dataManager;
        this.shape = shape;
        this.shapes = shapes;
    }

    @Override
    public void doTransaction() {
        this.shapes.remove(this.shape);
    }

    @Override
    public void undoTransaction() {
        this.shapes.add(this.shape);
    }

}

