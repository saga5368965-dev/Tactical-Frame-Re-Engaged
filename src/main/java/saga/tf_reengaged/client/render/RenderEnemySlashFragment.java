package saga.tf_reengaged.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import saga.tf_reengaged.tf_reengaged;
import saga.tf_reengaged.entity.EntityEnemySlashFragment;
import wmlib.client.obj.SAObjModel;

public class RenderEnemySlashFragment extends EntityRenderer<EntityEnemySlashFragment> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(tf_reengaged.MODID, "textures/entity/shell.png");
    private final SAObjModel model;
    // 1.20ではRenderType.eyes()が「最も確実に光って映る」設定です
    private final RenderType rt;

    public RenderEnemySlashFragment(EntityRendererProvider.Context context) {
        super(context);
        this.model = new SAObjModel(new ResourceLocation(tf_reengaged.MODID, "models/entity/slashfrag.obj").toString());
        // BlendGlowGlintが不安定な場合があるため、標準のeyes（発光・カリングなし）をベースにします
        this.rt = RenderType.eyes(TEXTURE);
    }

    @Override
    public void render(EntityEnemySlashFragment entity, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int light) {
        if (this.model == null) return;

        poseStack.pushPose();

        // 基本のサイズと位置（まずは大きくして確認）
        poseStack.translate(0, 0.5D, 0);
        poseStack.scale(2.0F, 2.0F, 2.0F);

        // 回転の補間
        float fYaw = Mth.rotLerp(partialTicks, entity.yRotO, entity.getYRot());
        float fPitch = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());
        poseStack.mulPose(Axis.YP.rotationDegrees(fYaw - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(fPitch));

        // 自転アニメーション
        float rotation = (entity.tickCount + partialTicks) * 20.0F;
        poseStack.mulPose(Axis.XP.rotationDegrees(rotation));

        // --- 修正ポイント：RenderSystemを使わずWMlibの機能を正しく使う ---

        // 第4引数を 15728880 にすることで、暗い場所でも真っ白（フルブライト）に映ります
        // 1.20ではRenderSystem.disableBlend()などはrender内で呼んではいけません
        this.model.setRender(this.rt, null, poseStack, 15728880);

        // renderAllではなく、バッファを指定して描画するのが1.20流
        // WMlibが内部で正しく処理するように設定して描画
        this.model.renderAll();

        poseStack.popPose();

        super.render(entity, yaw, partialTicks, poseStack, buffer, light);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityEnemySlashFragment entity) {
        return TEXTURE;
    }
}