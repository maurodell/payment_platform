package com.paymentchain.customer.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.paymentchain.customer.entitites.Customer;
import com.paymentchain.customer.entitites.CustomerProduct;
import com.paymentchain.customer.repository.CustomerRepository;
import io.netty.channel.ChannelOption;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springdoc.core.annotations.RouterOperation;
import org.springframework.http.*;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/customer")
public class CustomerController {

    private final CustomerRepository customerRepository;

    private final WebClient.Builder clientBuilder;

    public CustomerController(CustomerRepository customerRepository, WebClient.Builder clientBuilder) {
        this.customerRepository = customerRepository;
        this.clientBuilder = clientBuilder;
    }
    //WebClient requiere el objeto client de HttpClient con las siguiente propiedades.
    HttpClient client = HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
            .option(ChannelOption.SO_KEEPALIVE, true)
            .option(EpollChannelOption.TCP_KEEPIDLE, 300)
            .option(EpollChannelOption.TCP_KEEPINTVL, 60)
            //Response Timeout: The maximun time we wait to receive a response after sending a request
            .responseTimeout(Duration.ofSeconds(1))
            //Read and Write Timeout: A read timeout occurs when no data was read within a certain
            //period of time, while the write timeout when a write operation cannot finish at a specific time
            .doOnConnected(conn -> {
                conn.addHandlerLast(new ReadTimeoutHandler(5000, TimeUnit.MILLISECONDS));
                conn.addHandlerLast(new WriteTimeoutHandler(5000, TimeUnit.MILLISECONDS));
            });


    @GetMapping
    @RouterOperation(operation = @Operation(description = "Say hello", operationId = "hello", tags = "persons",
            responses = @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Customer.class)))))
    public List<Customer> findAll(){
        return customerRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable("id") Long idCustomer){
        return customerRepository.findById(idCustomer)
                .map(customer -> {
                    return new ResponseEntity<>(customer, HttpStatus.OK);
                })
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Customer customer){
        customer.getProducts().forEach(product -> {
            product.setCustomer(customer);
        });
        Customer customersaved = customerRepository.save(customer);
        return new ResponseEntity<>(customersaved, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable("id") Long idCustomer, @RequestBody Customer customer){
        return customerRepository.findById(idCustomer)
                .map(existingCustomer -> {
                    updateCustomer(existingCustomer, customer);
                    Customer updatedCustomer = customerRepository.save(existingCustomer);
                    return new ResponseEntity<>(updatedCustomer, HttpStatus.OK);
                })
                .orElse(new ResponseEntity<>(HttpStatus.BAD_REQUEST));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") Long idCustomer){
        customerRepository.deleteById(idCustomer);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/full")
    public ResponseEntity<?> getByCodeProduct(@RequestParam(name = "code") String code){
        Customer customer = customerRepository.findByCode(code);
        List<CustomerProduct> products = customer.getProducts();
        //find each product to setting de name
        products.forEach(product -> {
            product.setProductName(getProductName(product.getId()));
        });

        //find all transactions that belong this account number
        List<?> transactions = getTransactions(customer.getIban());
        customer.setTransactions(transactions);
        return new ResponseEntity<>(customer, HttpStatus.OK);
    }

    private void updateCustomer(Customer existingCustomer, Customer newCustomer){
        existingCustomer.setName(newCustomer.getName());
        existingCustomer.setPhone(newCustomer.getPhone());
    }

    /**
     * This method call microservice Product by id and return product name
     * @param id    long
     * @return      product name
     */
    private String getProductName(long id){
        WebClient build = clientBuilder.clientConnector(new ReactorClientHttpConnector(client))
                .baseUrl("http://localhost:8083/api/product")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultUriVariables(Collections.singletonMap("url", "http://localhost:8083/api/product"))
                .build();
        JsonNode block = build.method(HttpMethod.GET).uri("/"+id)
                .retrieve().bodyToMono(JsonNode.class).block();
        String name = block.get("name").asText();
        return name;
    }


    private List<?> getTransactions(String iban){
        WebClient build = clientBuilder.clientConnector(new ReactorClientHttpConnector(client))
                .baseUrl("http://localhost:8082/api/transaction")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                //.defaultUriVariables(Collections.singletonMap("url", "http://localhost:8083/api/transaction/customer/transaction"))
                .build();
        List<?> transactions = build.method(HttpMethod.GET).uri(uriBuilder -> uriBuilder
                        .path("/customer/transaction")
                        .queryParam("ibanAccount", iban)
                        .build())
                .retrieve().bodyToFlux(Object.class).collectList().block();
        return transactions;
    }
}
