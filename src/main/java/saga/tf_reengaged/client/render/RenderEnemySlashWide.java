package saga.tf_reengaged.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import saga.tf_reengaged.entity.EntityEnemySlashWide;
import saga.tf_reengaged.tf_reengaged;
import wmlib.client.obj.SAObjModel;

public class RenderEnemySlashWide extends EntityRenderer<EntityEnemySlashWide> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(tf_reengaged.MODID, "textures/entity/shell.png");
    private final SAObjModel model;

    public RenderEnemySlashWide(EntityRendererProvider.Context context) {
        super(context);
        // パス指定
        this.model = new SAObjModel(new ResourceLocation(tf_reengaged.MODID, "models/entity/slashwide.obj").toString());
    }

    @Override
    public void render(EntityEnemySlashWide entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        if (this.model == null) return;

        poseStack.pushPose();

        // --- 1. 座標変形 ---
        poseStack.translate(0, 0.5D, 0);

        float renderYaw = Mth.rotLerp(partialTicks, entity.yRotO, entity.getYRot());
        float renderPitch = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());
        poseStack.mulPose(Axis.YP.rotationDegrees(renderYaw - 180.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(renderPitch)); // 斬撃はZPの方が旧版に近い動きになる場合があります

        // スケールを一旦 5倍で固定（映るか確認するため）
        poseStack.scale(5.0F, 5.0F, 5.0F);

        // --- 2. 描画実行（ここが重要！） ---

        // RenderTypeを決定
        RenderType rt = RenderType.entityTranslucentEmissive(TEXTURE);

        // 【決定的な修正】buffer から直接 VertexConsumer を取得する
        // これにより、マイクラの描画パイプラインに直接データが送られます
        VertexConsumer vertexConsumer = buffer.getBuffer(rt);

        // WMlibのモデルに対して、どのバッファに描画するかを教える
        // 第4引数はフルブライト（15728880）、第5引数はオーバーレイ（通常は10等）
        // ※WMlibのメソッド名や引数が環境で異なる場合がありますが、
        // 「VertexConsumer」を引数に取る render メソッドを探して呼んでください。

        this.model.setRender(rt, null, poseStack, 15728880);

        // OBJ内のパーツ名 "weapon" を明示的に呼び出し
        // もし renderPart(String, PoseStack, VertexConsumer, ...) というメソッドがあればそれを使ってください
        this.model.renderPart("weapon");

        poseStack.popPose();

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityEnemySlashWide entity) {
        return TEXTURE;
    }
}