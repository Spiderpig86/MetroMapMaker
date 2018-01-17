package mmm.controls;

import djf.AppTemplate;
import djf.ui.AppDialogs;
import javafx.event.EventHandler;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import mmm.data.MetroData;
import mmm.data.MetroState;
import mmm.gui.MetroWorkspace;
import mmm.transactions.TextTransaction;
import properties_manager.PropertiesManager;

public class DraggableText extends Text implements Draggable {

    protected double startX;
    protected double startY;
    protected double diffX;
    protected double diffY;
    protected AppTemplate app;
    protected boolean isBolded;
    protected boolean isItalicized;

    public DraggableText() {
        this.app = null;
        setOpacity(1.0);
        startX = 0.0;
        startY = 0.0;
        setText("label");
    }

    public DraggableText(String message, AppTemplate initApp) {
        this.app = initApp;
        setOpacity(1.0);
        startX = 0.0;
        startY = 0.0;
        setText(message);

        //super.setStrokeType(StrokeType.INSIDE);

        // Set default stroke color
        //super.setStroke(Color.TRANSPARENT);

        // Add listener for double click
        setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                // Check for left click
                if (event.getButton().equals(MouseButton.PRIMARY)) {
                    MetroWorkspace workspace = (MetroWorkspace) app
                            .getWorkspaceComponent();
                    workspace.updateFontToolbar(getTextView()); // Pass
                    // in text object to get the text properties

                    // Check for double click
                    if (event.getClickCount() == 2) {
                        PropertiesManager props = PropertiesManager.getPropertiesManager();
                        // Open a dialog to change the text
                        // Create a new dialog for user to enter the text
                        String text = AppDialogs.showTextInputDialog(app
                                .getStage(), "Edit " +
                                "Label", "Update label text");
                        if (text.length() > 0 && !text.equals("(cancelled)")) {
                            // Get the old text for the transaction
                            String oldText = getText();

                            // Set the label text if it is valid
                            updateText(text);
                            MetroData dataManager = (MetroData) app
                                    .getDataComponent();

                            // Build the new transaction
                            TextTransaction transaction = new TextTransaction(getTextView(), oldText, text);
                            app.getTPS().addTransaction(transaction);
                            app.getGUI().disableUndoRedo(app.getTPS().canUndo(), app.getTPS().canRedo());
                        }
                    }
                }
            }
        });
    }

    private void updateText(String msg) {
        super.setText(msg);
    }

    @Override
    public MetroState getStartingState() {
        return MetroState.DRAGGING_NOTHING;
    }

    @Override
    public void start(int x, int y) {
        // Part 7: Fixed mouse dragging
        // Let startX and startY be the X and Y coordinates of the top left corner
        double absoluteX = super.getLayoutBounds().getMinX(); // Get location of rectangle
        double absoluteY = super.getLayoutBounds().getMinY();

        diffX = absoluteX - x;
        diffY = absoluteY - y; // Offsets to minize text jumping

        startX = x + diffX;
        startY = y + diffY;
    }

    @Override
    public void drag(int x, int y) {
        // Let startX and startY be the X and Y coordinates of the top left corner
        double newX = x + diffX;
        double newY = y + diffY + (getFont().getSize() - super.getStrokeWidth() * 2); // Must include font size in offset

        xProperty().set(newX);
        yProperty().set(newY);

        startX = newX; // Move the rectangle while keeping the difference (user never lets go of mouse)
        startY = newY;
    }

    public String cT(double x, double y) {
        return "(x,y): (" + x + "," + y + ")";
    }

    @Override
    public void size(int x, int y) {
        double width = x - getX();
        super.prefWidth(width);
        double height = y - getY();
        super.prefHeight(height);
    }

    @Override
    public void setLocationAndSize(double initX, double initY, double initWidth, double initHeight) {
        xProperty().set(initX);
        yProperty().set(initY);
        startX = initX;
        startY = initY;
    }

    @Override
    public String getShapeType() {
        return TEXT;
    }

    @Override
    public double getWidth() {
        return getBoundsInLocal().getWidth();
    }

    @Override
    public double getHeight() {
        return getBoundsInLocal().getHeight();
    }

    public DraggableText getTextView() {
        return this;
    }

    public void setBolded(boolean bolded) {
        isBolded = bolded;

        if (bolded) {
            if (isItalicized) {
                this.setFont(Font.font(this.getFont().getFamily(), FontWeight.BOLD, FontPosture.ITALIC, this.getFont().getSize()));
            } else {
                this.setFont(Font.font(this.getFont().getFamily(), FontWeight.BOLD, this.getFont().getSize()));
            }
        } else {
            if (isItalicized) {
                this.setFont(Font.font(this.getFont().getFamily(), FontWeight.NORMAL, FontPosture.ITALIC, this.getFont().getSize()));
            } else {
                this.setFont(Font.font(this.getFont().getFamily(), FontWeight.NORMAL, this.getFont().getSize()));
            }
        }
    }

    public void setItalicized(boolean italicized) {
        isItalicized = italicized;
        if (italicized) {
            if (isBolded) {
                this.setFont(Font.font(this.getFont().getFamily(), FontWeight.BOLD, FontPosture.ITALIC, this.getFont().getSize()));
            } else {
                this.setFont(Font.font(this.getFont().getFamily(), FontPosture.ITALIC, this.getFont().getSize()));
            }

        } else {
            if (isBolded) {
                this.setFont(Font.font(this.getFont().getFamily(), FontWeight.BOLD, FontPosture.REGULAR, this.getFont().getSize()));
            } else {

                this.setFont(Font.font(this.getFont().getFamily(), FontPosture.REGULAR, this.getFont().getSize()));
            }
        }
    }

    public boolean isBolded() {
        return isBolded;
    }

    public boolean isItalicized() {
        return isItalicized;
    }
}