package com.example.springbootcamelkafka.route;

import jakarta.xml.bind.UnmarshalException;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.apache.camel.spi.DataFormat;
import org.springframework.stereotype.Component;

@Component
public class TransactionRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        try (DataFormat jaxb = new JaxbDataFormat("com.example.springbootcamelkafka.generated")) {

            onException(UnmarshalException.class)
                    .handled(true)
                    .log("Transaction Route Error: " + exceptionMessage())
                    .setHeader("Status", simple("Error"))
                    .setBody(simple("UnmarshalException"))
                    .to("micrometer:counter:simple.errors.count")
                    .to("direct:statusRoute");


            from("{{kafka.broker1.camel-request-topic-path}}")
                    .routeId("Requests Route")
                    .to("micrometer:timer:simple.timer?action=start")
                    .to("micrometer:counter:simple.request.count")
                    .log("Message received from Kafka1 : ${body}")
                    .log("    on the topic ${headers[kafka.TOPIC]}")
                    .log("    on the partition ${headers[kafka.PARTITION]}")
                    .log("    with the offset ${headers[kafka.OFFSET]}")
                    .log("    with the key ${headers[kafka.KEY]}")
                    .unmarshal(jaxb)
                    .to("direct:TransactionProcRoute");
        }
    }
}
