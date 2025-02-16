package ru.novacore.command;

import ru.novacore.command.impl.DispatchResult;

public interface CommandDispatcher {
    DispatchResult dispatch(String command);
}
