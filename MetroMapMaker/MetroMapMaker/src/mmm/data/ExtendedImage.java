package mmm.data;

import javafx.scene.image.Image;

public class ExtendedImage {
    private final String imgPath;
    private final Image image;

    public ExtendedImage(Image image, String url) {
        this.image = image;
        this.imgPath = url; // Store the image path
    }

    public String getImagePath() {
        return this.imgPath;
    }

    public Image getImage() {
        return this.image;
    }
}
