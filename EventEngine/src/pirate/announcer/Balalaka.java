package pirate.announcer;

import com.aionemu.gameserver.model.ChatType;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.World;
import java.util.Iterator;

/**
 *
 * @author flashman
 */
public class Balalaka {

    /**
     * Посылает сообщение всем игрокам в мире.
     *
     * @param message
     */
    public static void sayInWorld(final String message) {
        Iterator<Player> iter = World.getInstance().getPlayersIterator();
        while (iter.hasNext()) {
            PacketSendUtility.sendYellowMessageOnCenter(iter.next(), message);
        }
    }

    /**
     * Посылает сообщение всем игрокам в указанной локации.
     *
     * @param worldId
     * @param message
     */
    public static void sayInWorld(final int worldId, final String message) {
        Iterator<Player> iter = World.getInstance().getPlayersIterator();
        while (iter.hasNext()) {
            Player p = iter.next();
            if (p.isOnline() && p.getWorldId() == worldId) {
                PacketSendUtility.sendYellowMessageOnCenter(p, message);
            }
        }
    }

    /**
     * Посылает сообщение всем игрокам в мире, с указаной задержкой.
     *
     * @param sender
     * @param msg
     * @param delay в секундах
     */
    public static void sayInWorldWithDelay(final String msg, int delay) {
        if (delay == 0) {
            sayInWorld(msg);
        } else {
            ThreadPoolManager.getInstance().schedule(new Runnable() {
                @Override
                public void run() {
                    sayInWorld(msg);
                }
            }, delay * 1000);
        }
    }

    /**
     * Посылает сообщение всем игрокам в мире, с указаной задержкой.
     *
     * @param sender
     * @param msg
     * @param delay в секундах
     */
    public static void sayInWorldWithDelay(final String msg, final int worldId, int delay) {
        if (delay == 0) {
            sayInWorld(worldId, msg);
        } else {
            ThreadPoolManager.getInstance().schedule(new Runnable() {
                @Override
                public void run() {
                    sayInWorld(worldId, msg);
                }
            }, delay * 1000);
        }
    }

    /**
     * Посылает сообщение всем игрокам в мире. [Sender]: Text
     *
     * @param sender
     * @param msg
     */
    public static void sayInWorldOrangeTextCenter(final String sender, final String msg) {
        Iterator<Player> iter = World.getInstance().getPlayersIterator();
        while (iter.hasNext()) {
            PacketSendUtility.sendMessage(iter.next(), sender, msg, ChatType.GROUP_LEADER);
        }
    }

    /**
     * Посылает сообщение всем игрокам в указанной локации. [Sender]: Text
     *
     * @param sender
     * @param msg
     */
    public static void sayInWorldOrangeTextCenter(final String sender, final String msg, final int worldId) {
        Iterator<Player> iter = World.getInstance().getPlayersIterator();
        while (iter.hasNext()) {
            Player p = iter.next();
            if (p.isOnline() && p.getWorldId() == worldId) {
                PacketSendUtility.sendMessage(p, sender, msg, ChatType.GROUP_LEADER);
            }
        }
    }

    /**
     * Посылает сообщение всем игрокам в мире, с указаной задержкой. [Sender]:
     * Text
     *
     * @param sender
     * @param msg
     * @param delay в секундах
     */
    public static void sayInWorldOrangeTextCenterWithDelay(final String sender, final String msg, int delay) {
        if (delay == 0) {
            sayInWorldOrangeTextCenter(sender, msg);
        } else {
            ThreadPoolManager.getInstance().schedule(new Runnable() {
                @Override
                public void run() {
                    sayInWorldOrangeTextCenter(sender, msg);
                }
            }, delay * 1000);
        }
    }

    /**
     * Посылает сообщение всем игрокам в мире, с указаной задержкой. [Sender]:
     * Text
     *
     * @param sender
     * @param msg
     * @param delay в секундах
     */
    public static void sayInWorldOrangeTextCenterWithDelay(final String sender, final String msg, final int worldId, int delay) {
        if (delay == 0) {
            sayInWorldOrangeTextCenter(sender, msg, worldId);
        } else {
            ThreadPoolManager.getInstance().schedule(new Runnable() {
                @Override
                public void run() {
                    sayInWorldOrangeTextCenter(sender, msg, worldId);
                }
            }, delay * 1000);
        }
    }
}
