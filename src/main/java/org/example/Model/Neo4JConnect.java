package org.example.Model;

import org.neo4j.driver.*;

public class Neo4JConnect {
    private final Driver driver;

    public Neo4JConnect(String uri, String user, String password) {
        driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
    }

    public Neo4JConnect(Driver driver) {
        this.driver = driver;
    }

    public Session getSession() {
        return driver.session();
    }

    public void close() {
        driver.close();
    }
}