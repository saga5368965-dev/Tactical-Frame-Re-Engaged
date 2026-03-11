package saga.tf_reengaged.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import saga.tf_reengaged.entity.EntityTF44;
import saga.tf_reengaged.tf_reengaged;
import wmlib.client.obj.SAObjModel;

import static net.minecraft.client.renderer.entity.LivingEntityRenderer.getOverlayCoords;

public class RenderTF44 extends EntityRenderer<EntityTF44> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(tf_reengaged.MODID, "textures/mob/tf44.png");
    private static final ResourceLocation EYES = new ResourceLocation(tf_reengaged.MODID, "textures/mob/tf44eye.png");
    // OBJのパス指定をResourceLocationのまま保持（toStringによるバグ防止）
    private static final ResourceLocation MODEL_LOCATION = new ResourceLocation(tf_reengaged.MODID, "models/entity/nephthys.obj");

    private final SAObjModel model;

    public RenderTF44(EntityRendererProvider.Context context) {
        super(context);
        // パス指定の修正
        this.model = new SAObjModel(MODEL_LOCATION.toString());
    }

    @Override
    public void render(EntityTF44 entity, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int light) {
        if (this.model == null) return;

        poseStack.pushPose();

        // --- 1. 基本セットアップ ---
        float ageInTicks = (float)entity.tickCount + partialTicks;
        float floating = Mth.sin(ageInTicks * 0.1f) * 0.15f;
        poseStack.translate(0, 0.5D + floating, 0); // 少し浮かせて地面埋まりを防止

        float bodyYaw = Mth.rotLerp(partialTicks, entity.yRotO, entity.getYRot());
        poseStack.mulPose(Axis.YP.rotationDegrees(-bodyYaw));

        // --- 2. メインパスの描画 ---
        // ダメージ時の赤らみをバニラ標準のOverlayで制御（RenderSystemを直接使わない）
        int overlay = getOverlayCoords(entity, partialTicks);

        // RenderTypeをこの場で生成・指定するのが最も確実
        RenderType mainRt = RenderType.entityCutoutNoCull(TEXTURE);
        this.model.setRender(mainRt, null, poseStack, light);

        // 全パーツループ（isEyePass: false）
        renderFullModel(poseStack, entity, partialTicks, ageInTicks, false, light, overlay);

        // --- 3. 発光パスの描画 ---
        RenderType eyesRt = RenderType.eyes(EYES);
        this.model.setRender(eyesRt, null, poseStack, 15728880); // フルブライト

        // 全パーツループ（isEyePass: true）
        renderFullModel(poseStack, entity, partialTicks, ageInTicks, true, 15728880, overlay);

        poseStack.popPose();

        // ネームタグなどは最後に描画
        super.render(entity, yaw, partialTicks, poseStack, buffer, light);
    }

    private void renderFullModel(PoseStack poseStack, EntityTF44 entity, float partialTicks, float ageInTicks, boolean isEyePass, int light, int overlay) {
        String[] parts = {
                "body", "head", "eye", "weapon", "rightarm", "rightarmfore", "leftarm", "leftarmfore",
                "wing1", "wing2", "wing3", "wing4", "wing5", "wing6",
                "rightthighs", "leftthighs", "rightleg", "leftleg"
        };

        for (String part : parts) {
            if (isEyePass && !part.equals("eye") && !part.equals("head")) continue;

            poseStack.pushPose();
            applyOldLogicAnimations(part, poseStack, entity, partialTicks, ageInTicks);

            // 1.20.1ではrenderPartを呼ぶ際、モデル自体が持つバッファに書き込まれます
            model.renderPart(part);

            poseStack.popPose();
        }
    }
    private void applyOldLogicAnimations(String part, PoseStack poseStack, EntityTF44 entity, float partialTicks, float ageInTicks) {
        // 旧コードのanimationTime1, 2, 3の状態を取得
        int anim1 = entity.animationTime1;
        int anim2 = entity.animationTime2;
        int anim3 = entity.animationTime3;

        // 移動量の計算 (旧コードの f > 0.0F の判定用)
        double moveDist = Math.sqrt(entity.getDeltaMovement().x * entity.getDeltaMovement().x + entity.getDeltaMovement().z * entity.getDeltaMovement().z);
        boolean isMoving = moveDist > 0.001D;

        // --- 共通：頭部の回転 ---
        if (part.equals("head") || part.equals("eye")) {
            float headYaw = Mth.rotLerp(partialTicks, entity.yHeadRotO, entity.yHeadRot);
            float headPitch = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());
            float yawRotation = headYaw - entity.getYRot();

            rotateAround(poseStack, 0, 4.1, 0, Axis.YP, yawRotation);
            rotateAround(poseStack, 0, 4.1, 0, Axis.XP, headPitch);
        }

        // --- 旧コードのif-elseロジックを忠実に再現 ---
        if (anim1 > 0) { // Wide Slash 等のアニメーション
            // 体の傾き
            if (part.equals("body") || part.startsWith("wing")) {
                rotateAround(poseStack, 0, 0, 0, Axis.YP, -50.0F);
            }
            // 右腕と武器の振りかぶり
            if (part.equals("rightarm") || part.equals("rightarmfore") || part.equals("weapon")) {
                rotateAround(poseStack, 0, 3.84, 0, Axis.YP, -80.0F);
                if (part.equals("weapon")) {
                    rotateAround(poseStack, 0, 2.5, 0, Axis.YP, 180.0F); // 武器を逆手に
                }
            }
        }
        else if (anim2 > 0) { // Barrage (翼の展開)
            if (part.startsWith("wing")) {
                float wingIndex = Float.parseFloat(part.substring(4));
                // 旧コードのwing1~6それぞれの複雑な座標オフセットと回転を再現
                float spread = Mth.sin(ageInTicks * 0.2f) * 10f; // 1.20らしいなめらかさを追加
                if (wingIndex <= 3) {
                    rotateAround(poseStack, -0.6 * wingIndex, 5.0, 0, Axis.YP, -40.0F + spread);
                } else {
                    rotateAround(poseStack, 0.6 * (wingIndex-3), 5.0, 0, Axis.YP, 40.0F - spread);
                }
            }
        }
        else if (anim3 > 0) { // Impact (両腕を上げる)
            if (part.contains("arm")) {
                rotateAround(poseStack, 0, 3.84, 0, Axis.XP, -160.0F);
            }
        }
        else if (isMoving) { // 歩行モーション
            float walkAnim = Mth.sin(ageInTicks * 0.2f) * 15f;
            if (part.equals("body")) rotateAround(poseStack, 0, 4.1, 0, Axis.XP, 30.0F); // 前傾姿勢
            if (part.equals("rightthighs") || part.equals("rightleg")) {
                rotateAround(poseStack, 0, 2.79, 0, Axis.ZP, -10.0F + walkAnim);
            }
            if (part.equals("leftthighs") || part.equals("leftleg")) {
                rotateAround(poseStack, 0, 2.79, 0, Axis.ZP, 10.0F - walkAnim);
            }
        }
        else { // 待機モーション (Idle)
            float idleWave = Mth.sin(ageInTicks * 0.05f) * 5f;
            if (part.startsWith("wing")) {
                rotateAround(poseStack, 0, 4.0, 0, Axis.ZP, idleWave);
            }
        }
    }

    private void rotateAround(PoseStack poseStack, double x, double y, double z, Axis axis, float degrees) {
        poseStack.translate(x, y, z);
        poseStack.mulPose(axis.rotationDegrees(degrees));
        poseStack.translate(-x, -y, -z);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityTF44 entity) {
        return TEXTURE;
    }
}