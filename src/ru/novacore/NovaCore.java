package ru.novacore;

import com.jagrosh.discordipc.IPCClient;
import com.jagrosh.discordipc.IPCListener;
import com.jagrosh.discordipc.entities.Packet;
import com.jagrosh.discordipc.entities.RichPresence;
import com.jagrosh.discordipc.entities.pipe.PipeStatus;
import com.jagrosh.discordipc.exceptions.NoDiscordClientException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import ru.novacore.command.*;
import ru.novacore.command.friends.FriendStorage;
import ru.novacore.command.impl.*;
import ru.novacore.command.impl.feature.*;
import ru.novacore.command.staffs.StaffStorage;
import ru.novacore.config.ConfigStorage;
import ru.novacore.config.LastAccountConfig;
import ru.novacore.events.EventSystem;
import ru.novacore.events.input.EventKey;
import ru.novacore.functions.api.Function;
import ru.novacore.functions.api.FunctionRegistry;
import ru.novacore.ui.altmanager.AltConfig;
import ru.novacore.ui.altmanager.AltManager;
import ru.novacore.ui.clickgui.Panel;
import ru.novacore.ui.notify.NotificationManager;
import ru.novacore.ui.styles.Style;
import ru.novacore.ui.styles.StyleFactory;
import ru.novacore.ui.styles.StyleFactoryImpl;
import ru.novacore.ui.styles.StyleManager;
import ru.novacore.utils.TPSCalc;
import ru.novacore.utils.client.ServerTPS;
import ru.novacore.utils.drag.DragManager;
import ru.novacore.utils.drag.Dragging;
import ru.novacore.utils.font.FontManager;
import ru.novacore.utils.render.ShaderUtils;
import via.ViaMCP;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NovaCore {

    public static UserData userData = new UserData("hogrider", 1);
    public boolean playerOnServer = false;
    public static final String CLIENT_NAME = "NovaCore Moon | ";
    public static final String BUILD_DATE = "#21022025";


    // Экземпляр Novacore
    @Getter
    private static NovaCore instance;

    // Менеджеры
    private FunctionRegistry functionRegistry;
    private ConfigStorage configStorage;
    private CommandDispatcher commandDispatcher;
    private ServerTPS serverTPS;
    private MacroManager macroManager;
    private StyleManager styleManager;
    private AltManager account;
    private LastAccountConfig lastAccountConfig;
    private NotificationManager notificationManager;
    private AltConfig altConfig;
    // Менеджер событий
    private final EventSystem eventSystem = new EventSystem();

    // Директории
    private final File clientDir = new File(Minecraft.getInstance().gameDir + "\\novacore");
    private final File filesDir = new File(Minecraft.getInstance().gameDir + "\\novacore\\files");

    private Panel dropDown;

    // Конфигурация и обработчики
    private ViaMCP viaMCP;
    private TPSCalc tpsCalc;

    public NovaCore() {
        instance = this;

        if (!clientDir.exists()) {
            clientDir.mkdirs();
        }
        if (!filesDir.exists()) {
            filesDir.mkdirs();
        }

        clientLoad();
        FriendStorage.load();
        StaffStorage.load();
        startRPC();
    }


    final IPCClient client = new IPCClient(1337459906608889979L);

    public void startRPC() {

        client.setListener(new IPCListener() {
            @Override
            public void onPacketReceived(IPCClient client, Packet packet) {
                IPCListener.super.onPacketReceived(client, packet);
            }

            @Override
            public void onReady(IPCClient client) {
                RichPresence.Builder builder = new RichPresence.Builder();
                builder.setDetails("ROLE: User").setStartTimestamp(OffsetDateTime.now()).setLargeImage("avatar", "always on top");
                client.sendRichPresence(builder.build());
            }
        });
        try {
            client.connect();
        } catch (NoDiscordClientException e) {
            System.out.println("DiscordRPC: " + e.getMessage());
        }
    }

    public void stopRPC() {
        if (client.getStatus() == PipeStatus.CONNECTED) client.close();
    }

    public Dragging createDrag(Function module, String name, float x, float y) {
        DragManager.draggables.put(name, new Dragging(module, name, x, y));
        return DragManager.draggables.get(name);
    }

    private void clientLoad() {
        viaMCP = new ViaMCP();
        serverTPS = new ServerTPS();
        functionRegistry = new FunctionRegistry();
        macroManager = new MacroManager();
        configStorage = new ConfigStorage();
        lastAccountConfig = new LastAccountConfig();
        altConfig = new AltConfig();
        notificationManager = new NotificationManager();
        functionRegistry.init();
        ShaderUtils.init();
        initCommands();
        initStyles();
        tpsCalc = new TPSCalc();
        account = new AltManager();
        FontManager.init();
        try {
            lastAccountConfig.init();
        } catch (IOException e) {
            System.out.println("Ошибка при подгрузке последнего аккаунт.");
        }

        try {
            altConfig.init();
        } catch (IOException e) {
            System.out.println("Ошибка при подгрузке списка аккаунтов.");
        }

        try {
            configStorage.init();
        } catch (IOException e) {
            System.out.println("Ошибка при подгрузке конфига.");
        }
        try {
            macroManager.init();
        } catch (IOException e) {
            System.out.println("Ошибка при подгрузке конфига макросов.");
        }
        DragManager.load();
        dropDown = new Panel();

        EventSystem.register(this);
    }

    private final EventKey eventKey = new EventKey(-1);

    public void onKeyPressed(int key) {
        if (functionRegistry.getSelfDestruct().unhooked) return;
        eventKey.setKey(key);

        EventSystem.call(eventKey);

        macroManager.onKeyPressed(key);

        if (key == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            Minecraft.getInstance().displayGuiScreen(dropDown);
        }
    }

    public void shutDown() {
        System.out.println("bye bye, see you later!");
        configStorage.saveConfiguration("autocfg");
        lastAccountConfig.updateFile();
        DragManager.save();
    }

    private void initCommands() {
        Minecraft mc = Minecraft.getInstance();
        Logger logger = new MultiLogger(List.of(new ConsoleLogger(), new MinecraftLogger()));
        List<Command> commands = new ArrayList<>();
        Prefix prefix = new PrefixImpl();
        commands.add(new ListCommand(commands, logger));
        commands.add(new FriendCommand(prefix, logger, mc));
        commands.add(new BindCommand(prefix, logger));
        commands.add(new GPSCommand(prefix, logger));
        commands.add(new ConfigCommand(configStorage, prefix, logger));
        commands.add(new MacroCommand(macroManager, prefix, logger));
        commands.add(new VClipCommand(prefix, logger, mc));
        commands.add(new HClipCommand(prefix, logger, mc));
        commands.add(new StaffCommand(prefix, logger));
        commands.add(new MemoryCommand(logger));
        commands.add(new RCTCommand(logger, mc));

        AdviceCommandFactory adviceCommandFactory = new AdviceCommandFactoryImpl(logger);
        ParametersFactory parametersFactory = new ParametersFactoryImpl();

        commandDispatcher = new StandaloneCommandDispatcher(commands, adviceCommandFactory, prefix, parametersFactory, logger);
    }

    private void initStyles() {
        StyleFactory styleFactory = new StyleFactoryImpl();
        List<Style> styles = new ArrayList<>();

        styles.add(styleFactory.createStyle("Новогодний", new Color(239, 59, 54), new Color(255, 255, 255)));
        styles.add(styleFactory.createStyle("Космический", new Color(0, 97, 255), new Color(96, 239, 255)));
        styles.add(styleFactory.createStyle("Прикольный", new Color(89, 92, 255), new Color(198, 248, 255)));
        styles.add(styleFactory.createStyle("Приятный", new Color(64, 201, 255), new Color(232, 28, 255)));
        styles.add(styleFactory.createStyle("Безупречный", new Color(237, 227, 66), new Color(255, 81, 235)));
        styles.add(styleFactory.createStyle("Малиновый", new Color(245, 230, 173), new Color(241, 60, 119)));
        styles.add(styleFactory.createStyle("Огненный", new Color(233, 208, 34), new Color(230, 11, 9)));
        styles.add(styleFactory.createStyle("Кровавый", new Color(235, 87, 87), new Color(0, 0, 0)));

        styleManager = new StyleManager(styles, styles.get(0));
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class UserData {
        final String user;
        final int uid;
    }

}
