package org.faboo.example.datagenerator;

import org.faboo.example.datagenerator.generator.DataGenerator;
import org.neo4j.driver.Driver;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

@SpringBootApplication
public class DataGeneratorApplication {

    public static void main(String[] args ) throws InterruptedException {
        System.out.println("waiting a bit to let neo4j start");
        Thread.sleep(6_000);
        SpringApplication.run( DataGeneratorApplication.class, args );
    }

    @Bean
    public TaskExecutor taskExecutor() {
        return new SimpleAsyncTaskExecutor();
    }

    @Bean
    public CommandLineRunner foo(TaskExecutor executor, Driver driver) {
        return args -> {
            if (args.length != 3) {
                System.out.println("need to provide 3 parameters: numberThreads batchSize sleepTime(in Milliseconds) ");
                System.exit(1);
            }
            var schemaCreator = new SchemaCreator(driver);
            schemaCreator.run();
            int numberThreads = Integer.parseInt(args[0]);
            int batchSize = Integer.parseInt(args[1]);
            int sleepTime = Integer.parseInt(args[2]);
            for (int i = 0; i < numberThreads; i++) {
                executor.execute(new DataGenerator(driver, batchSize, sleepTime, "thread-" + i));
            }
        };
    }
}
