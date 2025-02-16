package ru.novacore.command;

import ru.novacore.command.impl.AdviceCommand;

public interface AdviceCommandFactory {
    AdviceCommand adviceCommand(CommandProvider commandProvider);
}
