package saga.tf_reengaged.init;

import com.finderfeed.fdlib.systems.bedrock.animations.Animation;
import saga.tf_reengaged.tf_reengaged;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

public class BossAnims {

    // 修正後：引数は ResourceLocation 1つだけにします
    public static final Supplier<Animation> NEFTHYS_IDLE = () ->
            new Animation(new ResourceLocation(tf_reengaged.MODID, "nephthys"));

    public static final Supplier<Animation> NEFTHYS_WALK = () ->
            new Animation(new ResourceLocation(tf_reengaged.MODID, "nephthys"));

    public static final Supplier<Animation> NEFTHYS_SWORD_ATTACK = () ->
            new Animation(new ResourceLocation(tf_reengaged.MODID, "nephthys"));

}