package me.vaxry.harakiri.impl.module.combat;

import me.vaxry.harakiri.framework.event.EventStageable;
import me.vaxry.harakiri.framework.event.player.EventPlayerJoin;
import me.vaxry.harakiri.framework.event.player.EventPlayerUpdate;
import me.vaxry.harakiri.framework.event.render.EventRender2D;
import me.vaxry.harakiri.framework.event.world.EventAddEntity;
import me.vaxry.harakiri.framework.event.world.EventLoadWorld;
import me.vaxry.harakiri.framework.util.Timer;
import me.vaxry.harakiri.impl.module.hidden.ReconnectModule;
import me.vaxry.harakiri.impl.module.world.NukerModule;

import javax.swing.JOptionPane;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.Friend;
import me.vaxry.harakiri.framework.Module;
import me.vaxry.harakiri.framework.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

public final class AutoDisconnectModule extends Module {

    private Timer queueTimer = new Timer();
    private boolean toDisconnect = false;
    private boolean showConfirmDialog = false;

    public final Value<Float> health = new Value("Health", new String[]{"Hp"}, "The amount of health, in HP, to disconnect.", 8.0f, 0.0f, 20.0f, 0.5f);
    public final Value<Boolean> onspot = new Value("On Spotted", new String[]{"Spot"}, "When a player enters", false);

    public AutoDisconnectModule() {
        super("AutoDisconnect", new String[]{"Disconnect"}, "Automatically disconnects when your health is low enough.", "NONE", -1, ModuleType.COMBAT);
    }

    @Listener
    public void onworld(EventLoadWorld e) {
        queueTimer.reset();
    }

    @Listener
    public void onplayerjoin(EventAddEntity e) {
        if (Minecraft.getMinecraft().world == null || Minecraft.getMinecraft().player == null)
            return;

        final Minecraft mc = Minecraft.getMinecraft();

        if (!Minecraft.getMinecraft().player.isDead && e.getEntity() instanceof EntityPlayer && !e.getEntity().getName().equalsIgnoreCase(Minecraft.getMinecraft().player.getName())) {
            if (onspot.getValue()) {
                if (Harakiri.get().getModuleManager().find(NukerModule.class).isEnabled() && ((NukerModule) Harakiri.get().getModuleManager().find(NukerModule.class)).highwayMode.getValue()) {
                    final Friend friend = Harakiri.get().getFriendManager().isFriend(e.getEntity());                                                                                                                                    // todo: remember pos or delta
                    if (Minecraft.getMinecraft().player.dimension != -1 && friend == null && ((NukerModule) Harakiri.get().getModuleManager().find(NukerModule.class)).nukerStatus.contains("Gaming") && queueTimer.passed(2000) && (mc.player.posX > 100000.f || mc.player.posZ > 100000.f)) {
                        Minecraft.getMinecraft().player.connection.sendPacket(new CPacketHeldItemChange(420));

                        ReconnectModule rcm = (ReconnectModule) Harakiri.get().getModuleManager().find(ReconnectModule.class);
                        if (rcm.isEnabled())
                            rcm.auto.setValue(false);

                        this.toggle();

                        if (!showConfirmDialog) {
                            showConfirmDialog = true;
                            new Thread(() -> {
                                JOptionPane.showConfirmDialog(null, "Harakiri: Disconnected on spotted by player " + e.getEntity().getName() + " at coords " + Integer.toString(mc.player.getPosition().getX()) + ", " + Integer.toString(mc.player.getPosition().getZ()));
                                showConfirmDialog = false;
                            }).start();
                        }
                    }
                } else {
                    Minecraft.getMinecraft().player.connection.sendPacket(new CPacketHeldItemChange(420));

                    ReconnectModule rcm = (ReconnectModule) Harakiri.get().getModuleManager().find(ReconnectModule.class);
                    if (rcm.isEnabled())
                        rcm.auto.setValue(false);

                    this.toggle();
                }
            }
        }
    }

    @Listener
    public void render2d(EventRender2D e) {
        if (toDisconnect) {
            toDisconnect = false;
            final Minecraft mc = Minecraft.getMinecraft();

            ReconnectModule rm = (ReconnectModule) Harakiri.get().getModuleManager().find(ReconnectModule.class);

            // literally yoinked from the mc source
            mc.world.sendQuittingDisconnectingPacket();
            mc.loadWorld((WorldClient) null);

            rm.reconnect();

            queueTimer.reset();
        }
    }

    @Listener
    public void onUpdate(EventPlayerUpdate event) {
        if (Minecraft.getMinecraft().world == null)
            queueTimer.reset();

        if (event.getStage() == EventStageable.EventStage.PRE) {
            if (Harakiri.get().getModuleManager().find(NukerModule.class).isEnabled() && ((NukerModule)Harakiri.get().getModuleManager().find(NukerModule.class)).highwayMode.getValue()) {
                if (Minecraft.getMinecraft().player.dimension != -1) {

                    if (queueTimer.passed(30000)) {
                        // 30s in queue? reconnect legitly.
                        toDisconnect = true;
                    }

                    return;
                } else {
                    queueTimer.reset();
                }
            }
                
            
            if (Minecraft.getMinecraft().player.getHealth() <= this.health.getValue()) {
                Minecraft.getMinecraft().player.connection.sendPacket(new CPacketHeldItemChange(420));

                new Thread(() -> {
                    JOptionPane.showConfirmDialog(null, "Harakiri: Disconnect on low health");
                }).start();

                ReconnectModule rcm = (ReconnectModule)Harakiri.get().getModuleManager().find(ReconnectModule.class);
                if (rcm.isEnabled())
                    rcm.auto.setValue(false);

                
                this.toggle();
            }
        }
    }

}
