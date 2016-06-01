package tethering;

/**
 * Created by Oliver on 25.05.2016.
 */
public interface Tetherable {
    void startTethering();
    void stopTethering();
    int getTetheringStatus();
}
