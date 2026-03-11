package saga.tf_reengaged.init;

import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import saga.tf_reengaged.entity.EntityTF44;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, "tf_reengaged");

    // これが「EntityInit.TF44」の正体です
    public static final RegistryObject<EntityType<EntityTF44>> TF44 =
            ENTITIES.register("tf44", () -> EntityType.Builder.of(EntityTF44::new, MobCategory.MONSTER)
                    .sized(1.2F, 2.5F) // ヒットボックスのサイズ
                    .build("tf44"));
}
