package mmm.data;

import mmm.controls.MetroStation;

public class StationReference {

    private MetroStation station;

    public StationReference(MetroStation station) {
        this.station = station;
    }

    public MetroStation getStation() {
        return station;
    }

    @Override
    public String toString() {
        return this.station.toString();
    }
}
