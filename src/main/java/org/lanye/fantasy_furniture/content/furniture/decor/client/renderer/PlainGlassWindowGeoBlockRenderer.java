package org.lanye.fantasy_furniture.content.furniture.decor.client.renderer;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lanye.fantasy_furniture.FantasyFurniture;
import org.lanye.fantasy_furniture.content.furniture.decor.PlainGlassWindowMaterials;
import org.lanye.fantasy_furniture.content.furniture.decor.PlainGlassWindowSharedTextures;
import org.lanye.fantasy_furniture.content.furniture.decor.PlainGlassWindowShapes;
import org.lanye.fantasy_furniture.content.furniture.decor.block.PlainGlassWindowBlock;
import org.lanye.fantasy_furniture.content.furniture.decor.blockentity.PlainGlassWindowBlockEntity;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

/**
 * 普通玻璃窗：仅绑定 geo / 纹理 / 静态动画资源；姿态与几何与 Blockbench 的对齐由导出脚本写入的 geo 决定，
 * 本类<strong>不</strong>覆盖 {@link GeoBlockRenderer} 的渲染或旋转逻辑。
 */
@OnlyIn(Dist.CLIENT)
public final class PlainGlassWindowGeoBlockRenderer extends GeoBlockRenderer<PlainGlassWindowBlockEntity> {

    // #region agent log
    private static final Path AGENT_LOG =
            Path.of(System.getProperty("user.dir", ".")).resolve("debug-cac4e1.log").toAbsolutePath();
    private static final Set<Integer> AGENT_LOGGED_SHAPES = ConcurrentHashMap.newKeySet();

    private static String agentSha256GeoResource(ResourceLocation loc) {
        try {
            var opt = Minecraft.getInstance().getResourceManager().getResource(loc);
            if (opt.isEmpty()) {
                return "missing_resource";
            }
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            try (InputStream in = opt.get().open()) {
                byte[] buf = new byte[16384];
                int n;
                while ((n = in.read(buf)) > 0) {
                    md.update(buf, 0, n);
                }
            }
            byte[] d = md.digest();
            StringBuilder sb = new StringBuilder(d.length * 2);
            for (byte b : d) {
                sb.append(String.format(Locale.ROOT, "%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Throwable t) {
            return "sha256_error:" + t.getClass().getSimpleName();
        }
    }

    private static void agentLogModelChoice(
            int shape, Direction facing, String basename, ResourceLocation modelLoc) {
        if (!AGENT_LOGGED_SHAPES.add(shape)) {
            return;
        }
        String sha = agentSha256GeoResource(modelLoc);
        String json =
                String.format(
                        Locale.ROOT,
                        "{\"sessionId\":\"cac4e1\",\"timestamp\":%d,\"hypothesisId\":\"H1-H3,H10\","
                                + "\"location\":\"PlainGlassWindowGeoBlockRenderer.getModelResource\","
                                + "\"message\":\"first_model_resolve_per_shape\","
                                + "\"data\":{\"shape\":%d,\"facing\":\"%s\",\"basename\":\"%s\","
                                + "\"model\":\"%s\",\"geoSha256\":\"%s\","
                                + "\"logPath\":\"%s\",\"userDir\":\"%s\"}}\n",
                        System.currentTimeMillis(),
                        shape,
                        facing == null ? "null" : facing.getName(),
                        basename.replace("\\", "\\\\").replace("\"", "\\\""),
                        modelLoc.toString().replace("\\", "\\\\").replace("\"", "\\\""),
                        sha,
                        AGENT_LOG.toString().replace("\\", "\\\\").replace("\"", "\\\""),
                        Path.of(System.getProperty("user.dir", "."))
                                .toAbsolutePath()
                                .toString()
                                .replace("\\", "\\\\")
                                .replace("\"", "\\\""));
        try {
            Files.writeString(AGENT_LOG, json, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (Throwable ignored) {
        }
    }
    // #endregion

    /** 与 {@link PlainGlassWindowBlockEntity} 的 {@code PlayState.STOP} 一致；多造型共用，避免每 geo 一份空动画 JSON。 */
    private static final ResourceLocation STATIC_ANIMATION =
            ResourceLocation.fromNamespaceAndPath(
                    FantasyFurniture.MODID, "animations/block/geolib_static.animation.json");

    public PlainGlassWindowGeoBlockRenderer() {
        super(
                new GeoModel<PlainGlassWindowBlockEntity>() {
                    @Override
                    public ResourceLocation getModelResource(PlainGlassWindowBlockEntity entity) {
                        int shape = entity.getBlockState().getValue(PlainGlassWindowBlock.SHAPE);
                        String b = PlainGlassWindowShapes.geoBasename(shape);
                        ResourceLocation loc =
                                ResourceLocation.fromNamespaceAndPath(
                                        FantasyFurniture.MODID, "geo/block/" + b + ".geo.json");
                        // #region agent log
                        agentLogModelChoice(
                                shape,
                                entity.getBlockState().getValue(PlainGlassWindowBlock.FACING),
                                b,
                                loc);
                        // #endregion
                        return loc;
                    }

                    @Override
                    public ResourceLocation getTextureResource(PlainGlassWindowBlockEntity entity) {
                        int mat = PlainGlassWindowBlock.materialIndex(entity.getBlockState());
                        return PlainGlassWindowSharedTextures.textureLocationForStem(
                                FantasyFurniture.MODID, PlainGlassWindowMaterials.itemPreviewStem(mat));
                    }

                    @Override
                    public ResourceLocation getAnimationResource(PlainGlassWindowBlockEntity entity) {
                        return STATIC_ANIMATION;
                    }
                });
    }
}
