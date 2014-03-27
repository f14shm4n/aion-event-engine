package com.aionemu.gameserver.instance.handlers;

import pirate.events.enums.EventType;

/**
 *
 * @author f14shm4n
 */
public class GeneralEventHandler extends GeneralInstanceHandler {

    protected EventType eType = EventType.E_DEFAULT;

    public final EventType getEventType() {
        return eType;
    }

    public final void setEventType(EventType et) {
        if (eType == EventType.E_DEFAULT && et != EventType.E_DEFAULT) {
            eType = et;
        }
    }
}
