package utils;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

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
        }
        process.destroy();
        out.close();
        in.close();
        return content;
    }

    public String[] executeRoot(List<String> cmds) throws IOException, InterruptedException {
        if (!this.hasRoot) {
            throw new SecurityException("You have no root privileges");
        }
        Process process = Runtime.getRuntime().exec("su");
        InputStream in = process.getInputStream();
        OutputStream out = process.getOutputStream();
        for (String cmd : cmds) {
            out.write((cmd + "\n").getBytes());
        }
        out.write("exit\n".getBytes());
        out.flush();
        byte[] buffer = new byte[24 * 1012]; //Able to read up to 12 KB (12288 bytes)
        process.waitFor();
        int length = in.read(buffer);
        String content = "";
        if (length != -1) {
            content = new String(buffer, 0, length);
        }
        process.destroy();
        out.close();
        in.close();
        return content.split("\n");
    }

    public static ShellExecutor getSingleton() {
        return ShellExecutor.singleton;
    }

}
