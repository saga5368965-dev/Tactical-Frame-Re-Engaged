package saga.tf_reengaged.registry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import saga.tf_reengaged.tf_reengaged; // メインクラスのパッケージに合わせて変更してください

public class SoundRegistry {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, tf_reengaged.MODID);

    // ネフティスの声 (Ambient)
    public static final RegistryObject<SoundEvent> TF_SAY1 = registerSound("tfsay1");
    public static final RegistryObject<SoundEvent> TF_SAY2 = registerSound("tfsay2");
    public static final RegistryObject<SoundEvent> TF_SAY3 = registerSound("tfsay3");

    // ネフティスの移動音 (Fly)
    public static final RegistryObject<SoundEvent> TF_ROBOT_FLY = registerSound("tfrobotfly");
    public static final RegistryObject<SoundEvent> TF_HURT = registerSound("tfhurt");
    // ヘルパーメソッド
    private static RegistryObject<SoundEvent> registerSound(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(tf_reengaged.MODID, name)));
    }

    // メインクラスから呼ぶための初期化メソッド
    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}