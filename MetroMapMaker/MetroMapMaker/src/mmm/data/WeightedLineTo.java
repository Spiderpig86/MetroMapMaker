package mmm.data;

import javafx.scene.shape.LineTo;
import mmm.controls.MetroLine;
import mmm.controls.MetroStation;

import java.awt.*;

public class WeightedLineTo extends LineTo {
    private double weight = 0; // This will store the distance from one node to
    // another
    private MetroStation srcStation; // Closer to line start
    private MetroStation destStation; // Closer to line end
    private MetroLine line;

    public WeightedLineTo(MetroStation source, MetroStation dest, MetroLine
            line) {
        this.srcStation = source;
        this.destStation = dest;
        this.line = line;
        this.weight = Point.distance(source.getX(), source.getY(), dest.getX
                (), dest.getY());
    }

    public WeightedLineTo(double x, double y, MetroStation source,
                          MetroStation dest, MetroLine line) {
        super(x, y);
        this.srcStation = source;
        this.destStation = dest;
        this.line = line;
        this.weight = Point.distance(source.getX(), source.getY(), dest.getX
                (), dest.getY());
    }

    /**
     * Recalculate the weight/distance when user releases mouse while
     * selecting a node
     */
    public void refreshWeight() {
        if (srcStation != null && destStation != null) {
            weight = Point.distance(srcStation.getX(), srcStation.getY(), destStation.getX(),
                    destStation.getY());
        }
    }

    /* HELPER METHODS */
    public double getWeight() { return weight; }

    public MetroStation getSource() { return srcStation; }
    public void setSource(MetroStation source) { this.srcStation = source; }

    public MetroStation getDestination() { return destStation; }
    public void setDestStation(MetroStation dest) { this.destStation =
            dest; }

    public MetroLine getLine() {
        return line;
    }
}
