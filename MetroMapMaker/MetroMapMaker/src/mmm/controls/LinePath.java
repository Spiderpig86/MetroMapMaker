package mmm.controls;

import javafx.scene.shape.Path;
import mmm.data.MetroState;

public class LinePath extends Path implements Draggable {

    private MetroLine associatedLine;

    public LinePath(MetroLine associatedLine) {
        super();
        this.associatedLine = associatedLine;
    }

    public MetroLine getAssociatedLine() {
        return associatedLine;
    }

    @Override
    public MetroState getStartingState() {
        return MetroState.DRAGGING_NOTHING;
    }

    @Override
    public void start(int x, int y) {

    }

    @Override
    public void drag(int x, int y) {

    }

    @Override
    public void size(int x, int y) {

    }

    @Override
    public double getX() {
        return 0;
    }

    @Override
    public double getY() {
        return 0;
    }

    @Override
    public double getWidth() {
        return 0;
    }

    @Override
    public double getHeight() {
        return 0;
    }

    @Override
    public void setLocationAndSize(double initX, double initY, double initWidth, double initHeight) {

    }

    @Override
    public String getShapeType() {
        return LINE;
    }
}
