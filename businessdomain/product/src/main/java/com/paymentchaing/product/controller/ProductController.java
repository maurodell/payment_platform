package com.paymentchaing.product.controller;

import com.paymentchaing.product.entities.Product;
import com.paymentchaing.product.repository.ProductRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springdoc.core.annotations.RouterOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/product")
public class ProductController {

    private final ProductRepository productRepository;

    public ProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping
    @RouterOperation(operation = @Operation(description = "Say hello", operationId = "hello", tags = "persons",
            responses = @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Product.class)))))
    public List<Product> findAll(){
        return productRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable("id") Long idProduct){
        return productRepository.findById(idProduct)
                .map(customer -> {
                    return new ResponseEntity<>(customer, HttpStatus.OK);
                })
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    @Operation(
            summary = "Create a new customer",
            description = "Creates a new customer and returns the created customer with HTTP status 201 (Created).",
            tags = {"Product"},
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Product object to be created",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = Product.class)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Product successfully created",
                            content = @Content(
                                    schema = @Schema(implementation = Product.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input data"
                    )
            }
    )
    public ResponseEntity<?> create(@RequestBody Product customer){
        Product customersaved = productRepository.save(customer);
        return new ResponseEntity<>(customersaved, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable("id") Long idProduct, @RequestBody Product customer){
        return productRepository.findById(idProduct)
                .map(existingProduct -> {
                    updateProduct(existingProduct, customer);
                    Product updatedProduct = productRepository.save(existingProduct);
                    return new ResponseEntity<>(updatedProduct, HttpStatus.OK);
                })
                .orElse(new ResponseEntity<>(HttpStatus.BAD_REQUEST));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> detele(@PathVariable("id") Long idProduct){
        productRepository.deleteById(idProduct);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    private void updateProduct(Product existingProduct, Product newProduct){
        existingProduct.setName(newProduct.getName());
        existingProduct.setCode(newProduct.getCode());
    }
}
