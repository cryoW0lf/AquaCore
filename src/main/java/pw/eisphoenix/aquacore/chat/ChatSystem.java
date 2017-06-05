package pw.eisphoenix.aquacore.chat;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import pw.eisphoenix.aquacore.dependency.DependencyInjector;
import pw.eisphoenix.aquacore.dependency.Inject;
import pw.eisphoenix.aquacore.dependency.InjectionHook;
import pw.eisphoenix.aquacore.service.MessageService;
import pw.eisphoenix.aquacore.service.MuteService;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Year: 2017
 *
 * @author Eisphoenix
 */
public final class ChatSystem implements Listener, InjectionHook {
    @Inject
    private MuteService muteService;
    @Inject
    private MessageService messageService;
    private DateFormat dateFormat;

    public ChatSystem() {
        DependencyInjector.inject(this);
    }

    @EventHandler
    public final void onChat(final AsyncPlayerChatEvent event) {
        final boolean result = muteService.isMuted(event.getPlayer().getUniqueId()).join();
        if (!result) {
            return;
        }
        event.setCancelled(true);
        final Mute mute = muteService.getCurrentMute(event.getPlayer().getUniqueId()).join();
        messageService.getMessage("mute.message", MessageService.MessageType.WARNING).thenApply(
                message -> message.replaceAll("%TIME%", mute.getLength() <= 0 ?
                        messageService.getMessage("mute.permanent").join() :
                        messageService.getMessage("mute.date").join()
                                .replaceAll("%DATE%",
                                        dateFormat.format(new Date(mute.getTimestamp() + mute.getLength()))
                                )
                ).replaceAll("%REASON%", mute.getReason())
        ).thenAccept(event.getPlayer()::sendMessage);

    }


    @Override
    public final void postInjection() {
        messageService.getMessage("ban.format").thenAccept(message -> dateFormat = new SimpleDateFormat(message));
    }
}
