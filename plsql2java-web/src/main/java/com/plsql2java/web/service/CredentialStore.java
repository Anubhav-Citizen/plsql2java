package com.plsql2java.web.service;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stores JDBC passwords in memory as char[] for the duration of config resolution.
 * Passwords are cleared immediately after use. Never serialized or logged. (SECURITY-03/12)
 */
@Component
public class CredentialStore {

    private final ConcurrentHashMap<String, char[]> store = new ConcurrentHashMap<>();

    public void store(String configId, char[] password) {
        store.put(configId, password);
    }

    public char[] retrieve(String configId) {
        return store.get(configId);
    }

    /** Zeroes the password array and removes it from the store. */
    public void clear(String configId) {
        char[] password = store.remove(configId);
        if (password != null) {
            Arrays.fill(password, '\0');
        }
    }
}
