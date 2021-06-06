package org.faboo.example.datagenerator;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;

public class SchemaCreator {

    private final Driver driver;

    public SchemaCreator(Driver driver) {
        this.driver = driver;
    }

    public void run() {

        try (Session session = driver.session()) {
            session.writeTransaction(tx -> {
                tx.run("CREATE CONSTRAINT IF NOT EXISTS on (a:Address) ASSERT a.fullAddress IS UNIQUE");
                tx.run("CREATE CONSTRAINT IF NOT EXISTS on (c:City) ASSERT c.name IS UNIQUE");
                tx.run("CREATE CONSTRAINT IF NOT EXISTS on (p:Person) ASSERT p.name IS UNIQUE");
                return null;
            });
        }
    }

}
