package me.vaxry.harakiri.impl.manager;

import me.vaxry.harakiri.framework.Module;
import me.vaxry.harakiri.framework.util.StringUtil;
import me.vaxry.harakiri.framework.Value;
import me.vaxry.harakiri.impl.module.combat.*;
import me.vaxry.harakiri.impl.module.hidden.*;
import me.vaxry.harakiri.impl.module.lua.ReloadLuasModule;
import me.vaxry.harakiri.impl.module.misc.*;
import me.vaxry.harakiri.impl.module.movement.*;
import me.vaxry.harakiri.impl.module.player.*;
import me.vaxry.harakiri.impl.module.render.*;
import me.vaxry.harakiri.impl.module.ui.HudEditorModule;
import me.vaxry.harakiri.impl.module.ui.WatermarkModule;
import me.vaxry.harakiri.impl.module.world.*;
import me.vaxry.harakiri.impl.module.world.ScaffoldModule;
import net.minecraftforge.common.MinecraftForge;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class ModuleManager {

    private List<Module> moduleList = new ArrayList<Module>();

    public ModuleManager() {

        add(new CustomFontModule()); // Load first

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
        add(new RespawnModule());
        add(new NoFallModule());
        add(new NoSwingModule());
        add(new HandOffsetModule());
        add(new GuiPlusModule());
        ESPModule espmod = new ESPModule();
        add(espmod);
        ChamsModule chammod = new ChamsModule();
        add(chammod);
        add(new NametagsModule());
        add(new MobOwnerModule());
        add(new SneakModule());
        add(new MiddleClickFriendsModule());
        add(new FullbrightModule());
        add(new HitmarkersModule());
        ReconnectModule recmod = new ReconnectModule();
        add(recmod);
        add(new AutoFishModule());
        add(new InteractModule());
        add(new TracersModule());
        add(new FastPlaceModule());
        add(new SpeedMineModule());
        add(new AutoToolModule());
        add(new RandomizerModule());
        add(new NoBreakAnimModule());
        add(new FreeCamModule());
        add(new EntityControlModule());
        add(new SafeWalkModule());
        add(new PhaseModule());
        add(new FlightModule());
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
        add(new AutoWalkModule());
        add(new AvoidModule());
        add(new MacroModule());
        add(new SpeedModule());
        add(new AntiHungerModule());
        add(new HorseJumpModule());
        add(new NewChunksModule());
        add(new NoCrystalModule());
        add(new StorageESPModule());
        //add(new SourceBlockESPModule()); //Replaced by a lua
        add(new BedrockFinder());
        add(new AutoDisconnectModule());
        add(new ChatFilterModule());
        add(new ProjectilesModule());
        add(new ScaffoldModule());
        add(new SkyModule());
        add(new LiquidInteractModule());
        add(new NoBreakCooldown());
        add(new NoAfkModule());
        add(new NoGlitchBlocks());
        add(new NukerModule());
        add(new SlimeChunksModule());
        add(new StepModule());
        add(new ViewClipModule());
        add(new NoGlobalSoundsModule());
        add(new NoBiomeColorModule());
        add(new BuildHeightModule());
        add(new BlockHighlightModule());
        add(new NoWeatherModule());
        add(new ObsidianReplaceModule());
        add(new HudEditorModule());
        add(new WatermarkModule());
        add(new StorageAlertModule());
        add(new StrafeModule());
        add(new NoBossHealthModule());
        add(new DiscordRPCModule());
        add(new ExtraTabModule());
        add(new HolesModule());
        add(new SmallShieldModule());
        add(new PullDownModule());
        add(new ShulkerPreviewModule());
        add(new LogoutSpotsModule());
        add(new ChatSuffixModule());
        add(new VisualRangeModule());
        add(new HotBarRefillModule());
        add(new TotemNotifierModule());
        add(new MiddleClickPearlModule());
        add(new NameAlertModule());
        add(new EntityDesyncModule());
        add(new NoPacketModule());
        add(new NoEffectsModule());
        add(new NoEntityTraceModule());
        add(new MultitaskModule());
        add(new SearchModule());
        add(new ReloadLuasModule());
        add(new AutoGappleModule());
        add(new AutoEatModule());
        add(new NoFriendHurtModule());
        add(new ReachModule());
        add(new StashLoggerModule());
        add(new PacketLogModule());
        add(new SelfWebModule());
        add(new BurrowModule());
        add(new HealthOverlayModule());
        add(new AnchorModule());
        HitsoundModule hitsoundModule = new HitsoundModule();
        add(hitsoundModule);

        MinecraftForge.EVENT_BUS.register(espmod);
        MinecraftForge.EVENT_BUS.register(recmod);
        MinecraftForge.EVENT_BUS.register(hitsoundModule);
        MinecraftForge.EVENT_BUS.register(chammod);

        //this.loadExternalModules();

        moduleList.sort(Comparator.comparing(Module::getDisplayName));
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
            if(alias == null || mod.getDisplayName() == null)
                continue;

            if (alias.equalsIgnoreCase(mod.getDisplayName())) {
                return mod;
            }

            if (mod.getAlias() != null && mod.getAlias().length > 0) {
                for (String s : mod.getAlias()) {
                    if(alias == null)
                        continue; // Fix when obfuscated
                    if (alias.equalsIgnoreCase(s)) {
                        return mod;
                    }
                }
            }
        }
        return null;
    }

    public Module findLua(String luaName) {
        for (Module mod : this.getModuleList()) {
            if(mod.luaName.equals(""))
                continue;
            if (luaName.equalsIgnoreCase(mod.luaName)) {
                return mod;
            }
        }
        return null;
    }

    public Module findLuaShort(String luaName) {
        for (Module mod : this.getModuleList()) {
            if(mod.luaName.equals("") || mod.getType() != Module.ModuleType.LUA /* Fix configs */)
                continue;
            if (luaName.equalsIgnoreCase(mod.luaName.substring(0, mod.luaName.indexOf(".lua")))) {
                return mod;
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

    public void removeLuaModule(String name){
        Module toRemove = null;
        for(Module mod : moduleList){
            if(mod.getType() != Module.ModuleType.LUA || mod.luaName.equals(""))
                continue;
            if(mod.luaName.equalsIgnoreCase(name)) {
                toRemove = mod;
                break;
            }
        }
        if(toRemove != null)
            moduleList.remove(toRemove);
    }

    public void removeConfigModule(Module mod){
        this.moduleList.remove(mod);
    }

    public Module getConfigFromName(String name){
        for (Module mod : this.getModuleList()) {
            if (mod.getDisplayName().equalsIgnoreCase(name) && mod.getType() == Module.ModuleType.CONFIG) {
                return mod;
            }
        }
        return null;
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
