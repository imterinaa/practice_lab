package com.example.springbootcamelkafka.route;

import com.example.springbootcamelkafka.generated.Response;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.kafka.KafkaConstants;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.springframework.stereotype.Component;

@Component
public class StatusRoute extends RouteBuilder {
    @Override
    public void configure() throws Exception {

        try (JaxbDataFormat jaxb = new JaxbDataFormat("com.example.springbootcamelkafka.generated")) {

            from("direct:statusRoute")
                    .process(exchange -> {
                        Response response = new Response();
                        response.setStatus(exchange.getIn().getHeader("Status", String.class));
                        response.setMessage(exchange.getIn().getBody(String.class));
                        exchange.getMessage().setBody(response, Response.class);
                    })
                    .marshal(jaxb)
                    .setHeader(KafkaConstants.KEY, simple("Camel"))
                    .log("Send to status_topic : ${body}")
                    .to("kafka:status_topic?brokers={{kafka.broker1.host}}")
                    .to("micrometer:timer:simple.timer?action=stop");
        }
    }
}
