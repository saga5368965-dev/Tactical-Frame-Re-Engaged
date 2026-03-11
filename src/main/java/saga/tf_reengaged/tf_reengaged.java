package saga.tf_reengaged;

import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;
import saga.tf_reengaged.entity.EntityTF44;
import saga.tf_reengaged.registry.ClientModEvents;
import saga.tf_reengaged.registry.EntityRegistry;
import saga.tf_reengaged.registry.SoundRegistry;

@Mod(tf_reengaged.MODID)
public class tf_reengaged {
    public static final String MODID = "tf_reengaged";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public tf_reengaged() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        EntityRegistry.register(modEventBus);
        SoundRegistry.register(modEventBus);
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(this::onAttributeCreation);

        if (FMLEnvironment.dist.isClient()) {
            modEventBus.register(ClientModEvents.class);
        }

        saga.tf_reengaged.config.TFConfig.register();
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // 共通設定
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            // ここで mesh を解決しようとしていたコード（ObjEntityModelsなど）を削除しました。
            // モデルの読み込みは RenderTF44 のコンストラクタで行うようにします。
            LOGGER.info("TF_REENGAGED: Client Setup initialized.");
        });
    }

    private void onAttributeCreation(EntityAttributeCreationEvent event) {
        event.put(EntityRegistry.NEFTHYS.get(), EntityTF44.createAttributes().build());
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("TF:REENGAGED SERVER STARTING");
    }
}