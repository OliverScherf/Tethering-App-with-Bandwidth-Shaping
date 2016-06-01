package utils;

/**
 * Created by Oliver on 31.05.2016.
 */
public interface Loggable {
    void log(String msg);
    void err(String msg, Throwable t);

}
