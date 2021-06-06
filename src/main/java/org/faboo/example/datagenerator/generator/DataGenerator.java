package org.faboo.example.datagenerator.generator;

import com.github.javafaker.Address;
import com.github.javafaker.Faker;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;

import java.util.*;
import java.util.stream.Collectors;

import static org.neo4j.driver.Values.parameters;

public class DataGenerator implements Runnable {

    private final int batchSize;
    private final long pauseTime;
    private final Driver driver;
    private final String id;

    public DataGenerator(Driver driver, int batchSize, long pauseTime, String id) {
        this.batchSize = batchSize;
        this.pauseTime = pauseTime;
        this.driver = driver;
        this.id = id;
    }

    @Override
    public void run() {

        while (true) {
            var addresses = generateAddress();
            persistAddresses(addresses);
            System.out.println(id + "\t: persisted " + batchSize + " addresses (and persons and cities)");
            try {
                Thread.sleep(pauseTime);
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    private Collection<Address> generateAddress() {
        var faker = new Faker( Locale.GERMAN );
        var batch = new ArrayList<Address>(batchSize);
        for ( int i = 0; i < batchSize; i++ ) {
            batch.add( faker.address() );
        }
        return batch;
    }

    private void persistAddresses(Collection<Address> addresses) {

        List<Map<String, Object>> params = addresses
                .stream()
                .map(address -> {
                    var map = new HashMap<String, Object>();
                    map.put("city", address.city());
                    map.put("country", address.country());
                    map.put("countryCode", address.countryCode());
                    map.put("fullAddress", address.fullAddress());
                    map.put("streetName", address.streetName());
                    map.put("streetAddressNumber", address.streetAddressNumber());
                    map.put("firstName", address.firstName());
                    map.put("lastName", address.lastName());
                    map.put("zipCode", address.zipCode());
                    return map;
                }).collect(Collectors.toList());

        try (Session session = driver.session()) {
            session.writeTransaction(tx -> {
                tx.run("foreach(adr in $adr | " +
                                "MERGE (c:City {name: adr.city}) ON CREATE SET c.country= adr.country, c.countryCode = adr.countryCode " +
                                "MERGE (a:Address {fullAddress: adr.fullAddress}) ON CREATE SET a.streetName = adr.streetName, a.streetAddressNumber= adr.streetAddressNumber, a.zipCode = adr.zipCode " +
                                "MERGE (p:Person {name: adr.firstName + ' ' + adr.lastName}) ON CREATE SET p.firstname = adr.firstName, p.lastName = adr.lastName " +
                                "MERGE (a)-[:BELONGS_TO]->(c) " +
                                "MERGE (p)-[:LIVES_AT]->(a)" +
                                " )",
                        parameters("adr", params));
                return null;
            });
        }
    }

}
