package com.example.connectionchecker.commands;

public interface HttpCommand<C, R> {

    R execute(C context) throws Exception;
}
