package org.lanye.fantasy_furniture.content.soap;

import java.util.List;
import javax.annotation.Nullable;

/**
 * 肥皂架上瓶罐组合（历史方案 1–3）已由地面瓶罐摞 + 特殊场合 2/3 取代，见 {@code
 * docs/设计/组合玩法/04玩意/肥皂/combo-fantasy_furniture-soap_bottle_combos.md}。
 *
 * <p>本类 {@link #canAccept} 恒为 false，架上不再接受新瓶罐；入皂仍由 {@link
 * org.lanye.fantasy_furniture.content.soap.block.SoapRackBlock} 处理。
 */
public final class SoapRackComboRules {

    private SoapRackComboRules() {}

    public record Counts(int wash, int shampoo, int cream) {
        public int liquids() {
            return wash + shampoo;
        }

        public int totalBottles() {
            return wash + shampoo + cream;
        }

        public static Counts of(List<SoapBottleLayer> layers) {
            int wash = 0;
            int shampoo = 0;
            int cream = 0;
            for (SoapBottleLayer layer : layers) {
                switch (layer.kind()) {
                    case BODY_WASH -> wash++;
                    case SHAMPOO -> shampoo++;
                    case BODY_CREAM -> cream++;
                }
            }
            return new Counts(wash, shampoo, cream);
        }

        public Counts withAdded(SoapBottleKind kind) {
            return switch (kind) {
                case BODY_WASH -> new Counts(wash + 1, shampoo, cream);
                case SHAMPOO -> new Counts(wash, shampoo + 1, cream);
                case BODY_CREAM -> new Counts(wash, shampoo, cream + 1);
            };
        }
    }

    /** 仅空列表视为可达（兼容旧存档已挂瓶的只读展示）；不再接受新瓶。 */
    public static boolean isReachable(List<SoapBottleLayer> layers, boolean hasSoap) {
        return layers.isEmpty();
    }

    public static boolean isReachable(Counts counts) {
        return counts.totalBottles() == 0;
    }

    /** 架上不再接受瓶罐组合；恒 false。 */
    public static boolean canAccept(List<SoapBottleLayer> layers, SoapBottleKind incoming) {
        return false;
    }

    /** 旧方案完成态已废止；恒返回 null。 */
    @Nullable
    public static SoapRackComboScheme completedScheme(List<SoapBottleLayer> layers, boolean hasSoap) {
        return null;
    }

    /** 旧方案 3 叠盒禁令已废止。 */
    public static boolean forbidsSoapBoxOnTop(List<SoapBottleLayer> layers, boolean hasSoap) {
        return false;
    }
}
