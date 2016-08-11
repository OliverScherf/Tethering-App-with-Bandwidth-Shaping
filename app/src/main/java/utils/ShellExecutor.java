package utils;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Oliver on 06.07.2016.
 */
public class ShellExecutor {

    private ShellExecutor() {}

    public synchronized static String[] executeRoot(List<String> cmds) throws IOException, InterruptedException {
        Process process = Runtime.getRuntime().exec("su");
        InputStream in = process.getInputStream();
        OutputStream out = process.getOutputStream();
        for (String cmd : cmds) {
            out.write((cmd + "\n").getBytes());
        }
        out.write("exit\n".getBytes());
        out.flush();
        byte[] buffer = new byte[24 * 1012];
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

    public synchronized static String executeRoot(String cmd) throws IOException, InterruptedException {
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
}
