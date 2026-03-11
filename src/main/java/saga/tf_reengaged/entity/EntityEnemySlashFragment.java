package saga.tf_reengaged.entity;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import saga.tf_reengaged.registry.EntityRegistry;

public class EntityEnemySlashFragment extends ThrowableProjectile {
    private double damage = 5.0D;
    private int life = 100; // 5秒で自動消滅

    public EntityEnemySlashFragment(EntityType<? extends ThrowableProjectile> type, Level level) {
        super(type, level);
    }

    public EntityEnemySlashFragment(Level level, LivingEntity shooter) {
        super(EntityRegistry.ENEMY_SLASH_FRAGMENT.get(), shooter, level);
    }

    @Override
    public void tick() {
        super.tick();
        // 5秒経過、または水中での減速（旧コードの inWaterSpeed 0.99 相当）
        if (!this.level().isClientSide) {
            if (this.tickCount > life) {
                this.discard();
            }
            if (this.isInWater()) {
                this.setDeltaMovement(this.getDeltaMovement().scale(0.99D));
            }
        }
    }

    @Override
    protected void onHitEntity(@NotNull EntityHitResult result) {
        if (!this.level().isClientSide) {
            if (result.getEntity() instanceof LivingEntity living) {
                // 1. ダメージを与える
                living.hurt(this.damageSources().mobAttack((LivingEntity) this.getOwner()), (float) this.damage);

                // 2. 旧コードの bulletHit (デバフ付与) の再現
                // もし自作の「脆弱(Vulnerability)」ポーションがある場合は、MobEffects.WITHER 等をそれに差し替えてください。
                // 特撮の「防御ダウン」演出なら MobEffects.DAMAGE_RESISTANCE (耐性) の負の効果や、MobEffects.WITHER が近いです。
                living.addEffect(new MobEffectInstance(MobEffects.WITHER, 200, 0));

                // プレイヤーがガード中 (func_184585_cz) であっても、
                // 旧コードではガードを無視してデバフをかけていたようなので、あえて条件分岐せず付与します。
            }
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
    protected float getGravity() {
        return 0.0F; // 重力無視で直進
    }

    @Override
    protected void defineSynchedData() {}

    public void setDamage(double damage) { this.damage = damage; }

    @Override
    public @NotNull Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}