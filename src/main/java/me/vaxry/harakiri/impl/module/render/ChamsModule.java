package me.vaxry.harakiri.impl.module.render;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.event.render.EventRender3D;
import me.vaxry.harakiri.framework.Module;
import me.vaxry.harakiri.framework.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

public class ChamsModule extends Module {

    private boolean selfBdown = false;
    private boolean friendBdown = false;
    private boolean enemyBdown = false;


    public final Value<Boolean> self = new Value<Boolean>("LocalPlayer", new String[]{"Local", "l"}, "Local player master switch", false);
    public final Value<Integer> selfR = new Value<Integer>("Local R", new String[]{"LocalR", "lr"}, "Local player R", 255, 0, 255, 1);
    public final Value<Integer> selfG = new Value<Integer>("Local G", new String[]{"LocalG", "lg"}, "Local player G", 255, 0, 255, 1);
    public final Value<Integer> selfB = new Value<Integer>("Local B", new String[]{"LocalB", "lb"}, "Local player B", 255, 0, 255, 1);
    public final Value<Integer> selfA = new Value<Integer>("Local A", new String[]{"LocalA", "la"}, "Local player A", 255, 0, 255, 1);
    public final Value<Boolean> selfBL = new Value<Boolean>("Local Blink", new String[]{"LocalB", "lb"}, "Local player Blink", false);
    public final Value<Float> selfBS = new Value<Float>("Local Blink Speed", new String[]{"LocalBS", "lbs"}, "Local player Blink speed", 1.f, 0.1f, 10f, 0.2f);
    public final Value<Boolean> selfNoAr = new Value<Boolean>("Local No Armor", new String[]{"LocalNA", "lna"}, "Local player no armor render", false);
    public final Value<Boolean> selfFGl = new Value<Boolean>("Local Force Glint", new String[]{"LocalFG", "lfg"}, "Local player force glint", false);
    public final Value<Boolean> selfLTH = new Value<Boolean>("Local Lightning", new String[]{"LocalLTH", "llth"}, "Local player force lightning", false);
    public final Value<Boolean> selfANG = new Value<Boolean>("Local Angel", new String[]{"LocalANG", "lang"}, "Local player angel effect", false);


    public final Value<Boolean> friend = new Value<Boolean>("Friends", new String[]{"Friends", "f"}, "Friends master switch", false);
    public final Value<Integer> friendR = new Value<Integer>("Friends R", new String[]{"FriendsR", "fr"}, "Friends R", 255, 0, 255, 1);
    public final Value<Integer> friendG = new Value<Integer>("Friends G", new String[]{"FriendsG", "fg"}, "Friends G", 255, 0, 255, 1);
    public final Value<Integer> friendB = new Value<Integer>("Friends B", new String[]{"FriendsB", "fb"}, "Friends B", 255, 0, 255, 1);
    public final Value<Integer> friendA = new Value<Integer>("Friends A", new String[]{"FriendsA", "fa"}, "Friends A", 255, 0, 255, 1);
    public final Value<Boolean> friendBL = new Value<Boolean>("Friends Blink", new String[]{"FriendsB", "fb"}, "Friends Blink", false);
    public final Value<Float> friendBS = new Value<Float>("Friends Blink Speed", new String[]{"FriendsBS", "fbs"}, "Friends Blink speed", 1.f, 0.1f, 10f, 0.2f);
    public final Value<Boolean> friendNoAr = new Value<Boolean>("Friends No Armor", new String[]{"FriendsNA", "fna"}, "Friends no armor render", false);
    public final Value<Boolean> friendFGl = new Value<Boolean>("Friends Force Glint", new String[]{"FriendsFG", "ffg"}, "Friends force glint", false);


    public final Value<Boolean> enemy = new Value<Boolean>("Enemies", new String[]{"Enemies", "e"}, "Enemies master switch", false);
    public final Value<Integer> enemyR = new Value<Integer>("Enemies R", new String[]{"EnemiesR", "er"}, "Enemies R", 255, 0, 255, 1);
    public final Value<Integer> enemyG = new Value<Integer>("Enemies G", new String[]{"EnemiesG", "eg"}, "Enemies G", 255, 0, 255, 1);
    public final Value<Integer> enemyB = new Value<Integer>("Enemies B", new String[]{"EnemiesB", "eb"}, "Enemies B", 255, 0, 255, 1);
    public final Value<Integer> enemyA = new Value<Integer>("Enemies A", new String[]{"EnemiesA", "ea"}, "Enemies A", 255, 0, 255, 1);
    public final Value<Boolean> enemyBL = new Value<Boolean>("Enemy Blink", new String[]{"EnemyB", "eb"}, "Enemy Blink", false);
    public final Value<Float> enemyBS = new Value<Float>("Enemy Blink Speed", new String[]{"EnemyBS", "ebs"}, "Enemy Blink speed", 1.f, 0.1f, 10f, 0.2f);
    public final Value<Boolean> enemyNoAr = new Value<Boolean>("Enemy No Armor", new String[]{"EnemyNA", "ena"}, "Enemy no armor render", false);
    public final Value<Boolean> enemyFGl = new Value<Boolean>("Enemy Force Glint", new String[]{"EnemyFG", "efg"}, "Enemy force glint", false);


    public EntityPlayer lastPlayer = null;

    public ChamsModule() {
        super("Chams", new String[]{"Chams"}, "Changes the renderer's behavior.", "NONE", -1, ModuleType.RENDER);
    }

    @Listener
    public void onRender3D(EventRender3D event){
        if(!this.isEnabled())
            return;

        // Self
        if(this.selfBL.getValue()){
            if(this.selfBdown){
                this.selfA.setValue((int)(this.selfA.getValue() - this.selfBS.getValue()));
                if(this.selfA.getValue() < 2)
                    this.selfBdown = false;
            }else{
                this.selfA.setValue((int)(this.selfA.getValue() + this.selfBS.getValue() * 2.f));
                if(this.selfA.getValue() > 253)
                    this.selfBdown = true;
            }
        }

        // Friend
        if(this.friendBL.getValue()){
            if(this.friendBdown){
                this.friendA.setValue((int)(this.friendA.getValue() - this.friendBS.getValue()));
                if(this.friendA.getValue() < 2)
                    this.friendBdown = false;
            }else{
                this.friendA.setValue((int)(this.friendA.getValue() + this.friendBS.getValue()));
                if(this.friendA.getValue() > 253)
                    this.friendBdown = true;
            }
        }

        // Enemy
        if(this.enemyBL.getValue()){
            if(this.enemyBdown){
                this.enemyA.setValue((int)(this.enemyA.getValue() - this.enemyBS.getValue()));
                if(this.enemyA.getValue() < 2)
                    this.enemyBdown = false;
            }else{
                this.enemyA.setValue((int)(this.enemyA.getValue() + this.enemyBS.getValue()));
                if(this.enemyA.getValue() > 253)
                    this.enemyBdown = true;
            }
        }
    }

    @SubscribeEvent
    public void onRenderPlayerEvent(RenderPlayerEvent.Pre event){
        if(!(event.getEntity() instanceof EntityPlayer))
            return;

        EntityPlayer e = (EntityPlayer)event.getEntity();

        ChamsModule chamsModule = (ChamsModule) Harakiri.get().getModuleManager().find(ChamsModule.class);

        if(!chamsModule.isEnabled() || e == null)
            return;

        if(Harakiri.get().getFriendManager().isFriend(e) != null && chamsModule.friend.getValue() ||
                Harakiri.get().getFriendManager().isFriend(e) == null && chamsModule.enemy.getValue() ||
                Minecraft.getMinecraft().player.getName().equalsIgnoreCase(e.getName()) && chamsModule.self.getValue()){

            GlStateManager.enableAlpha();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            GlStateManager.depthMask(false);

            if(Minecraft.getMinecraft().player.getName().equalsIgnoreCase(e.getName())){
                GL11.glColor4f(chamsModule.selfR.getValue() / 255.f,chamsModule.selfG.getValue() / 255.f,chamsModule.selfB.getValue() / 255.f,chamsModule.selfA.getValue() / 255.f);
            } else if(Harakiri.get().getFriendManager().isFriend(e) != null){
                //friend settings
                GL11.glColor4f(chamsModule.friendR.getValue() / 255.f,chamsModule.friendG.getValue() / 255.f,chamsModule.friendB.getValue() / 255.f,chamsModule.friendA.getValue() / 255.f);
            } else if(Harakiri.get().getFriendManager().isFriend(e) == null){
                //enemy settings
                GL11.glColor4f(chamsModule.enemyR.getValue() / 255.f,chamsModule.enemyG.getValue() / 255.f,chamsModule.enemyB.getValue() / 255.f,chamsModule.enemyA.getValue() / 255.f);
            }
        }
    }

}
