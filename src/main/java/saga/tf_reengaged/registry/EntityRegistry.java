package saga.tf_reengaged.registry;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import saga.tf_reengaged.tf_reengaged;
import saga.tf_reengaged.entity.EntityTF44;
import saga.tf_reengaged.entity.EntityEnemySlashWide;
import saga.tf_reengaged.entity.EntityEnemySlashFragment;

public class EntityRegistry {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, tf_reengaged.MODID);

    // --- ボス：Nephthys ---
    public static final RegistryObject<EntityType<EntityTF44>> NEFTHYS =
            ENTITIES.register("nephthys", () -> EntityType.Builder.of(EntityTF44::new, MobCategory.MONSTER)
                    .sized(1.2F, 3.5F) // 当たり判定のサイズ
                    .build("nephthys"));

    // --- 攻撃用：拡散スラッシュ ---
    public static final RegistryObject<EntityType<EntityEnemySlashWide>> ENEMY_SLASH_WIDE =
            ENTITIES.register("enemy_slash_wide", () -> EntityType.Builder.<EntityEnemySlashWide>of(EntityEnemySlashWide::new, MobCategory.MISC)
                    .sized(1.0F, 1.0F)
                    .noSummon() // コマンド等での直接召喚を制限（任意）
                    .setCustomClientFactory((spawnEntity, level) -> new EntityEnemySlashWide(level, null)) // クライアント側生成用
                    .build("enemy_slash_wide"));

    // --- 攻撃用：破片 ---
    public static final RegistryObject<EntityType<EntityEnemySlashFragment>> ENEMY_SLASH_FRAGMENT =
            ENTITIES.register("enemy_slash_fragment", () -> EntityType.Builder.<EntityEnemySlashFragment>of(EntityEnemySlashFragment::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .noSummon()
                    .setCustomClientFactory((spawnEntity, level) -> new EntityEnemySlashFragment(level, null))
                    .build("enemy_slash_fragment"));

    public static void register(IEventBus eventBus) {
        ENTITIES.register(eventBus);
    }
}