package org.lanye.fantasy_furniture.content.soap.client;

import javax.annotation.Nullable;
import org.lanye.fantasy_furniture.content.soap.SoapBottleKind;

/** 肥皂架瓶罐 overlay 渲染态（ThreadLocal）。 */
public final class SoapRackBottleRenderState {

    private static final ThreadLocal<State> STATE = new ThreadLocal<>();

    private SoapRackBottleRenderState() {}

    public static void set(SoapBottleKind kind, int materialId) {
        STATE.set(new State(kind, materialId));
    }

    public static void clear() {
        STATE.remove();
    }

    @Nullable
    public static SoapBottleKind kind() {
        State s = STATE.get();
        return s == null ? null : s.kind;
    }

    public static int materialId() {
        State s = STATE.get();
        return s == null ? 1 : s.materialId;
    }

    private record State(SoapBottleKind kind, int materialId) {}
}
