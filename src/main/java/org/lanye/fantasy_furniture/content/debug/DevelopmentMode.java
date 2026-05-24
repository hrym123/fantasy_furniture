package org.lanye.fantasy_furniture.content.debug;

import org.lanye.fantasy_furniture.Config;

/** 开发模式开关（{@code fantasy_furniture-common.toml} → {@code [development] enabled}）。 */
public final class DevelopmentMode {

    private DevelopmentMode() {}

    /** 是否启用开发工具（调试棒、对齐探针等）。 */
    public static boolean enabled() {
        return Config.developmentModeEnabled();
    }
}
