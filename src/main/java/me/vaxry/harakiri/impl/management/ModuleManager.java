package me.vaxry.harakiri.impl.management;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.api.event.module.EventModuleLoad;
import me.vaxry.harakiri.api.module.Module;
import me.vaxry.harakiri.api.util.ReflectionUtil;
import me.vaxry.harakiri.api.util.StringUtil;
import me.vaxry.harakiri.api.value.Value;
import me.vaxry.harakiri.impl.module.combat.*;
import me.vaxry.harakiri.impl.module.hidden.*;
import me.vaxry.harakiri.impl.module.misc.*;
import me.vaxry.harakiri.impl.module.movement.*;
import me.vaxry.harakiri.impl.module.player.*;
import me.vaxry.harakiri.impl.module.render.*;
import me.vaxry.harakiri.impl.module.ui.HudEditorModule;
import me.vaxry.harakiri.impl.module.ui.WatermarkModule;
import me.vaxry.harakiri.impl.module.world.*;
import net.minecraftforge.common.MinecraftForge;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;

/**
 * Author Seth
 * 4/7/2019 @ 10:13 PM.
 */
public final class ModuleManager {

    private List<Module> moduleList = new ArrayList<Module>();

    public ModuleManager() {

        ESPModule espmod = new ESPModule();

        add(new KeybindsModule());
        add(new CommandsModule());
        add(new HudModule());
        add(new ArrayListModule());
        add(new ThreatCamModule());
        add(new PlexusModule());
        add(new NoOverlayModule());
        add(new OffscreenModule());
        add(new NoPushModule());
        add(new GodModeModule());
        add(new BlinkModule());
        add(new FakeLagModule());
        add(new XrayModule());
        add(new NoSlowDownModule());
        add(new NoHurtCamModule());
        add(new JesusModule());
        add(new ColoredBooksModule());
        add(new NoLagModule());
        add(new MoreInvModule());
        add(new GuiMoveModule());
        add(new NoHandShakeModule());
        add(new SprintModule());
        add(new ColoredSignsModule());
        add(new CoordLoggerModule());
        add(new VelocityModule());
        add(new PortalGuiModule());
        add(new NoRotateModule());
        add(new TimerModule());
        add(new ChatMutatorModule());
        add(new RespawnModule());
        add(new NoFallModule());
        add(new NoSwingModule());
        add(espmod);
        add(new NametagsModule());
        add(new SneakModule());
        add(new MiddleClickFriendsModule());
        add(new BrightnessModule());
        add(new ReconnectModule());
        add(new AutoFishModule());
        add(new InteractModule());
        add(new TracersModule());
        add(new ChamsModule());
        add(new FastPlaceModule());
        add(new SpeedMineModule());
        add(new AutoToolModule());
        add(new NoBreakAnimModule());
        add(new FreeCamModule());
        add(new EntityControlModule());
        add(new GreeterModule());
        add(new SafeWalkModule());
        add(new PhaseModule());
        add(new FlightModule());
        add(new NoHungerModule());
        add(new CrystalAuraModule());
        add(new AutoTotemModule());
        add(new FastBowModule());
        add(new BowBombModule());
        add(new KillAuraModule());
        add(new RegenModule());
        add(new AutoArmorModule());
        add(new CriticalsModule());
        add(new RotationLock());
        add(new ElytraFlyModule());
        add(new ItemSpoofModule());
        add(new AutoWalkModule());
        add(new LaggerModule());
        add(new MacroModule());
        add(new BreedModule());
        add(new ShearModule());
        add(new DyeModule());
        add(new WaypointsModule());
        add(new SpeedModule());
        add(new NoVoidModule());
        add(new HorseJumpModule());
        add(new TimeStampModule());
        add(new NewChunksModule());
        add(new NoCrystalModule());
        add(new StorageESPModule());
        add(new SourceBlockESPModule());
        add(new BedrockFinder());
        add(new AutoDisconnectModule());
        add(new ChatFilterModule());
        add(new NoChunkModule());
        add(new ProjectilesModule());
        add(new ScaffoldModule());
        add(new LiquidInteractModule());
        add(new NoAfkModule());
        add(new NoDesyncModule());
        add(new NukerModule());
        add(new SlimeChunksModule());
        add(new AutoSignModule());
        add(new IgnoreModule());
        add(new AutoIgnoreModule());
        add(new StepModule());
        add(new AutoCraftModule());
        add(new ViewClipModule());
        add(new NoGlobalSoundsModule());
        add(new NoBiomeColorModule());
        add(new VanillaTabModule());
        add(new PacketLoggerModule());
        add(new BuildHeightModule());
        add(new BlockHighlightModule());
        add(new NoWeatherModule());
        add(new ObsidianReplaceModule());
        add(new ChatTimeStampsModule());
        add(new HudEditorModule());
        add(new WatermarkModule());
        add(new StorageAlertModule());
        add(new StrafeModule());
        add(new MapBypassModule());
        add(new NoBossHealthModule());
        add(new DiscordBypassModule());
        add(new HolesModule());
        add(new SmallShieldModule());
        add(new PullDownModule());
        add(new PortalFinderModule());
        add(new ShulkerPreviewModule());
        add(new LogoutSpotsModule());
        add(new ChatSuffixModule());
        add(new VisualRangeModule());
        add(new HotBarRefillModule());
        add(new QuickCraftModule());
        add(new TotemNotifierModule());
        add(new MiddleClickPearlModule());
        add(new NameAlertModule());
        add(new CrosshairModule());
        add(new AutoMountModule());
        add(new EntityDesyncModule());
        add(new NoPacketModule());
        add(new BridgeModule());
        add(new AutoFarmModule());
        add(new NoEffectsModule());
        add(new NoEntityTraceModule());
        add(new MultitaskModule());
        add(new InfEnderChestModule());
        add(new SearchModule());
        add(new AutoGappleModule());
        add(new AutoEatModule());
        add(new NoFriendHurtModule());
        add(new DonkeyAlertModule());
        add(new ReachModule());
        add(new AutoWitherModule());
        add(new NoteBotModule());

        MinecraftForge.EVENT_BUS.register(espmod);

        // p2w experience
        if (Harakiri.INSTANCE.getCapeManager().hasCape())
            add(new CapeModule());

        this.loadExternalModules();

        moduleList.sort(Comparator.comparing(Module::getDisplayName));
    }

    /**
     * This is where we load custom external modules from disk
     * This allows users to create their own modules and load
     * them during runtime
     */
    public void loadExternalModules() {
        try {
            //create a directory at "harakiri/Modules"
            final File dir = new File("harakiri/Modules");

            //if it doesnt exist create it
            if (!dir.exists()) {
                dir.mkdirs();
            }

            //all jars/zip files in the dir
            //loop though all classes within the jar/zip
            for (Class clazz : ReflectionUtil.getClassesEx(dir.getPath())) {
                if (clazz != null) {
                    //if we have found a class and the class inherits "Module"
                    if (Module.class.isAssignableFrom(clazz)) {
                        //create a new instance of the class
                        final Module module = (Module) clazz.newInstance();

                        //add the class to our list of modules
                        add(module);

                        Harakiri.INSTANCE.getEventManager().dispatchEvent(new EventModuleLoad(module));
                        Harakiri.INSTANCE.getLogger().log(Level.INFO, "Found external module " + module.getDisplayName());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void unload() {
        for (Module mod : this.moduleList) {
            mod.onDisable();
            mod.unload();
        }
        this.moduleList.clear();
    }

    /**
     * Find all fields within the module that are values
     * and add them to the list of values inside of the module
     *
     * @param mod
     */
    public void add(Module mod) {
        try {
            for (Field field : mod.getClass().getDeclaredFields()) {
                if (Value.class.isAssignableFrom(field.getType())) {
                    if (!field.isAccessible()) {
                        field.setAccessible(true);
                    }
                    final Value val = (Value) field.get(mod);
                    mod.getValueList().add(val);
                }
            }
            this.moduleList.add(mod);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns a given module based on display name or alias
     *
     * @param alias
     * @return
     */
    public Module find(String alias) {
        for (Module mod : this.getModuleList()) {
            if (alias.equalsIgnoreCase(mod.getDisplayName())) {
                return mod;
            }

            if (mod.getAlias() != null && mod.getAlias().length > 0) {
                for (String s : mod.getAlias()) {
                    if (alias.equalsIgnoreCase(s)) {
                        return mod;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Returns a given module based on the class
     *
     * @param clazz
     * @return
     */
    public Module find(Class clazz) {
        for (Module mod : this.getModuleList()) {
            if (mod.getClass() == clazz) {
                return mod;
            }
        }
        return null;
    }

    /**
     * Returns the most similar module based on display name or alias
     *
     * @param input
     * @return
     */
    public Module findSimilar(String input) {
        Module mod = null;
        double similarity = 0.0f;

        for (Module m : this.getModuleList()) {
            final double currentSimilarity = StringUtil.levenshteinDistance(input, m.getDisplayName());

            if (currentSimilarity >= similarity) {
                similarity = currentSimilarity;
                mod = m;
            }
        }

        return mod;
    }

    public List<Module> getModuleList() {
        return moduleList;
    }

    public List<Module> getModuleList(Module.ModuleType type) {
        List<Module> list = new ArrayList<>();
        for (Module module : moduleList) {
            if (module.getType().equals(type)) {
                list.add(module);
            }
        }
        return list;
    }

    public void setModuleList(List<Module> moduleList) {
        this.moduleList = moduleList;
    }
}