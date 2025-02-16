package ru.novacore.ui.schedules.rw.impl;

import ru.novacore.ui.schedules.rw.Schedule;
import ru.novacore.ui.schedules.rw.TimeType;

public class MascotSchedule
        extends Schedule {
    @Override
    public String getName() {
        return "Талисман";
    }

    @Override
    public TimeType[] getTimes() {
        return new TimeType[]{TimeType.NINETEEN_HALF};
    }
}
