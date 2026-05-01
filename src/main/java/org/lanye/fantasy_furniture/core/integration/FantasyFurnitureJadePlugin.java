package org.lanye.fantasy_furniture.core.integration;

import org.lanye.fantasy_furniture.content.sweeper.entity.SweeperRobotEntity;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

/**
 * 玉（Jade）集成入口（可选依赖；无 Jade 时不会加载）。
 */
@WailaPlugin
public final class FantasyFurnitureJadePlugin implements IWailaPlugin {

    @Override
    public void register(IWailaCommonRegistration registration) {
        // 无服务端同步数据；扫地机血量客户端可直接读实体
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerEntityComponent(SweeperRobotJadeProvider.INSTANCE, SweeperRobotEntity.class);
    }
}
