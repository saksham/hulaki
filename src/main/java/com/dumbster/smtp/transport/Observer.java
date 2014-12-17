package com.dumbster.smtp.transport;

public interface Observer<T> {
    public void notify(T data);
}