package org.lanye.fantasy_furniture.content.furniture.livingroom.item;

import net.minecraft.world.item.Item;

/** 长枕头：六色单材质图，栏内共用，不按床板拆物品。 */
public final class LongPillowItem extends Item {

    public static final int COUNT = 6;

    private final int materialId;

    public LongPillowItem(Properties properties, int materialId) {
        super(properties);
        if (materialId < 1 || materialId > COUNT) {
            throw new IllegalArgumentException("materialId out of range: " + materialId);
        }
        this.materialId = materialId;
    }

    public int materialId() {
        return materialId;
    }
}
