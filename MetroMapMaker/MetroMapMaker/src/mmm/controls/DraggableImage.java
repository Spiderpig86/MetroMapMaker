package mmm.controls;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import mmm.data.ExtendedImage;
import mmm.data.MetroState;

/**
 * A draggable ImageView the user can place onto the canvas of the application
 * @author Stanley Lim
 */
public class DraggableImage extends Rectangle implements Draggable {
    double startX;
    double startY;
    double diffX;
    double diffY;
    Image currentImage;
    String imagePath;

    public DraggableImage(ExtendedImage image) {
//        setX(0.0);
//	setY(0.0);
        setWidth(image.getImage().getWidth());
        setHeight(image.getImage().getHeight());
        setOpacity(1.0);
//	startX = 0.0;
//	startY = 0.0;
        currentImage = image.getImage();
        imagePath = image.getImagePath();
        this.setFill(new ImagePattern(currentImage)); // Add imageview to control

        // Set default colors to avoid errors when loading
        super.setStroke(Color.TRANSPARENT);
        //super.setStrokeType(StrokeType.INSIDE);
        super.setStrokeWidth(0.0);
    }

    @Override
    public MetroState getStartingState() {
        return MetroState.DRAGGING_NOTHING;
    }

    @Override
    public void drag(int x, int y) {
        // Let startX and startY be the X and Y coordinates of the top left corner
        double newX = x + diffX + 15; // Use 15 since stroke width is 0
        double newY = y + diffY + 15;

        setX(newX);
        setY(newY);

        startX = newX; // Move the rectangle while keeping the difference (user never lets go of mouse)
        startY = newY;
    }

    @Override
    public void size(int x, int y) {
        double width = x - getX();
        super.setWidth(width);
        double height = y - getY();
        super.setHeight(height);
    }

    @Override
    public void setLocationAndSize(double initX, double initY, double initWidth, double initHeight) {
        xProperty().set(initX);
        yProperty().set(initY);
        widthProperty().set(initWidth);
        heightProperty().set(initHeight);
    }

    @Override
    public String getShapeType() {
        return IMAGE;
    }

    @Override
    public void start(int x, int y) {
        // Part 7: Fixed mouse dragging
        // Let startX and startY be the X and Y coordinates of the top left corner
        double absoluteX = super.getBoundsInParent().getMinX(); // Get location of rectangle
        double absoluteY = super.getBoundsInParent().getMinY();

        diffX = absoluteX - x;
        diffY = absoluteY - y;

        startX = x + diffX;
        startY = y + diffY;
    }

    public Image getImage() {
        return currentImage;
    }

    public void setImage(Image image) {
        this.currentImage = image;
        this.setFill(new ImagePattern(image)); // Add imageview to control
    }

    public String getImagePath() {
        return imagePath;
    }

}
