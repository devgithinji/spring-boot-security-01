package com.densoft.springbootbasicauth.controller;

import com.densoft.springbootbasicauth.model.Product;
import com.densoft.springbootbasicauth.service.ProductService;
import org.hibernate.query.criteria.internal.predicate.PredicateImplementor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public List<Product> getProducts(Model model) {
        return productService.listAll();
    }

    @PostMapping
    public Product saveProduct(@RequestBody Product product) {
        return productService.save(product);
    }

    @GetMapping("/{productId}")
    public Product getProduct(@PathVariable("productId") long id) {
        return productService.get(id);
    }


    @DeleteMapping("/{id}")
    public String deleteProduct(@PathVariable(name = "id") int id) {
        productService.delete(id);
        return "product deleted";
    }
}
