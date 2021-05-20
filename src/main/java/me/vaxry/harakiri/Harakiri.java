package me.vaxry.harakiri;

import io.github.vialdevelopment.attendance.manager.impl.ParentEventManager;
import me.vaxry.harakiri.framework.Module;
import me.vaxry.harakiri.framework.extd.FontRendererExtd;
import me.vaxry.harakiri.framework.extd.RenderItemAlpha;
import me.vaxry.harakiri.framework.harakiriFormatter;
import me.vaxry.harakiri.framework.util.TTFFontUtil;
import me.vaxry.harakiri.impl.gui.hud.GuiHudEditor;
import me.vaxry.harakiri.impl.gui.hud.component.PlexusComponent;
import me.vaxry.harakiri.impl.gui.hud.component.effect.PlexusEffect;
import me.vaxry.harakiri.impl.gui.menu.HaraMainMenu;
import me.vaxry.harakiri.impl.manager.*;
import me.vaxry.harakiri.impl.module.config.ReloadConfigsModule;
import me.vaxry.harakiri.impl.module.render.CustomFontModule;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

import javax.swing.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.ConsoleHandler;
import java.util.logging.Logger;

/**
 * @author vax
 * @author Zim
 */
public final class Harakiri {

    private boolean isTTF = true;
    public static final Harakiri INSTANCE = new Harakiri();
    private static String OS = System.getProperty("os.name").toLowerCase();
    private String username = "";
    private String loggedAccount = "";

    private Logger logger;

    //private String prevTitle;

    private ParentEventManager eventManager = new ParentEventManager();

    private APIManager apiManager;

    private ModuleManager moduleManager;

    private CommandManager commandManager;

    private FriendManager friendManager;

    private ConfigManager configManager;

    private RotationManager rotationManager;

    private MacroManager macroManager;

    private TickRateManager tickRateManager;

    private ChatManager chatManager;

    private WorldManager worldManager;

    private PositionManager positionManager;

    private JoinLeaveManager joinLeaveManager;

    private HudManager hudManager;

    private AnimationManager animationManager;

    private NotificationManager notificationManager;

    private GuiHudEditor hudEditor;

    private CameraManager cameraManager;

    private PlexusEffect plexusEffect = null;

    private DiscordManager discordManager;

    private FontRendererExtd fontRendererExtd;

    private TTFFontUtil fontUtil;

    private HaraMainMenu haraMainMenu;

    private RenderItemAlpha renderItemAlpha;


    public void init() {
        try {
            this.fontUtil = new TTFFontUtil("gravity", 18);

            this.eventManager = new ParentEventManager();
            this.apiManager = new APIManager();
            this.configManager = new ConfigManager();
            this.friendManager = new FriendManager();
            this.rotationManager = new RotationManager();
            this.macroManager = new MacroManager();
            this.tickRateManager = new TickRateManager();
            this.chatManager = new ChatManager();
            this.worldManager = new WorldManager();
            this.positionManager = new PositionManager();
            this.joinLeaveManager = new JoinLeaveManager();
            this.animationManager = new AnimationManager();
            this.notificationManager = new NotificationManager();
            this.moduleManager = new ModuleManager();
            this.commandManager = new CommandManager();
            this.cameraManager = new CameraManager();
            this.hudManager = new HudManager();
            this.hudEditor = new GuiHudEditor();
            this.discordManager = new DiscordManager();
            this.haraMainMenu = new HaraMainMenu();
            this.eventManager.build();

            //this.plexusEffect = new PlexusEffect(); -- inits in GuiHudEditor

            for(Module mod : this.moduleManager.getModuleList()){
                mod.onFullLoad();
            }
            this.eventManager.registerAttender(this.configManager);
            Harakiri.get().getEventManager().build();
            this.eventManager.setAttending(this.configManager, true);



            this.username = "Final";

            try {
                this.moduleManager.add(new ReloadConfigsModule(this.moduleManager)); // Load cfgs
                this.configManager.init(); // Keep last, so we load configs after everything else inits
            }catch(Exception t){
                StringWriter errors = new StringWriter();
                t.printStackTrace(new PrintWriter(errors));
                //Harakiri.get().logChat("StorageESP Threw an Error: " + errors.toString());
                JOptionPane.showMessageDialog(null, errors.toString(), "Init failed", JOptionPane.INFORMATION_MESSAGE);
            }

//            EventLoad loadEvent = new EventLoad();
//            this.getEventManager().dispatch(loadEvent);

            // Create the font renderer
            fontRendererExtd = new FontRendererExtd(Minecraft.getMinecraft().gameSettings, new ResourceLocation("harakirimod", "textures/ascii.png"), Minecraft.getMinecraft().renderEngine, true);

            // Add runtime hook to listen for shutdown to save configs
            Runtime.getRuntime().addShutdownHook(new Thread("Harakiri SH") {
                @Override
                public void run() {
                    getConfigManager().saveAll();
                }
            });
        }catch(Throwable t){
            StringWriter errors = new StringWriter();
            t.printStackTrace(new PrintWriter(errors));
            //Harakiri.get().logChat("StorageESP Threw an Error: " + errors.toString());
            JOptionPane.showMessageDialog(null, errors.toString(), "Error in init()!", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public void errorChat(String message) {
        Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new TextComponentString("\2475[\2477\247lHarakiri\247r\2475]\247r\247c " + message));
    }

    public void errorfChat(String format, Object... objects) {
        errorChat(String.format(format, objects));
    }

    public void logChat(String message) {
        Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new TextComponentString("\2475[\2477\247lHarakiri\247r\2475]\247r\247f " + message));
    }

    public void logcChat(ITextComponent textComponent) {
        Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new TextComponentString("\2475[\2477\247lHarakiri\247r\2475]\247r\247f ").appendSibling(textComponent));
    }

    public void logChatRaw(String s) {
        Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new TextComponentString(s));
    }

    public void logfChat(String format, Object... objects) {
        logChat(String.format(format, objects));
    }

    public void unload() {
        this.moduleManager.unload();
        this.apiManager.unload();
        this.commandManager.unload();
        this.friendManager.unload();
        this.macroManager.unload();
        this.tickRateManager.unload();
        this.chatManager.unload();
        this.joinLeaveManager.unload();
        this.hudManager.unload();
        this.animationManager.unload();
        this.notificationManager.unload();
        this.hudEditor.unload();
        this.cameraManager.unload();

        ModContainer harakiriModContainer = null;

        for (ModContainer modContainer : Loader.instance().getActiveModList()) {
            if (modContainer.getModId().equals("harakirimod")) {
                harakiriModContainer = modContainer;
            }
        }

        if (harakiriModContainer != null) {
            Loader.instance().getActiveModList().remove(harakiriModContainer);
        }

        //Display.setTitle(this.prevTitle);
        Minecraft.getMinecraft().ingameGUI.getChatGUI().clearChatMessages(true);
        System.gc();
    }

    public String getUsername(){
        return this.username;
    }

    public void setUsername(String u){
        this.username = u;
    }

    public String getLoggedAccount(){
        return this.loggedAccount;
    }

    public void setLoggedAccount(String l){
        this.loggedAccount = l;
    }

    /**
     * Setup a logger and set the format
     */
    private void initLogger() {
        this.logger = Logger.getLogger(Harakiri.class.getName());
        logger.setUseParentHandlers(false);
        final ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new harakiriFormatter());
        logger.addHandler(handler);
    }

    public Logger getLogger() {
        if (this.logger == null) {
            this.initLogger();
        }

        return this.logger;
    }

    public ParentEventManager getEventManager() {
        if (this.eventManager == null) {
            this.eventManager = new ParentEventManager();
        }

        return this.eventManager;
    }

    public APIManager getApiManager() {
        if (this.apiManager == null) {
            this.apiManager = new APIManager();
        }
        return this.apiManager;
    }

    public ModuleManager getModuleManager() {
        if (this.moduleManager == null) {
            this.moduleManager = new ModuleManager();
        }
        return this.moduleManager;
    }

    public CommandManager getCommandManager() {
        if (this.commandManager == null) {
            this.commandManager = new CommandManager();
        }
        return this.commandManager;
    }

    public FriendManager getFriendManager() {
        if (this.friendManager == null) {
            this.friendManager = new FriendManager();
        }
        return this.friendManager;
    }

    public ConfigManager getConfigManager() {
        if (this.configManager == null) {
            this.configManager = new ConfigManager();
        }
        return this.configManager;
    }

    public RotationManager getRotationManager() {
        if (this.rotationManager == null) {
            this.rotationManager = new RotationManager();
        }
        return this.rotationManager;
    }

    public MacroManager getMacroManager() {
        if (this.macroManager == null) {
            this.macroManager = new MacroManager();
        }
        return this.macroManager;
    }

    public TickRateManager getTickRateManager() {
        if (this.tickRateManager == null) {
            this.tickRateManager = new TickRateManager();
        }
        return this.tickRateManager;
    }

    public ChatManager getChatManager() {
        if (this.chatManager == null) {
            this.chatManager = new ChatManager();
        }
        return this.chatManager;
    }

    public WorldManager getWorldManager() {
        if (this.worldManager == null) {
            this.worldManager = new WorldManager();
        }
        return this.worldManager;
    }

    public PositionManager getPositionManager() {
        if (this.positionManager == null) {
            this.positionManager = new PositionManager();
        }
        return this.positionManager;
    }

    public JoinLeaveManager getJoinLeaveManager() {
        if (this.joinLeaveManager == null) {
            this.joinLeaveManager = new JoinLeaveManager();
        }
        return this.joinLeaveManager;
    }

    public HudManager getHudManager() {
        if (this.hudManager == null) {
            this.hudManager = new HudManager();
        }
        return this.hudManager;
    }

    public AnimationManager getAnimationManager() {
        if (this.animationManager == null) {
            this.animationManager = new AnimationManager();
        }
        return this.animationManager;
    }

    public NotificationManager getNotificationManager() {
        if (this.notificationManager == null) {
            this.notificationManager = new NotificationManager();
        }
        return this.notificationManager;
    }

    public GuiHudEditor getHudEditor() {
        if (this.hudEditor == null) {
            this.hudEditor = new GuiHudEditor();
        }
        return this.hudEditor;
    }

    public CameraManager getCameraManager() {
        if (this.cameraManager == null) {
            this.cameraManager = new CameraManager();
        }
        return this.cameraManager;
    }

    public DiscordManager getDiscordManager() {
        if (this.discordManager == null) {
            this.discordManager = new DiscordManager();
        }
        return this.discordManager;
    }

    public FontRendererExtd getFontRendererExtd(){
        if(this.fontRendererExtd == null){
            this.fontRendererExtd = new FontRendererExtd(Minecraft.getMinecraft().gameSettings, new ResourceLocation("harakirimod", "textures/ascii.png"), Minecraft.getMinecraft().renderEngine, true);
        }
        return this.fontRendererExtd;
    }

    public TTFFontUtil getTTFFontUtil(){
        if(this.fontUtil == null){
            this.fontUtil = new TTFFontUtil("gravity", 20);
        }
        return this.fontUtil;
    }

    public RenderItemAlpha getRenderItemAlpha(){
        if(this.renderItemAlpha == null){
            this.renderItemAlpha = new RenderItemAlpha(Minecraft.getMinecraft().getTextureManager(), Minecraft.getMinecraft().modelManager, Minecraft.getMinecraft().getItemColors());
        }
        return this.renderItemAlpha;
    }

    public boolean isTTF(){
        CustomFontModule customFontModule = (CustomFontModule)Harakiri.get().getModuleManager().find(CustomFontModule.class);
        if(customFontModule == null)
            return false;
        return customFontModule.isEnabled();
    }

    public PlexusEffect getPlexusEffect() {
        return this.plexusEffect;
    }

    public void initPlexusEffect(PlexusComponent plx) {
        this.plexusEffect = new PlexusEffect(plx);
    }

    public static Harakiri get(){
        return INSTANCE;
    }


    public static final ResourceLocation LIGHTNING_TEXTURE = new ResourceLocation("textures/entity/creeper/creeper_armor.png");
    public static final ResourceLocation ENCHANTED_ITEM_GLINT_RES = new ResourceLocation("textures/misc/enchanted_item_glint.png");

    public static boolean isNix(){
        return OS.contains("nix") || OS.contains("nux") || OS.contains("aix") || OS.contains("darwin");
    }
}
