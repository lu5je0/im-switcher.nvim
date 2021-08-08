package com.lu5je0.neovim.switcher.mac;

import com.ensarsarajcic.neovim.java.corerpc.message.RequestMessage;
import com.ensarsarajcic.neovim.java.handler.annotations.NeovimRequestHandler;
import com.lu5je0.neovim.ImSwitcherBootstrap;
import com.lu5je0.neovim.switcher.SwitcherNative;
import com.sun.jna.Native;

public class MacSwitcherNative implements SwitcherNative {

    static {
        Native.register(ImSwitcherBootstrap.libPath);
    }

    private final String englishIme = "com.apple.keylayout.ABC";

    private String lastIme = englishIme;

    public native String getCurrentInputSourceID();

    public native void switchInputSource(String targetIme);

    @NeovimRequestHandler("switchInsertMode")
    public void switchInsertMode(RequestMessage requestMessage) {
        if (!lastIme.equals(englishIme)) {
            switchInputSource(lastIme);
        }
    }

    @NeovimRequestHandler("switchNormalMode")
    public void switchNormalMode(RequestMessage requestMessage) {
        lastIme = getCurrentInputSourceID();
        switchInputSource(englishIme);
    }

    @Override
    public String defaultEnglishIme() {
        return englishIme;
    }

}
