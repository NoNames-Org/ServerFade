package mc.net.fell.serverfade;

import java.util.Locale;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

/** {@code /serverfade test} — затемнение без перехода, {@code /serverfade reload} — перечитать config.yml. */
public final class ServerFadeCommand extends Command {

    private final ServerFadePlugin plugin;
    private final FadeListener listener;

    ServerFadeCommand(ServerFadePlugin plugin, FadeListener listener) {
        super("serverfade", "serverfade.admin");
        this.plugin = plugin;
        this.listener = listener;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        String action = args.length == 1 ? args[0].toLowerCase(Locale.ROOT) : "";
        switch (action) {
            case "reload" -> {
                listener.settings(plugin.loadSettings());
                sender.sendMessage(new TextComponent(ChatColor.GREEN + "ServerFade: config.yml перечитан"));
            }
            case "test" -> {
                if (sender instanceof ProxiedPlayer player) {
                    listener.test(player);
                } else {
                    sender.sendMessage(new TextComponent(ChatColor.RED + "Только для игрока"));
                }
            }
            default -> sender.sendMessage(new TextComponent(ChatColor.YELLOW + "/serverfade test | reload"));
        }
    }
}
