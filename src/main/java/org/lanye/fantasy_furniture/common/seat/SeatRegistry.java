package org.lanye.fantasy_furniture.common.seat;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 按 id 注册 {@link SeatConfig}，供 {@link org.lanye.fantasy_furniture.entity.FurnitureSeatEntity} 序列化与逻辑使用。
 * <p>
 * id 字符串须与 {@link org.lanye.fantasy_furniture.registry.ModSeatConfigs} 中公开常量及
 * {@link org.lanye.fantasy_furniture.entity.FurnitureSeatEntity#NBT_SEAT_CONFIG_ID} 一致，勿在多处硬编码不同拼写。
 */
public final class SeatRegistry {

    private static final Map<String, SeatConfig> BY_ID = new LinkedHashMap<>();

    private SeatRegistry() {}

    public static void register(String id, SeatConfig config) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(config);
        if (BY_ID.containsKey(id)) {
            throw new IllegalStateException("duplicate seat config id: " + id);
        }
        BY_ID.put(id, config);
    }

    public static SeatConfig get(String id) {
        return BY_ID.get(id);
    }

    public static Iterable<Map.Entry<String, SeatConfig>> entries() {
        return List.copyOf(BY_ID.entrySet());
    }
}
