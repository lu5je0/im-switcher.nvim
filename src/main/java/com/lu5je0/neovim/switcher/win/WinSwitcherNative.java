package com.lu5je0.neovim.switcher.win;

import com.lu5je0.neovim.ImSwitcherBootstrap;
import com.lu5je0.neovim.switcher.SwitcherNative;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;

public class WinSwitcherNative implements SwitcherNative {

    private Socket socket;

    private BufferedReader bufferedReader;

    private OutputStreamWriter writer;


    public WinSwitcherNative() {
        try {
            socket = new Socket(InetAddress.getByName(ImSwitcherBootstrap.wslHost), 38713);
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new OutputStreamWriter(socket.getOutputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static final Integer KEY_LAYOUT_US = 67699721;

    @Override
    public String getCurrentInputSourceID() {
        try {
            writer.write("get\n");
            writer.flush();
            return bufferedReader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public void switchInputSource(String targetIme) {
        try {
            writer.write("set " + targetIme + "\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String defaultEnglishIme() {
        return String.valueOf(KEY_LAYOUT_US);
    }

    public static void main(String[] args) {
        WinSwitcherNative winSwitcherNative = new WinSwitcherNative();
        System.out.println(winSwitcherNative.getCurrentInputSourceID());
        winSwitcherNative.switchInputSource("134481924");
        System.out.println();
    }

}
