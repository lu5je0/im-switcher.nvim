package com.lu5je0.neovim;

import com.ensarsarajcic.neovim.java.corerpc.client.RpcClient;
import com.ensarsarajcic.neovim.java.corerpc.client.StdIoRpcConnection;
import com.ensarsarajcic.neovim.java.corerpc.message.RequestMessage;
import com.ensarsarajcic.neovim.java.handler.NeovimHandlerManager;
import com.ensarsarajcic.neovim.java.handler.annotations.NeovimRequestHandler;
import com.sun.jna.Native;

public final class ImSwitcherBootstrap {

    public static String libPath = "";

    private ImSwitcherBootstrap() {
        //no instance
    }

    public static void main(String[] args) throws Exception {
        libPath = args[0];
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

        static {
            Native.register(ImSwitcherBootstrap.libPath);
        }

        private final String englishIme = "com.apple.keylayout.ABC";

        private String lastIme = englishIme;

        public native String getCurrentInputSourceID();

        public native int switchInputSource(String targetIme);

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

    }
}
