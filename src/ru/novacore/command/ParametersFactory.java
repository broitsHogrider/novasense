package ru.novacore.command;

public interface ParametersFactory {
    Parameters createParameters(String message, String delimiter);
}
