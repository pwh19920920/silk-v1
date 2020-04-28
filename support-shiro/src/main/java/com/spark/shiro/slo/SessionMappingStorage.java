package com.spark.shiro.slo;

import org.apache.shiro.subject.Subject;

import java.io.Serializable;

public interface SessionMappingStorage {
    Subject removeSessionByMappingId(Serializable var1);

    void addSessionById(Serializable var1, Subject var2);
}
