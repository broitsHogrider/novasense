package ru.novacore.command;

public interface CommandProvider {
    Command command(String alias);
}
