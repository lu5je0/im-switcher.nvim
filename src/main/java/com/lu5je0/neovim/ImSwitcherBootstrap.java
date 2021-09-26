package com.lu5je0.neovim;

import com.ensarsarajcic.neovim.java.corerpc.client.RpcClient;
import com.ensarsarajcic.neovim.java.corerpc.client.StdIoRpcConnection;
import com.ensarsarajcic.neovim.java.corerpc.message.RequestMessage;
import com.ensarsarajcic.neovim.java.handler.NeovimHandlerManager;
import com.ensarsarajcic.neovim.java.handler.annotations.NeovimRequestHandler;
import com.lu5je0.neovim.switcher.SwitcherNative;
import com.lu5je0.neovim.switcher.mac.MacSwitcherNative;
import com.lu5je0.neovim.switcher.win.WinSwitcherNative;

public final class ImSwitcherBootstrap {

    public static String libPath = "";

    public static String wslHost = null;

    private ImSwitcherBootstrap() {
        //no instance
    }

    public static void main(String[] args) {
        libPath = args[0];
        wslHost = args[1];

        var rpcConnection = new StdIoRpcConnection();
        var streamer = RpcClient.getDefaultAsyncInstance();
        NeovimHandlerManager neovimHandlerManager = new NeovimHandlerManager();
        neovimHandlerManager.registerNeovimHandler(new ImSwitcher());
        neovimHandlerManager.attachToStream(streamer);
        streamer.attach(rpcConnection);
    }

    /**
     * Example of remote plugin from https://neovim.io/doc/user/remote_plugin.html
     * It simply limits number of requests made to it
     */
    public static final class ImSwitcher {

        private final SwitcherNative switcherNative;

        private String lastIme;

        private final String englishIme;

        public ImSwitcher() {
            String osName = System.getProperty("os.name");
            if (osName.startsWith("Mac OS")) {
                switcherNative = new MacSwitcherNative();
            } else {
                switcherNative = new WinSwitcherNative();
            }

            lastIme = switcherNative.defaultEnglishIme();
            englishIme = switcherNative.defaultEnglishIme();
        }

        @NeovimRequestHandler("switchInsertMode")
        public void switchInsertMode(RequestMessage requestMessage) {
            if (!lastIme.equals(englishIme)) {
                switcherNative.switchInputSource(lastIme);
            }
        }

        @NeovimRequestHandler("switchNormalMode")
        public void switchNormalMode(RequestMessage requestMessage) {
            lastIme = switcherNative.getCurrentInputSourceID();
            switcherNative.switchInputSource(englishIme);
        }

    }
}
