package org.example.Model;

import org.neo4j.driver.*;

public class Neo4JConnect {
    private static Neo4JConnect instance;
    private final Driver driver;

    public Neo4JConnect(String uri, String user, String password) {
        driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
    }

    public Neo4JConnect(Driver driver) {
        this.driver = driver;
    }

    public static Neo4JConnect getInstance() {
        if (instance == null) {
            instance = new Neo4JConnect("bolt://localhost:7687", "Neo4j", "Estructuras123");
        }
        return instance;
    }

    public Session getSession() {
        return driver.session();
    }

    public void close() {
        driver.close();
    }
}