package org.lanye.fantasy_furniture.content.furniture.cabinet;

/**
 * 柜内展品水平偏航：绕槽位中心竖直轴（垂直地面），步长 45°（0～7）。
 *
 * <p>类似展示框转物品：先到位再转，原地自转，不绕柜心公转。
 */
public final class CabinetYaw {

    public static final int STEPS = 8;
    public static final float DEGREES_PER_STEP = 45f;

    private CabinetYaw() {}

    public static int clamp(int rotation) {
        return rotation & (STEPS - 1);
    }

    public static float degrees(int rotation) {
        return clamp(rotation) * DEGREES_PER_STEP;
    }

    public static int next(int rotation) {
        return clamp(rotation + 1);
    }
}
