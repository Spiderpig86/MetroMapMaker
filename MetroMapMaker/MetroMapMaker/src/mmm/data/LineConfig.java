package mmm.data;

import javafx.scene.paint.Color;

public class LineConfig {
    private String name;
    private Color color;

    public LineConfig(String name, Color color) {
        this.name = name;
        this.color = color;
    }

    public String getName() { return this.name; }
    public Color getColor() { return this.color; }
}
