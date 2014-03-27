package events;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.instance.handlers.EventID;
import com.aionemu.gameserver.model.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.stats.container.PlayerLifeStats;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.player.PlayerReviveService;
import com.aionemu.gameserver.services.teleport.TeleportService2;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldMapInstance;
import java.util.concurrent.ScheduledFuture;
import pirate.announcer.Balalaka;
import pirate.events.EventRewardHelper;
import pirate.events.EventScore;
import pirate.events.enums.EventType;
import pirate.events.xml.EventStartPosition;

/**
 * Ивент - Один на Один
 *
 * @author flashman
 */
@EventID(eventId = 1)
public class PvPEventHandler extends BaseEventHandler {

    private ScheduledFuture endRoundTask;
    private ScheduledFuture nextRoundTask;
    private boolean isDraw = false;
    // задержка перед началом нового раунда, после завершения предыдущего в секундах
    private int delayBeforStartNextRound = 2;

    @Override
    public void onInstanceCreate(WorldMapInstance instance) {
        round = 1;
        winNeeded = 3;
        waitingTime = 10;
        battle_time = 360;
        super.onInstanceCreate(instance);
//        не надо если спавн отключен при создании инста
//        for (Npc npc : this.instance.getNpcs()) {
//            npc.getController().onDelete();
//        }
    }

    @Override
    public void onInstanceDestroy() {
        if (this.endRoundTask != null) {
            this.endRoundTask.cancel(true);
            this.endRoundTask = null;
        }
        if (this.nextRoundTask != null) {
            this.nextRoundTask.cancel(true);
            this.nextRoundTask = null;
        }
        this.players = null;
        this.score = null;
    }

    @Override
    public void onEnterInstance(Player player) {
        super.onEnterInstance(player);
        if (instance.isRegistered(player.getObjectId()) && !this.containsPlayer(player.getObjectId())) {
            players.add(player);
        } else {
            return;
        }
        ThreadPoolManager.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                StartRoundTask();
            }
        }, waitingTime * 1000);
        AddProtection(player, waitingTime * 1000);
        this.HealPlayer(player);
        this.sendSpecMessage(EventManager, "До начала 1-го раунда: " + this.waitingTime + " сек.");
        if (!this.containsInScoreList(player.getObjectId())) {
            this.addToScoreList(player);
        }
    }

    @Override
    public boolean isEnemy(Player attacker, Player target) {
        if (attacker != target) {
            return true;
        }
        return super.isEnemy(attacker, target);
    }

    @Override
    public void onPlayerLogOut(Player player) {
        super.onPlayerLogOut(player);
        if (!eventIsComplete) {
            this.removeFromScoreList(player.getObjectId());
            this.ifOnePlayer();
        }
    }

    @Override
    public void onLeaveInstance(Player player) {
        super.onLeaveInstance(player);
        if (!eventIsComplete) {
            this.removeFromScoreList(player.getObjectId());
            this.ifOnePlayer();
        }
    }

    @Override
    public boolean onDie(Player player, Creature lastAttacker) {
        //super.onDie(player, lastAttacker);
        this.deathPlayer(player, lastAttacker);
        return true;
    }

    @Override
    public boolean onReviveEvent(Player player) {
        PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_REBIRTH_MASSAGE_ME);
        PlayerReviveService.revive(player, 100, 100, false, 0);
        player.getGameStats().updateStatsAndSpeedVisually();
        return true;
    }

    private synchronized void StartRoundTask() {
        if (endRoundTask == null) {
            for (Player p : this.players) {
                RemoveProtection(p);
                this.HealPlayer(p, false, true);
            }

            if (this.ifOnePlayer()) {
                return;
            }
            endRoundTask = ThreadPoolManager.getInstance().schedule(new Runnable() {
                @Override
                public void run() {
                    NextRound(true);
                }
            }, battle_time * 1000);
            sendSpecMessage(EventManager, "Раунд: " + round + " - поехали");
            this.startTimer(this.battle_time);
        }
    }

    private void NextRound(boolean timeIsUp) {
        if (players == null || players.isEmpty()) {
            return;
        }
        if (this.endRoundTask != null) {
            endRoundTask.cancel(true);
            endRoundTask = null;
        }
        round++;

        if (this.ifOnePlayer()) {
            return;
        }

        if (timeIsUp) {
            Player winner = this.timeIsUpEvent();
            if (winner != null) {
                this.getScore(winner.getObjectId()).incWin();
                if (hasWinner()) {
                    sendSpecMessage(EventManager, "Ивент завершен");
                    DoReward();
                    return;
                } else {
                    this.moveToStartPosition();
                    this.sendSpecMessage(EventManager, "Время раунда закончилось, по решению судей этот раунд за: " + winner.getName());
                }
            } else {
                this.DoReward();
                return;
            }
        }

        ThreadPoolManager.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                StartRoundTask();
            }
        }, this.delayBeforStartNextRound * 1000);
    }

    protected void deathPlayer(Player victim, Creature lastAttacker) {
        if (lastAttacker.getActingCreature() instanceof Player && victim != lastAttacker) {
            Player winner = (Player) lastAttacker.getActingCreature();
            EventScore winnerScore = this.getScore(winner.getObjectId());
            EventScore loserScore = this.getScore(victim.getObjectId());
            winnerScore.incKills();
            winnerScore.incWin();
            loserScore.incDeath();
            loserScore.incLose();

            PacketSendUtility.sendPacket(winner, new SM_SYSTEM_MESSAGE(1360001, victim.getName()));

            if (this.endRoundTask != null) {
                this.endRoundTask.cancel(true);
                this.endRoundTask = null;
            }

            this.HealPlayer(victim, false, true);
            this.HealPlayer(winner, false, true);
            winner.setTarget(null);
            victim.setTarget(null);

            moveToStartPosition();

            AddProtection(victim, waitingTime * 1000, 1000);
            AddProtection(winner, waitingTime * 1000, 1000);

            this.stopTimer();

            this.sendSpecMessage(EventManager, "Раунд: " + round + " завершен, победитель: " + winner.getName());

            ThreadPoolManager.getInstance().schedule(new Runnable() {
                @Override
                public void run() {
                    if (hasWinner()) {
                        sendSpecMessage(EventManager, "Ивент завершен");
                        DoReward();
                        return;
                    }

                    if (nextRoundTask != null) {
                        nextRoundTask.cancel(true);
                        nextRoundTask = null;
                    }
                    for (Player p : players) {
                        HealPlayer(p, false, true);
                    }
                    nextRoundTask = ThreadPoolManager.getInstance().schedule(new Runnable() {
                        @Override
                        public void run() {
                            NextRound(false);
                        }
                    }, 4000);
                }
            }, 5000);
        }
    }

    @Override
    protected void DoReward() {
        if (!eventIsComplete) {
            eventIsComplete = true;
            if (!isDraw) {
                int rank;
                Object[] names = {"", ""};
                for (final Player player : this.players) {
                    EventScore es = this.getScore(player.getObjectId());
                    if (es.isWinner) {
                        rank = 1;
                    } else {
                        rank = 2;
                    }
                    this.sendSpecMessage(EventManager, String.format("Вы заняли %s место", rank), player);
                    EventRewardHelper.GiveRewardFor(player, EventType.E_1x1, es, rank);
                    moveToEntry(player);
                    switch (rank) {
                        case 1:
                            names[0] = player.getName();
                            break;
                        case 2:
                            names[1] = player.getName();
                            break;
                    }
                    this.stopTimer(player);
                }
                Balalaka.sayInWorldOrangeTextCenter(EventManager, String.format("Ивент: %s завершен, победил(а): %s, проиграл(а): %s",
                        eType.getEventTemplate().getEventName(), names));
            } else {
                for (Player player : this.players) {
                    moveToEntry(player);
                    this.stopTimer(player);
                    // при ничьей выдается наград за второе место всем участникам
                    EventRewardHelper.GiveRewardFor(player, EventType.E_1x1, this.getScore(player.getObjectId()), 2);
                }
                Balalaka.sayInWorldOrangeTextCenter(EventManager, String.format("Ивент: %s завершен, ничья", eType.getEventTemplate().getEventName()));
            }

            this.players.clear();
            if (this.prestartTasks != null) {
                for (ScheduledFuture sf : this.prestartTasks.values()) {
                    sf.cancel(true);
                }
                this.prestartTasks.clear();
            }
            if (this.endRoundTask != null) {
                this.endRoundTask.cancel(true);
            }
            this.prestartTasks = null;
            this.endRoundTask = null;
        }
    }

    private void moveToStartPosition() {
        int i = 0;
        for (Player p : this.players) {
            EventStartPosition point = EventType.E_1x1.getEventTemplate().getStartPositionInfo().getPositions().get(i);
            TeleportService2.teleportTo(p, this.mapId, this.instanceId, point.getX(), point.getY(), point.getZ(), (byte) 0, TeleportAnimation.BEAM_ANIMATION);
            i += 1;
        }
    }

    private boolean hasWinner() {
        for (EventScore es : this.score) {
            if (es.getWins() >= winNeeded) {
                es.isWinner = true;
                return true;
            }
        }
        return false;
    }

    private Player timeIsUpEvent() {
        if (players.size() == 2) {
            Player winner;
            PlayerLifeStats pls1 = players.get(0).getLifeStats();
            PlayerLifeStats pls2 = players.get(1).getLifeStats();
            if (pls1.getCurrentHp() > pls2.getCurrentHp()) {
                winner = players.get(0);
            } else if (pls1.getCurrentHp() < pls2.getCurrentHp()) {
                winner = players.get(1);
            } else {
                if (pls1.getMaxHp() > pls2.getMaxHp()) {
                    winner = players.get(0);
                } else if (pls1.getMaxHp() < pls2.getMaxHp()) {
                    winner = players.get(1);
                } else {
                    winner = players.get(Rnd.get(0, players.size() - 1));
                }
            }
            return winner;
        }
        return null;
    }
}
