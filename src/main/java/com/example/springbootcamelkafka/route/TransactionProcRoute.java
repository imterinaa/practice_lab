//package com.example.springbootcamelkafka.route;
//
//import com.example.springbootcamelkafka.model.Request;
//import lombok.RequiredArgsConstructor;
//import org.apache.camel.LoggingLevel;
//import org.apache.camel.builder.RouteBuilder;
//import org.apache.camel.component.kafka.KafkaConstants;
//import org.apache.camel.model.dataformat.JsonLibrary;
//import org.springframework.stereotype.Component;
//
//@Component
//public class TransactionProcRoute extends RouteBuilder {
//    @Override
//    public void configure() {
//        onException(Exception.class)
//                .handled(true)
//                .log("Request Processing Route Error: " + exceptionMessage())
//                .setHeader("Status", simple("Error"))
//                .setBody(exceptionMessage())
//                .to("micrometer:counter:simple.errors.count")
//                .to("direct:statusRoute")
//                .markRollbackOnly();
//
//        from("direct:requestProcessingRoute")
//                .routeId("Request Processing Route")
//                .transacted()
//                .to("direct:databaseRoute")
//                .to("direct:resultsRoute")
//                .setHeader("Status", simple("Success"))
//                .log("Message received from Kafka1 : ${body}")
//                .setBody(simple("Success processing"))
//                .to("micrometer:counter:simple.success.count")
//                .to("direct:statusRoute");
//
//        from("direct:databaseRoute")
//                .routeId("Database Route")
//                .process(exchange -> {
//                    com.example.springbootcamelkafka.gen.Request in = exchange.getIn().getBody(com.example.springbootcamelkafka.gen.Request.class);
//                    Request request = new Request();
//                    request.setRequestID(in.getRequestID());
//                    request.setCode(in.getCode());
//                    exchange.getMessage().setBody(request, Request.class);
//                })
//                .to("jpa:com.example.springbootcamelkafka.model.Request");
//
//        from("direct:resultsRoute")
//                .routeId("Results Route")
//                .process(exchange -> {
//                    Request request = exchange.getIn().getBody(Request.class);
//                    exchange.getMessage().setBody(request, Request.class);
//                })
//                .marshal().json(JsonLibrary.Jackson)
//                .setHeader(KafkaConstants.KEY, simple("Camel"))
//                .to("kafka:results?brokers={{kafka.broker2.host}}");
//    }
//}

package com.example.springbootcamelkafka.route;

import com.example.springbootcamelkafka.entity.Transaction;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.kafka.KafkaConstants;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.stereotype.Component;

@Component
public class TransactionProcRoute extends RouteBuilder {
    @Override
    public void configure() {
        onException(Exception.class)
                .handled(true)
                .log("Transaction Processing Route Error: " + exceptionMessage())
                .setHeader("Status", simple("Error"))
                .setBody(exceptionMessage())
                .to("micrometer:counter:simple.errors.count")
                .to("direct:statusRoute")
                .markRollbackOnly();

        from("direct:TransactionProcRoute")
                .routeId("Transaction Processing Route")
                .transacted()
                .to("direct:databaseRoute")
                .to("direct:resultsRoute")
                .setHeader("Status", simple("Success"))
                .log("Message received from Kafka1 : ${body}")
                .setBody(simple("Success processing"))
                .to("micrometer:counter:simple.success.count")
                .to("direct:statusRoute");

        from("direct:databaseRoute")
                .routeId("Database Route")
                .process(exchange -> {
                    com.example.springbootcamelkafka.generated.Transaction in = exchange.getIn().getBody(com.example.springbootcamelkafka.generated.Transaction.class);
                    Transaction tr = new Transaction();
                    tr.setTransaction_type(in.getTransaction_type());
                    tr.setAmount(in.getAmount());
                    exchange.getMessage().setBody(tr, Transaction.class);
                })
                .to("jpa:com.example.springbootcamelkafka.model.Transaction");

        from("direct:resultsRoute")
                .routeId("Results Route")
                .process(exchange -> {
                    Transaction transaction = exchange.getIn().getBody(Transaction.class);
                    exchange.getMessage().setBody(transaction, Transaction.class);
                })
                .marshal().json(JsonLibrary.Jackson)
                .setHeader(KafkaConstants.KEY, simple("Camel"))
                .to("kafka:results?brokers={{kafka.broker2.host}}");
    }
}
