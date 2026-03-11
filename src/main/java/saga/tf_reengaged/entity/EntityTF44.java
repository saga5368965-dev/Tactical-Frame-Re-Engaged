package saga.tf_reengaged.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import saga.tf_reengaged.registry.SoundRegistry;

/**
 * Nephthys (TF44) - Re-Engaged Edition
 * レンダラー側の旧ロジック挙動とAIタイミングを完全同期
 */
public class EntityTF44 extends Monster implements RangedAttackMob {

    private final ServerBossEvent bossEvent = new ServerBossEvent(this.getDisplayName(), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS);

    // データアクセサー (レンダラー側のアニメーション分岐と同期)
    private static final EntityDataAccessor<Integer> ANIM_STATE = SynchedEntityData.defineId(EntityTF44.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> WEAPON_TYPE = SynchedEntityData.defineId(EntityTF44.class, EntityDataSerializers.INT);

    // 旧コード互換のティックカウンタ (レンダラーが参照する)
    public int animationTime1, animationTime2, animationTime3;
    private int attackCooldown = 0;

    public EntityTF44(EntityType<? extends EntityTF44> type, Level level) {
        super(type, level);
        this.xpReward = 500;
        this.setMaxUpStep(2.5F);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        // 攻撃のきっかけを作るGoal (インターバルは短めにして内部のattackCooldownで制御)
        this.goalSelector.addGoal(1, new RangedAttackGoal(this, 1.2D, 20, 40.0F));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 15.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.FOLLOW_RANGE, 100.0D)
                .add(Attributes.MAX_HEALTH, 1000.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.33D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.ARMOR, 30.0D)
                .add(Attributes.ATTACK_DAMAGE, 20.0D);
    }

    @Override
    public void aiStep() {
        super.aiStep();

        // クライアント側でもアニメーション時間を減算して滑らかに描画
        if (this.animationTime1 > 0) this.animationTime1--;
        if (this.animationTime2 > 0) this.animationTime2--;
        if (this.animationTime3 > 0) this.animationTime3--;

        if (!this.level().isClientSide) {
            if (this.attackCooldown > 0) this.attackCooldown--;

            // 稼働中のアニメーション状態をSynchedEntityDataへ反映 (レンダラーへの通知)
            updateAnimationState();

            // 進行中のアニメーションに応じたロジック実行
            handleAdvancedAI();

            this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
            if (this.tickCount % 40 == 0) this.heal(2.0F);
        }
    }

    private void updateAnimationState() {
        if (this.animationTime1 > 0) this.entityData.set(ANIM_STATE, 1);
        else if (this.animationTime2 > 0) this.entityData.set(ANIM_STATE, 2);
        else if (this.animationTime3 > 0) this.entityData.set(ANIM_STATE, 3);
        else this.entityData.set(ANIM_STATE, 0);
    }

    private void handleAdvancedAI() {
        LivingEntity target = this.getTarget();
        if (target == null) return;

        float healthRatio = this.getHealth() / this.getMaxHealth();

        // --- Wide Slash (Time1) ---
        if (this.animationTime1 > 0) {
            // 旧レンダラーが「振りかぶっている」タイミングで攻撃判定
            if (this.animationTime1 == 30 || (this.animationTime1 == 20 && healthRatio < 0.7F) || (this.animationTime1 == 10 && healthRatio < 0.4F)) {
                spawnWideSlash(target);
            }
        }

        // --- Fragment Barrage (Time2) ---
        if (this.animationTime2 > 0) {
            // 翼が展開されている間、高速連射
            int interval = healthRatio < 0.5F ? 1 : 2;
            if (this.animationTime2 % interval == 0) {
                spawnFragment(target, 3.0F);
            }
        }

        // --- Impact / Shockwave (Time3) ---
        if (this.animationTime3 > 0) {
            // 両腕を上げきった瞬間に爆発
            if (this.animationTime3 == 30) {
                float dmg = healthRatio < 0.5F ? 30.0F : 20.0F;
                this.areaDamage(8.0D, dmg, true);
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 4.0F, 0.8F);
            }
        }

        // ピンチ時の自動反撃 (Time系が動いていなくても確率で発生)
        if (healthRatio < 0.5F && this.tickCount % 20 == 0) {
            spawnFragment(target, 2.0F);
        }
    }

    @Override
    public void performRangedAttack(LivingEntity target, float velocity) {
        if (this.attackCooldown <= 0 && animationTime1 <= 0 && animationTime2 <= 0 && animationTime3 <= 0) {
            int action = this.random.nextInt(3);

            // 旧コードの技選択をTimeに変換
            switch (action) {
                case 0 -> { // Slash
                    this.animationTime1 = 60;
                    this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundRegistry.TF_SAY1.get(), SoundSource.HOSTILE, 2.0F, 1.0F);
                }
                case 1 -> { // Barrage
                    this.animationTime2 = 80;
                    this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundRegistry.TF_SAY2.get(), SoundSource.HOSTILE, 2.0F, 1.0F);
                }
                case 2 -> { // Impact
                    this.animationTime3 = 60;
                }
            }

            // 特殊行動（テレポート）を確率で追加
            if (this.random.nextFloat() < 0.3F) {
                this.teleportToEntity(target);
            }

            this.attackCooldown = 30; // 次の攻撃までの遊び
            this.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 5));
        }
    }

    // --- 以下、ユーティリティメソッド (変更なし) ---

    private void spawnWideSlash(LivingEntity target) {
        double dx = target.getX() - this.getX();
        double dy = target.getY() - this.getY() - 2.1D;
        double dz = target.getZ() - this.getZ();
        float[] angles = {-0.3F, 0.0F, 0.3F, -0.7F, 0.7F};
        for (float angle : angles) {
            EntityEnemySlashWide slash = new EntityEnemySlashWide(this.level(), this);
            slash.setPos(this.getX(), this.getY() + 2.1D, this.getZ());
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);
            slash.shoot(dx * cos - dz * sin, dy, dx * sin + dz * cos, 3.2F, 0.0F);
            this.level().addFreshEntity(slash);
        }
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.HOSTILE, 3.0F, 0.5F);
    }

    private void spawnFragment(LivingEntity target, float speed) {
        EntityEnemySlashFragment frag = new EntityEnemySlashFragment(this.level(), this);
        frag.setPos(this.getX() + (this.random.nextDouble() - 0.5) * 4.0, this.getY() + 2.5D, this.getZ() + (this.random.nextDouble() - 0.5) * 4.0);
        double dx = target.getX() - frag.getX();
        double dy = target.getY() - frag.getY();
        double dz = target.getZ() - frag.getZ();
        frag.shoot(dx, dy, dz, speed, 2.0F);
        this.level().addFreshEntity(frag);
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundRegistry.TF_ROBOT_FLY.get(), SoundSource.HOSTILE, 1.0F, 1.5F);
    }

    private void areaDamage(double range, float damage, boolean applyBlindness) {
        AABB area = this.getBoundingBox().inflate(range);
        this.level().getEntitiesOfClass(LivingEntity.class, area).forEach(e -> {
            if (e != this && !(e instanceof EntityTF44)) {
                e.hurt(this.damageSources().mobAttack(this), damage);
                if (applyBlindness) e.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 140, 0));
            }
        });
    }

    protected void teleportToEntity(Entity entity) {
        this.teleportTo(entity.getX(), entity.getY() + 3.0D, entity.getZ());
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 2.0F, 1.0F);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ANIM_STATE, 0);
        this.entityData.define(WEAPON_TYPE, 0);
    }

    @Override
    public boolean hurt(@NotNull DamageSource source, float amount) {
        if (amount > 25.0F) amount = 25.0F;
        if (this.random.nextFloat() < 0.2F) this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundRegistry.TF_HURT.get(), SoundSource.HOSTILE, 1.5F, 1.0F);
        return super.hurt(source, amount);
    }

    @Override public void startSeenByPlayer(@NotNull ServerPlayer player) { super.startSeenByPlayer(player); this.bossEvent.addPlayer(player); }
    @Override public void stopSeenByPlayer(@NotNull ServerPlayer player) { super.stopSeenByPlayer(player); this.bossEvent.removePlayer(player); }
}