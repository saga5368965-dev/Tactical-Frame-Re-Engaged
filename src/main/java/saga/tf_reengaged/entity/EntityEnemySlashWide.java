package saga.tf_reengaged.entity;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import saga.tf_reengaged.registry.EntityRegistry;

import java.util.List;

public class EntityEnemySlashWide extends ThrowableProjectile {
    private double damage = 15.0D;
    private int lifeTime = 0;

    public EntityEnemySlashWide(EntityType<? extends ThrowableProjectile> type, Level level) {
        super(type, level);
    }

    public EntityEnemySlashWide(Level level, LivingEntity shooter) {
        super(EntityRegistry.ENEMY_SLASH_WIDE.get(), shooter, level);
    }

    @Override
    public void tick() {
        super.tick();

        // 旧コードの isGravity() 相当の処理: 周囲への広範囲攻撃判定
        if (!this.level().isClientSide) {
            this.applyAreaAttack();

            // 弾の寿命（特撮の演出上、どこまでも飛ばずに一定距離で消えるのが自然）
            this.lifeTime++;
            if (this.lifeTime > 100) {
                this.discard();
            }
        }
    }

    /**
     * 旧コードの AABB 拡張ダメージ判定を再現
     */
    private void applyAreaAttack() {
        // 斬撃の見た目（モデル）に合わせて判定を横に広げる (横3.0, 上下0.5, 前後3.0)
        AABB searchArea = this.getBoundingBox().inflate(3.0D, 0.5D, 3.0D);
        List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class, searchArea);

        for (LivingEntity target : targets) {
            // 自分（発射したボス）や味方には当たらないように判定
            if (target != this.getOwner() && !(target instanceof EntityTF44)) {
                DamageSource source = this.damageSources().mobAttack((LivingEntity) this.getOwner());

                // 無敵時間を無視してダメージを与える（旧コードの field_70172_ad = 0 相当）
                target.invulnerableTime = 0;
                target.hurt(source, (float) this.damage);
            }
        }
    }

    @Override
    protected void onHitEntity(@NotNull EntityHitResult result) {
        // 周囲判定でダメージを与えているので、直接当たった時の処理は discard() のみでOK
        if (!this.level().isClientSide) {
            this.discard();
        }
    }

    @Override
    protected void onHit(@NotNull HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide) {
            this.discard();
        }
    }

    @Override
    protected void defineSynchedData() {
        // 同期データが必要な場合はここに追加
    }

    // 重力の影響をゼロにする（真っ直ぐ飛ぶ斬撃波）
    @Override
    protected float getGravity() {
        return 0.0F;
    }

    public void setDamage(double damage) { this.damage = damage; }
    public double getDamage() { return this.damage; }

    @Override
    public @NotNull Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}