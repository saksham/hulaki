package com.dumbster.smtp;

public interface Observer<T> {
    public void notify(T data);
}