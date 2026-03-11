package saga.tf_reengaged.registry;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import saga.tf_reengaged.tf_reengaged;
import saga.tf_reengaged.client.render.RenderTF44;
import saga.tf_reengaged.client.render.RenderEnemySlashWide;
import saga.tf_reengaged.client.render.RenderEnemySlashFragment;

@Mod.EventBusSubscriber(modid = tf_reengaged.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        // 1wholibs (wmlib) のOBJローダーは、各Renderクラスでnew SAObjModelするときに
        // 初めて処理されるため、ここでの明示的な登録は不要なことが多いですが、
        // ログを出しておくとデバッグ時に安心です。
        tf_reengaged.LOGGER.info("Nephthys client systems initializing...");
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // 1. ボス本体のレンダラー
        event.registerEntityRenderer(EntityRegistry.NEFTHYS.get(), RenderTF44::new);

        // 2. 拡散スラッシュのレンダラー
        event.registerEntityRenderer(EntityRegistry.ENEMY_SLASH_WIDE.get(), RenderEnemySlashWide::new);

        // 3. 破片のレンダラー
        event.registerEntityRenderer(EntityRegistry.ENEMY_SLASH_FRAGMENT.get(), RenderEnemySlashFragment::new);

        tf_reengaged.LOGGER.info("All Nephthys-related renderers registered successfully.");
    }
}