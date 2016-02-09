package com.github.saksham.hulaki.transport;

public interface Observer<T> {
    public void notify(T data);
}