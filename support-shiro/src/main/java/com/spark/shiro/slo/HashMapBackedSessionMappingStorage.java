package com.spark.shiro.slo;


import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class HashMapBackedSessionMappingStorage implements SessionMappingStorage {
    private final Map<Serializable, Subject> MANAGED_SESSIONS = new HashMap();
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public HashMapBackedSessionMappingStorage() {
    }

    public synchronized void addSessionById(Serializable mappingId, Subject subject) {
        this.MANAGED_SESSIONS.put(mappingId, subject);
    }

    public synchronized Subject removeSessionByMappingId(Serializable mappingId) {
        Subject subject = (Subject)this.MANAGED_SESSIONS.get(mappingId);
        if (subject != null) {
            this.MANAGED_SESSIONS.remove(mappingId);
        }
        return subject;
    }
}
