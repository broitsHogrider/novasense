package ru.novacore.command;

import java.util.List;

public interface MultiNamedCommand {
    List<String> aliases();
}
