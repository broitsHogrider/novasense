package ru.novacore.command.impl;

import ru.novacore.command.Parameters;
import ru.novacore.command.ParametersFactory;

public class ParametersFactoryImpl implements ParametersFactory {

    @Override
    public Parameters createParameters(String message, String delimiter) {
        return new ParametersImpl(message.split(delimiter));
    }
}
