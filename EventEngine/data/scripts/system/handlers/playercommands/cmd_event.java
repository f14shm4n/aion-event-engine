package playercommands;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.PlayerCommand;
import pirate.events.EventManager;
import pirate.events.EventRegisterInfo;
import pirate.events.enums.EventType;

/**
 *
 * @author f14shm4n
 */
public class cmd_event extends PlayerCommand {

    private static final StringBuilder info = new StringBuilder();

    static {
        info.append("Информация по команде:\n");
        info.append(".event - выводит информацию которую вы сейчас читаете\n");
        info.append(".event reg <event type> - регистрация на указанный вами ивент\n");
        info.append(".event unreg <event type> - выход из списка регистрации на указанный вами ивент\n");
        info.append("Доступные типы ивентов(event type):\n");
        for (EventType et : EventType.values()) {
            if (et.IsDone()) {
                info.append("- ").append(et.getEventTemplate().getCmdName()).append("\n");
            }
        }
    }

    public cmd_event() {
        super("event");
    }

    @Override
    public void execute(Player player, String... params) {
        if ((player.getController().isInCombat() || player.isInInstance() || player.isInPrison()) && !player.isGM()) {
            PacketSendUtility.sendMessage(player, "Нельзя участвовать в ивенте, пока вы в тюрьме, во временной зоне либо в состоянии боя.");
            return;
        }
        if (params.length == 2) {
            EventType et = parseType(params[1]);
            if (params[0].equals("reg")) {
                if (et == null) {
                    ShowSyntax(player);
                    return;
                }
                EventRegisterInfo eri = EventManager.getInstance().register(player, et);
                PacketSendUtility.sendMessage(player, eri.getMessage());
            } else if (params[0].equals("unreg")) {
                if (et == null) {
                    ShowSyntax(player);
                    return;
                }
                EventRegisterInfo eri = EventManager.getInstance().unregister(player, et);
                PacketSendUtility.sendMessage(player, eri.getMessage());
            }
        } else {
            this.ShowSyntax(player);
        }
    }

    private void ShowSyntax(Player p) {
        PacketSendUtility.sendMessage(p, info.toString());
    }

    private EventType parseType(String str) {
        for (EventType et : EventType.values()) {
            if (!et.IsDone()) {
                continue;
            }
            if (str.equals(et.getEventTemplate().getCmdName())) {
                return et;
            }
        }
        return null;
    }

    @Override
    public void onFail(Player player, String message) {
        // TODO Auto-generated method stub
    }
}