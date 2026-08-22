package org.lanye.fantasy_furniture.content.furniture.decor.series;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.lanye.reverie_core.geolib.multiblock.WallPlaneFootprint;
import org.lanye.reverie_core.geolib.multiblock.WallPlanePlacement;

/**
 * 单型号窗配置：色表、geo、墙面足迹、整模北向碰撞（按格切片）。
 *
 * <p>1 与 1.5、2 与 2.5 可共用同一组 {@link #textureStems}（不同 geo）。
 */
public final class StyledWindowSeriesSpec {

    private final StyledWindowSeriesId id;
    private final String[] colorIds;
    private final String[] textureStems;
    private final String[] geoBasenames;
    private final int shapeCount;
    /** 足迹坐标系（底左为原点）下的整模北向盒，按造型。 */
    private final VoxelShape[] fullShapesNorth;
    private final WallPlaneFootprint footprint;
    /** geo 局部原点相对底左的格偏移（北向 U=右、V=上）。 */
    private final int geoOffsetU;
    private final int geoOffsetV;
    private final String displayNameZh;
    private final String displayNameEn;

    public StyledWindowSeriesSpec(
            StyledWindowSeriesId id,
            String[] colorIds,
            String[] textureStems,
            String[] geoBasenames,
            VoxelShape[] fullShapesNorth,
            WallPlaneFootprint footprint,
            int geoOffsetU,
            int geoOffsetV,
            String displayNameZh,
            String displayNameEn) {
        if (colorIds.length != textureStems.length) {
            throw new IllegalArgumentException(id + ": colorIds vs textureStems length");
        }
        if (geoBasenames.length == 0 || fullShapesNorth.length != geoBasenames.length) {
            throw new IllegalArgumentException(id + ": geo vs shapesNorth length");
        }
        this.id = id;
        this.colorIds = colorIds.clone();
        this.textureStems = textureStems.clone();
        this.geoBasenames = geoBasenames.clone();
        this.shapeCount = geoBasenames.length;
        this.fullShapesNorth = fullShapesNorth.clone();
        this.footprint = footprint;
        this.geoOffsetU = geoOffsetU;
        this.geoOffsetV = geoOffsetV;
        this.displayNameZh = displayNameZh;
        this.displayNameEn = displayNameEn;
    }

    public StyledWindowSeriesId id() {
        return id;
    }

    public WallPlaneFootprint footprint() {
        return footprint;
    }

    public int geoOffsetU() {
        return geoOffsetU;
    }

    public int geoOffsetV() {
        return geoOffsetV;
    }

    public int colorCount() {
        return colorIds.length;
    }

    public String colorId(int index) {
        return colorIds[Mth.clamp(index, 0, colorIds.length - 1)];
    }

    public String[] colorIds() {
        return colorIds.clone();
    }

    public String textureStem(int colorIndex) {
        return textureStems[Mth.clamp(colorIndex, 0, textureStems.length - 1)];
    }

    public String[] textureStems() {
        return textureStems.clone();
    }

    public int shapeCount() {
        return shapeCount;
    }

    public String geoBasename(int shape) {
        return geoBasenames[Mth.clamp(shape, 0, shapeCount - 1)];
    }

    public String[] geoBasenames() {
        return geoBasenames.clone();
    }

    /** 右击：0→1→…→末→0；单档时保持 0。 */
    public int nextShapeInCycle(int current) {
        if (shapeCount <= 1) {
            return 0;
        }
        int c = Mth.clamp(current, 0, shapeCount - 1);
        return (c + 1) % shapeCount;
    }

    public VoxelShape fullShapeNorth(int shape) {
        return fullShapesNorth[Mth.clamp(shape, 0, shapeCount - 1)];
    }

    /** 单格本地北向碰撞（足迹坐标系切片）。 */
    public VoxelShape shapeNorthForPart(int shape, int partU, int partV) {
        return WallPlanePlacement.sliceNorthToPart(fullShapeNorth(shape), partU, partV);
    }

    public String displayNameZh() {
        return displayNameZh;
    }

    public String displayNameEn() {
        return displayNameEn;
    }

    public String previewGeoPath() {
        return "geo/block/" + geoBasename(0) + ".geo.json";
    }
}
