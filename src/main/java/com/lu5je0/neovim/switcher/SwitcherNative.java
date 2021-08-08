package com.lu5je0.neovim.switcher;

public interface SwitcherNative {

    String getCurrentInputSourceID();

    void switchInputSource(String targetIme);

    String defaultEnglishIme();

}
