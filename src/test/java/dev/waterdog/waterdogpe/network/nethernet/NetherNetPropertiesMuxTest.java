/*
 * Copyright 2026 nekoy3
 * Licensed under the GNU General Public License, Version 2.0.
 * See LICENSE for the full terms.
 * Added on 2026-10-27 to verify downstream TURN-aware mux selection.
 */
package dev.waterdog.waterdogpe.network.nethernet;

import org.junit.jupiter.api.Test;
import java.io.InputStream;
import static org.junit.jupiter.api.Assertions.assertEquals;

class NetherNetPropertiesMuxTest {
    private boolean mux(String servers, String override) throws Exception {
        String iceKey = "waterdog.nethernetIceServers";
        String muxKey = "waterdog.nethernetIceMux";
        String oldIce = System.getProperty(iceKey);
        String oldMux = System.getProperty(muxKey);
        try {
            System.setProperty(iceKey, servers);
            if (override == null) System.clearProperty(muxKey);
            else System.setProperty(muxKey, override);
            ClassLoader parent = getClass().getClassLoader();
            String name = "dev.waterdog.waterdogpe.network.nethernet.NetherNetProperties";
            ClassLoader isolated = new ClassLoader(parent) {
                @Override protected Class<?> loadClass(String requested, boolean resolve) throws ClassNotFoundException {
                    if (!requested.equals(name)) return super.loadClass(requested, resolve);
                    Class<?> loaded = findLoadedClass(requested);
                    if (loaded == null) {
                        try (InputStream in = parent.getResourceAsStream(requested.replace('.', '/') + ".class")) {
                            if (in == null) throw new ClassNotFoundException(requested);
                            byte[] bytes = in.readAllBytes();
                            loaded = defineClass(requested, bytes, 0, bytes.length);
                        } catch (java.io.IOException e) { throw new ClassNotFoundException(requested, e); }
                    }
                    if (resolve) resolveClass(loaded);
                    return loaded;
                }
            };
            return isolated.loadClass(name).getField("ICE_MUX").getBoolean(null);
        } finally {
            if (oldIce == null) System.clearProperty(iceKey); else System.setProperty(iceKey, oldIce);
            if (oldMux == null) System.clearProperty(muxKey); else System.setProperty(muxKey, oldMux);
        }
    }
    @Test void noIceServersKeepsMux() throws Exception { assertEquals(true, mux("", null)); }
    @Test void stunOnlyKeepsMux() throws Exception { assertEquals(true, mux("stun:stun.example.com:3478", null)); }
    @Test void turnDisablesMux() throws Exception { assertEquals(false, mux("turn:test:example@turn.example.com:3478", null)); }
    @Test void secureTurnDisablesMuxCaseInsensitively() throws Exception { assertEquals(false, mux("TURNS:test:example@turn.example.com:443", null)); }
    @Test void explicitTrueOverridesTurnDefault() throws Exception { assertEquals(true, mux("turn:turn.example.com:3478", "true")); }
    @Test void explicitFalseOverridesStunDefault() throws Exception { assertEquals(false, mux("stun:stun.example.com:3478", "false")); }
}
