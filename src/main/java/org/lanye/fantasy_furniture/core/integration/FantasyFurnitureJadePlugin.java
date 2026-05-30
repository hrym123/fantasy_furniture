package org.lanye.fantasy_furniture.core.integration;

import org.lanye.fantasy_furniture.bootstrap.block.ModBlocks;
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
        // 普通玻璃窗：多方块共用一个 Block，名称须来自 pick（与 getCloneItemStack 一致），否则玉只显示方块译名「普通玻璃窗」
        registration.usePickedResult(ModBlocks.PLAIN_GLASS_WINDOW.block().get());
        // 床板 6：按击中高度区分被单/被套/枕头，玉标题与床品物品译名一致
        registration.usePickedResult(ModBlocks.BED_PLATE6.block().get());
        // 肥皂：方块状态含颜料/磨损，标题与 {@link org.lanye.fantasy_furniture.content.soap.item.SoapBarBlockItem#getName} 一致
        registration.usePickedResult(ModBlocks.SOAP_BAR.block().get());
        registration.usePickedResult(ModBlocks.SOAP_BOX.block().get());
        registration.usePickedResult(ModBlocks.SOAP_RACK.block().get());
        registration.usePickedResult(ModBlocks.SOAP_PAPER_BAG.block().get());
        registration.usePickedResult(ModBlocks.BODY_WASH.block().get());
        registration.usePickedResult(ModBlocks.SHAMPOO.block().get());
        registration.usePickedResult(ModBlocks.BODY_CREAM.block().get());
        registration.usePickedResult(ModBlocks.SOAP_PAPER_BOX.block().get());
        registration.usePickedResult(ModBlocks.SOAP_MOLD.block().get());
        registration.registerEntityComponent(SweeperRobotJadeProvider.INSTANCE, SweeperRobotEntity.class);
    }
}
