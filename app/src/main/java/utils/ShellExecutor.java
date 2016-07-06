package utils;

import android.util.Log;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Oliver on 06.07.2016.
 */
public class ShellExecutor {

    public static String execute(String cmd) throws Exception {
        Process process = Runtime.getRuntime().exec("su");
        InputStream in = process.getInputStream();
        OutputStream out = process.getOutputStream();
        out.write((cmd + "\n").getBytes());
        out.write("exit\n".getBytes());
        out.flush();
        byte[] buffer = new byte[472080]; //Able to read up to 12 KB (12288 bytes)
        int length = in.read(buffer);
        Log.d("ASD","Lenght: " +  length);
        process.waitFor();
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
