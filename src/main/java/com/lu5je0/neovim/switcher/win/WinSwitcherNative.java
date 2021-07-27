package com.lu5je0.neovim.switcher.win;

import com.lu5je0.neovim.switcher.SwitcherNative;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.win32.W32APIOptions;

public class WinSwitcherNative implements SwitcherNative {

    private static final Integer KEY_LAYOUT_US = 67699721;

    private static final Integer WM_INPUTLANG_CHANGE_REQUEST = 0x0050;

    private static final WinNative winNative = Native.load("user32", WinNative.class, W32APIOptions.DEFAULT_OPTIONS);

    public interface WinNative extends User32 {

        WinNT.HANDLE GetKeyboardLayout(WinDef.DWORD pid);

    }

    public Long getCurrentIme() {
        WinDef.HWND hwnd = winNative.GetForegroundWindow();
        int pid = winNative.GetWindowThreadProcessId(hwnd, null);
        return Pointer.nativeValue(winNative.GetKeyboardLayout(new WinDef.DWORD(pid)).getPointer());
    }

    public void setIme(long inputSourceId) {
        WinDef.HWND hwnd = winNative.GetForegroundWindow();
        winNative.PostMessage(hwnd, WM_INPUTLANG_CHANGE_REQUEST, new WinDef.WPARAM(0), new WinDef.LPARAM(inputSourceId));
    }

    @Override
    public String getCurrentInputSourceID() {
        return String.valueOf(new WinSwitcherNative().getCurrentIme());
    }

    @Override
    public int switchInputSource(String targetIme) {
        setIme(Integer.parseInt(targetIme));
        return 0;
    }

    @Override
    public String defaultEnglishIme() {
        return String.valueOf(KEY_LAYOUT_US);
    }

}
