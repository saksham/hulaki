package com.dumbster.smtp;

public interface Observable<T> {
    void addObserver(Observer<T> observer);
    void removeObserver(Observer<T> observer);
}