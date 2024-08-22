package com.example.springboot.controllers;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.catalina.connector.Response;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.example.springboot.dtos.ProductRecordDto;
import com.example.springboot.models.ProductModel;
import com.example.springboot.repositories.ProductRepository;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;


@RestController
public class ProductController {
    
    @Autowired
    ProductRepository productRepository;

    @PostMapping("/products")
    public ResponseEntity<ProductModel> saveProduct(@RequestBody @Valid ProductRecordDto productRecordDto) {
        var productModel = new ProductModel();
        BeanUtils.copyProperties(productRecordDto, productModel);
        return ResponseEntity.status(HttpStatus.CREATED).body(productRepository.save(productModel));
    }

    @GetMapping("/products")
    public ResponseEntity<List<ProductModel>> getAllProducts() {
        List<ProductModel> productsList = productRepository.findAll();

        if(productsList.isEmpty()) {
            return ResponseEntity.status(HttpStatus.OK).body(productsList);
        }

        for(ProductModel product : productsList) {
            UUID id = product.getIdProduct();
            product.add(linkTo(methodOn(ProductController.class).getById(id)).withSelfRel());
        }

        return ResponseEntity.status(HttpStatus.OK).body(productsList);
    }
    
    @GetMapping("products/{id}")
    public ResponseEntity<Object> getById(@PathVariable(value="id") UUID id) {
        Optional<ProductModel> product = productRepository.findById(id);
        if(product.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Item não encontrado");
        }
        product.get().add(linkTo(methodOn(ProductController.class).getAllProducts()).withRel("Products"));
        return ResponseEntity.ok().body(product);
    }

    @PutMapping("products/{id}")
    public ResponseEntity<Object> updateProduct(@PathVariable(value="id") UUID id, @RequestBody @Valid ProductRecordDto productRecordDto) {

        Optional<ProductModel> product = productRepository.findById(id);

        if(product.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Item não encontrado");
        }
        ProductModel productM = product.get();
        BeanUtils.copyProperties(productRecordDto, productM);
        return ResponseEntity.status(HttpStatus.OK).body(productRepository.save(productM));
    }

    @DeleteMapping("products/{id}")
    public ResponseEntity<Object> deleteProduct(@PathVariable(value="id") UUID id) {
        Optional<ProductModel> product = productRepository.findById(id);

        if(product.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Produto não encontrado");
        }
        
        productRepository.delete(product.get());

        return ResponseEntity.status(HttpStatus.OK).body("Item excluido com sucesso");
    }   
 }
