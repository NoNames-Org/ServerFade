package mc.net.fell.serverfade.paper;

import java.util.Locale;
import mc.net.fell.serverfade.api.ScreenFade;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public final class ServerFadePlugin extends JavaPlugin {

    private FadeService service;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        FadeSettings settings = FadeSettings.load(getConfig());
        service = new FadeService(this, settings);
        getServer().getPluginManager().registerEvents(service, this);
        getServer().getServicesManager().register(ScreenFade.class, service, this, ServicePriority.Normal);
        PluginCommand command = getCommand("fade");
        if (command != null) {
            command.setExecutor((sender, cmd, label, args) -> {
                execute(sender, args);
                return true;
            });
        }
        getLogger().info("Затемнение " + settings.fadeInTicks() + " тиков, проявление " + settings.revealTicks()
                + " тиков");
    }

    @Override
    public void onDisable() {
        getServer().getServicesManager().unregisterAll(this);
        if (service != null) {
            service.close();
            service = null;
        }
    }

    private void execute(CommandSender sender, String[] args) {
        String action = args.length == 1 ? args[0].toLowerCase(Locale.ROOT) : "";
        switch (action) {
            case "reload" -> {
                reloadConfig();
                service.settings(FadeSettings.load(getConfig()));
                sender.sendMessage(Component.text("ServerFade: config.yml перечитан", NamedTextColor.GREEN));
            }
            case "test" -> {
                if (sender instanceof Player player) {
                    service.test(player);
                } else {
                    sender.sendMessage(Component.text("Только для игрока", NamedTextColor.RED));
                }
            }
            default -> sender.sendMessage(Component.text("/fade test | reload", NamedTextColor.YELLOW));
        }
    }
}
