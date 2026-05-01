package org.lanye.fantasy_furniture.core.integration;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;
import org.lanye.fantasy_furniture.FantasyFurniture;

/**
 * 最小 GameTest：验证 GameTest 运行配置与模组在测试服中可加载。
 * <p>
 * 通过 IDE / {@code gameTestServer} 运行；正式 jar 中已排除本包（见 {@code build.gradle}）。
 */
@GameTestHolder(FantasyFurniture.MODID + ".gametest")
public final class FurnitureGameTests {

    private FurnitureGameTests() {}

    @GameTest(template = "empty")
    public static void environmentLoads(GameTestHelper helper) {
        helper.succeed();
    }
}
