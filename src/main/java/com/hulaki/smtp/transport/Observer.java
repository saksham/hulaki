package com.hulaki.smtp.transport;

public interface Observer<T> {
    public void notify(T data);
}