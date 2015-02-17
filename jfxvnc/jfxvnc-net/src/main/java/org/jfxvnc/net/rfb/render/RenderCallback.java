package org.jfxvnc.net.rfb.render;

import java.util.EventListener;

public interface RenderCallback extends EventListener {

    void renderComplete();
}
