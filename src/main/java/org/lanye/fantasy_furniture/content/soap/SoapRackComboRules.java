package org.lanye.fantasy_furniture.content.soap;

import java.util.List;
import javax.annotation.Nullable;

/**
 * 肥皂架上瓶罐组合合法性：仅允许能走到方案 1–3 之一的中间态 / 完成态。
 *
 * <p>地面瓶罐独立摞放不走本类。
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

    /** 当前层列表是否仍可到达某一合法方案（有皂可稍后补，不影响瓶罐可达性）。 */
    public static boolean isReachable(List<SoapBottleLayer> layers, boolean hasSoap) {
        return isReachable(Counts.of(layers));
    }

    public static boolean isReachable(Counts counts) {
        if (counts.totalBottles() == 0) {
            return true;
        }
        if (counts.wash() <= 1 && counts.shampoo() <= 1 && counts.cream() <= 1) {
            return true; // 方案 1 路径
        }
        if (counts.liquids() <= 3 && counts.cream() <= 1) {
            return true; // 方案 2 路径
        }
        if (counts.wash() == 0 && counts.shampoo() == 0 && counts.cream() <= 2) {
            return true; // 方案 3 路径
        }
        return false;
    }

    public static boolean canAccept(List<SoapBottleLayer> layers, SoapBottleKind incoming) {
        return isReachable(Counts.of(layers).withAdded(incoming));
    }

    /**
     * 完成态方案；中间态返回 null。沐浴+洗发+乳霜各 1 且有皂时优先方案 1（即使也像方案 2 子集）。
     */
    @Nullable
    public static SoapRackComboScheme completedScheme(List<SoapBottleLayer> layers, boolean hasSoap) {
        Counts c = Counts.of(layers);
        if (hasSoap && c.wash() == 1 && c.shampoo() == 1 && c.cream() == 1) {
            return SoapRackComboScheme.LIQUIDS_CREAM_RACK_SOAP;
        }
        if (c.liquids() == 3 && c.cream() == 1) {
            return SoapRackComboScheme.LIQUIDS_X3_CREAM;
        }
        if (hasSoap && c.wash() == 0 && c.shampoo() == 0 && c.cream() == 2) {
            return SoapRackComboScheme.CREAM2_RACK_SOAP;
        }
        return null;
    }

    /** 方案 3 完成态上禁止再叠肥皂盒（由肥皂盒交互侧查询）。 */
    public static boolean forbidsSoapBoxOnTop(List<SoapBottleLayer> layers, boolean hasSoap) {
        return completedScheme(layers, hasSoap) == SoapRackComboScheme.CREAM2_RACK_SOAP;
    }
}
