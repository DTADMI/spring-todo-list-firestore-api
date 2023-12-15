package ca.dtadmi.todolist.dao;

import java.util.Collection;
import java.util.Optional;

public interface Dao<T> {

    Optional<T> get(String id);
    Collection<T> getAll();
    T save(T t);
    Optional<T> update(T t);
    void delete(String id);
}