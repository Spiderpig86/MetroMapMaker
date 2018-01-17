package mmm.controls;

import djf.AppTemplate;
import djf.ui.AppDialogs;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import mmm.data.MetroData;
import mmm.data.MetroLabelLocation;
import mmm.data.MetroLabelOrientation;
import mmm.data.MetroState;
import mmm.gui.MetroWorkspace;
import mmm.transactions.TextTransaction;
import mmm.transactions.metrolabel.LabelLocationTransaction;
import mmm.transactions.metrolabel.LabelOrientationTransaction;
import properties_manager.PropertiesManager;

public class MetroStationLabel extends DraggableText implements Draggable {

    private String stationName;
    private MetroStation associatedStation;
    private MetroLabelLocation location = MetroLabelLocation.TOPLEFT;
    private MetroLabelOrientation orientation = MetroLabelOrientation
            .HORIZONTAL;
    private Point2D offset;
    private boolean isLineEnd; // Tells us if it is a line end
    private boolean isSelected; // Used with line ends to see if the label is
    // selected or not

    public MetroStationLabel(String name, MetroStation station, boolean
            isLineEnd, AppTemplate app) {
        this.stationName = name;
        this.associatedStation = station;
        this.setText(name); // Update the label
        this.setLocationAndSize(station.getCenterX(), station.getCenterY()); //
        // Update location
        this.isLineEnd = isLineEnd;
        this.app = app;

        // Set the clipping property
        Rectangle clip = ((MetroWorkspace) app.getWorkspaceComponent())
                .cloneCanvasClip();
        this.setClip(clip);

        // For a line end, update the text to reflect station color
        if (isLineEnd) {
            this.setFont(Font.font(this.getFont().getFamily(), 20.0));
            this.setBolded(true);
            MetroLineEnd parent = (MetroLineEnd) station;
            this.setFill(parent.getAssociatedLine().getLineColor()); // Update text
            // color
        } else {
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
                        if (event.getClickCount() == 2 && !isLineEnd) {
                            // Open a dialog to change the text
                            // Create a new dialog for user to enter the text
                            String text = AppDialogs.showTextInputDialog(app
                                    .getStage(), "Update Station Name",
                                    "Update station name:");
                            if (text.length() > 0 && !text.equals("(cancelled)")) {
                                // Get the old text for the transaction
                                String oldText = getText();

                                // Set the label text if it is valid
                                setText(text);
                                getAssociatedStation().setName(text); //
                                // Update station name

                                // Build the new transaction
                                TextTransaction transaction = new TextTransaction
                                        (getTextView(), oldText, text);
                                app.getTPS().addTransaction(transaction);
                                app.getGUI().disableUndoRedo(app.getTPS()
                                        .canUndo(), app.getTPS().canRedo());
                            }
                        }
                    }
                }
            });
        }
    }

    public Point2D getLocationOffset() {
        // Check if the station is a station or a line end
        if (isLineEnd) {
            // Check the direction of the line is at the end of a line
            MetroLineEnd lineEnd = (MetroLineEnd) this.associatedStation;

            // The location of the label is determined by the slope of
            // the line end compared to the line after/before it
            if (lineEnd.isLineStart()) {
                MetroStation nextStation = lineEnd.getNextByLine((lineEnd)
                        .getAssociatedLine());

                if (nextStation == null)
                    return new Point2D(0, 0);

                // Calculate the slope of the line from the previous station
                // to this station
                double slopeY = (this.getY() - nextStation.getY());
                double slopeX = (this.getX() - nextStation.getX());

                if (slopeX > 0) slopeX = 1;
                else slopeX = -1;

                if (slopeY > 0) slopeY = 1;
                else slopeY = -1;

                return new Point2D(20 * slopeX, 20 * slopeY);
            } else {
                MetroStation prevStation = lineEnd.getPrevByLine(lineEnd.getAssociatedLine());

                if (prevStation == null)
                    return new Point2D(0, 0);

                // Calculate the slope of the line from the previous station
                // to this station
                double slopeY = (prevStation.getY() - this.getY());
                double slopeX = (prevStation.getX() - this.getX());

                if (slopeX > 0) slopeX = 1;
                else slopeX = -1;

                if (slopeY > 0) slopeY = 1;
                else slopeY = -1;

                return new Point2D(-20 * slopeX, -20 * slopeY);
            }

        } else {
            // Return different offsets based on location enum
            switch (location) {
                case TOPLEFT:
                    return new Point2D(this.getWidth() * -1 -
                            (associatedStation.getRadiusX() * 2),this.getHeight() * -1);
                case TOPRIGHT:
                    return new Point2D(this.getAssociatedStation().getRadiusX
                            () * 2,this.getHeight() * -1);
                case BOTTOMLEFT:
                    return new Point2D(this.getWidth() * -1 -
                            (associatedStation.getRadiusX() * 2), this
                            .associatedStation.getRadiusY() * 2);
                default:
                    return new Point2D(this.associatedStation.getRadiusX() *
                            2 + 5,
                            this.associatedStation.getRadiusY() * 2);
            }
        }

    }

    public boolean isSelected() { return this.isSelected; }
    public void setSelected(boolean selected) {
        this.isSelected = selected;
        this.setVisible(selected);

        if (isLineEnd) {
            MetroLineEnd end = (MetroLineEnd) this.associatedStation;
            end.setSelected(!selected);
        }
    }

    public MetroStation getAssociatedStation() {
        return associatedStation;
    }

    @Override
    public MetroState getStartingState() {
        return MetroState.DRAGGING_NOTHING;
    }

    @Override
    public void start(int x, int y) {
        // Disable moving
    }

    @Override
    public void drag(int x, int y) {
        // Disable dragging
    }

    @Override
    public void size(int x, int y) {
        // Disable resizing
    }

    @Override
    public double getWidth() {
        return super.getLayoutBounds().getWidth();
    }

    @Override
    public double getHeight() {
        return super.getLayoutBounds().getHeight();
    }

    @Override
    public void setLocationAndSize(double initX, double initY, double initWidth, double initHeight) {

    }

    public boolean isLineEnd() { return this.isLineEnd; }

    public void setLocationAndSize(double initX, double initY) {
        // Used for the "binding" when the station location is moved
        offset = getLocationOffset();
        this.setX(initX + offset.getX());
        this.setY(initY + offset.getY());
    }

    public MetroLabelLocation getLocation() {
        return location;
    }

    public void setLocation(MetroLabelLocation location) {
        this.location = location;
        setLocationAndSize(associatedStation.getCenterX(), associatedStation
                .getCenterY()
        ); // Update label location
    }

    public void cycleLocation() {
        LabelLocationTransaction transaction;
        if (location == MetroLabelLocation.TOPLEFT)
            transaction = new LabelLocationTransaction(this, this.location,
                    MetroLabelLocation.TOPRIGHT);
        else if (location == MetroLabelLocation.TOPRIGHT)
            transaction = new LabelLocationTransaction(this, this.location,
                    MetroLabelLocation.BOTTOMLEFT);
        else if (location == MetroLabelLocation.BOTTOMLEFT)
            transaction = new LabelLocationTransaction(this, this.location,
                    MetroLabelLocation.BOTTOMRIGHT);
        else
            transaction = new LabelLocationTransaction(this, this.location,
                    MetroLabelLocation.TOPLEFT);
        app.getTPS().addTransaction(transaction);
    }

    public MetroLabelOrientation getOrientation() {
        return orientation;
    }

    public void setRotation(MetroLabelOrientation orientation) {
        this.orientation = orientation;
        if (orientation == MetroLabelOrientation.VERTICAL) {
            this.setRotate(-90);
        } else {
            this.setRotate(0);
        }
    }

    public void cycleRotation() {
        LabelOrientationTransaction transaction;
        if (orientation == MetroLabelOrientation.HORIZONTAL) {
            transaction = new LabelOrientationTransaction(this, orientation,
                    MetroLabelOrientation.VERTICAL);
            orientation = MetroLabelOrientation.VERTICAL;
        } else {
            transaction = new LabelOrientationTransaction(this, orientation,
                    MetroLabelOrientation.HORIZONTAL);
            orientation = MetroLabelOrientation.HORIZONTAL;
        }
        app.getTPS().addTransaction(transaction);
    }

    @Override
    public String getShapeType() {
        return (isLineEnd ? LINE_END_LABEL : STATION_LABEL);
    }
}
