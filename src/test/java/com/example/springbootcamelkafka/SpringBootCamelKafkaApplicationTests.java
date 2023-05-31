package com.example.springbootcamelkafka;

import com.example.springbootcamelkafka.repository.TransactionRepository;
import org.apache.camel.ConsumerTemplate;
import org.apache.camel.EndpointInject;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.apache.camel.test.spring.junit5.MockEndpoints;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@MockEndpoints
@CamelSpringBootTest
@EnableAutoConfiguration
@AutoConfigureTestDatabase
@EmbeddedKafka(partitions = 1, brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"}, topics = {"transactions", "results", "status_topic"})
@SpringBootTest(properties = {"kafka.broker1.host=localhost:9092", "kafka.broker2.host=localhost:9092", "kafka.broker1.camel-request-topic-path=direct:transactions"})
public class SpringBootCamelKafkaApplicationTests {
    @Autowired
    private ProducerTemplate producerTemplate;

    @Autowired
    private ConsumerTemplate consumerTemplate;

    @Autowired
    private TransactionRepository repository;

    @EndpointInject("mock:direct:statusRoute")
    public MockEndpoint statusRoute;
    @EndpointInject("mock:jpa:com.example.springbootcamelkafka.gen.Transaction")
    public MockEndpoint saveToDb;
    @EndpointInject("mock:direct:TransactionProcRoute")
    public MockEndpoint transactionProcessingRoute;

    @Test
    void canAddTransactionToRepIfBodyWasCorrect() throws InterruptedException {
        statusRoute.setExpectedMessageCount(1);
        transactionProcessingRoute.setExpectedMessageCount(1);
        producerTemplate.sendBody("direct:transactions", """
                <?xml version="1.0" encoding="UTF-8" ?>
                                
                <gen:Transaction xmlns:gen="/jaxb/gen">
                  <gen:transaction_type>debet</gen:transaction_type >
                  <gen:amount>3253534</gen:amount>
                </gen:Transaction>
                """);
        statusRoute.assertIsSatisfied();
        transactionProcessingRoute.assertIsSatisfied();
        assertEquals(repository.count(), 1);
    }

    @Test
    void cantAddTransactionToRepIfBodyWasIncorrect() throws InterruptedException {
        statusRoute.setExpectedMessageCount(1);
        transactionProcessingRoute.setExpectedMessageCount(0);
        producerTemplate.sendBody("direct:transactions", """
                <?xml version="1.0" encoding="UTF-8" ?>
                                
                <gen:Transaction xmlns:gen="/jaxb/gen">
                  <gen:credit></gen:transaction_type>
                  <gen:amount>33242</gen:amount>
                </gen:Transaction>
                """);
        statusRoute.assertIsSatisfied();
        transactionProcessingRoute.assertIsSatisfied();
        assertEquals(repository.count(), 0);
    }
    @Test
    public void sendSuccessStatusTest() throws InterruptedException {
        statusRoute.expectedBodiesReceived("Success processing");

        producerTemplate.sendBody("direct:transactions", """
                <?xml version="1.0" encoding="UTF-8" ?>
                                
                <gen:Transaction xmlns:gen="/jaxb/gen">
                 <gen:transaction_type>debet</gen:transaction_type >
                  <gen:amount>33242</gen:amount>
                </gen:Transaction>
                """);

        statusRoute.assertIsSatisfied();
    }
    @Test
    public void sendExceptionErrorStatusTest() throws InterruptedException {
        statusRoute.expectedBodiesReceived("UnmarshalException");

        producerTemplate.sendBody("direct:transactions", """
                
                <?xml version="1.0" encoding="UTF-8" ?>
                                
                <gen:Transaction xmlns:gen="/jaxb/gen">
                 <gen:transactidsfds>debet</gen:transaction_type >
                  <gen:amoundfsdt>33242</gen:amount>
                </gen:Transaction>""");

        statusRoute.assertIsSatisfied();
    }

}




