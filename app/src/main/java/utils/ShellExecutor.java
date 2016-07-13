package utils;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Oliver on 06.07.2016.
 */
public class ShellExecutor {

    private static ShellExecutor singleton = new ShellExecutor();
    private Process rootProcess;
    boolean hasRoot;

    private ShellExecutor() {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec("su");
            process.getOutputStream().write("exit\n".getBytes());
            process.getOutputStream().flush();
            this.hasRoot = true;
            this.rootProcess = process;
        } catch (Exception e) {
            this.hasRoot = false;
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }

    public String exectureUser(String cmd) throws Exception {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public String executeRoot(String cmd) throws IOException, InterruptedException {
        if (!this.hasRoot) {
            throw new SecurityException("You have no root privileges");
        }
        Process process = Runtime.getRuntime().exec("su");
        InputStream in = process.getInputStream();
        OutputStream out = process.getOutputStream();
        out.write((cmd + "\n").getBytes());
        out.write("exit\n".getBytes());
        out.flush();
        byte[] buffer = new byte[24 * 1012]; //Able to read up to 12 KB (12288 bytes)
        process.waitFor();
        int length = in.read(buffer);
        String content = "";
        if (length != -1) {
            content = new String(buffer, 0, length);
        } else {
            Log.d("Executor", "Achtung antwort length war -1! CMD: " + cmd);
        }
        process.destroy();
        out.close();
        in.close();
        return content;
    }

    public static ShellExecutor getSingleton() {
        return ShellExecutor.singleton;
    }

}
