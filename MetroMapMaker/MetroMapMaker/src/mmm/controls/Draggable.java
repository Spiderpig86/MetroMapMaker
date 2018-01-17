package mmm.controls;

import mmm.data.MetroState;

/**
 * This interface represents a family of draggable shapes.
 *
 * @author Richard McKenna
 * @author ?
 * @version 1.0
 */
public interface Draggable {
    public static final String ELLIPSE = "ELLIPSE";
    public static final String IMAGE = "IMAGE";
    public static final String TEXT = "TEXT";
    public static final String STATION = "STATION";
    public static final String STATION_LABEL = "STATION_LABEL";
    public static final String LINE = "LINE";
    public static final String LINE_END = "LINE_END";
    public static final String LINE_END_LABEL = "LINE_END_LABEL";

    public MetroState getStartingState();
    public void start(int x, int y);
    public void drag(int x, int y);
    public void size(int x, int y);
    public double getX();
    public double getY();
    public double getWidth();
    public double getHeight();
    public void setLocationAndSize(double initX, double initY, double initWidth, double initHeight);
    public String getShapeType();
}