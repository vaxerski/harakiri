package me.vaxry.harakiri.api.extd;

import com.sun.jna.Function;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.W32APIOptions;

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class MiscExtd {

    Socket socket;
    PrintWriter out;
    BufferedReader in;

    public MiscExtd(){
    }

    public boolean createFile(){
        try {
            socket = new Socket("127.0.0.1", 27035);

        } catch(Throwable t){
            return false;
        }
        return true;
    }

    public String readFile(){
        char[] buf = new char[1024];
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            in.read(buf, 0, 1024);
        }catch(Throwable t){
            StringWriter errors = new StringWriter();
            t.printStackTrace(new PrintWriter(errors));
            return "";
        }
        return new String(buf, 0, 1024);
    }

    public boolean writeFile(String in){
        try {
            in += "\0";
            out = new PrintWriter(socket.getOutputStream());
            out.write(in);
            out.flush();
        }catch(Throwable e){
            return false;
        }
        return true;
    }
}
