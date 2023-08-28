package io.swagger;


import com.chtrembl.petstore.product.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductCacheService {
    static final Logger log = LoggerFactory.getLogger(ProductCacheService.class);

    @Autowired
    private TagRepository tagRepo;

    @Autowired
    private CategoryRepository categoryRepo;

    @Autowired
    private ProductRepository productRepo;

    @Cacheable("products")
    public List<Product> findAll(){
        List<Product> products = productRepo.findAll();
        log.info("collecting products from db "+products.size());
        return products;
    }


    public void loadTags(){
        List<Tag> tags = new ArrayList<>();
        tags.add(buildTag(1l,"Small"));
        tags.add(buildTag(2l,"Large"));
        tagRepo.saveAll(tags);
    }
    public void loadCategories(){
        List<Category> categories = new ArrayList<>();
        categories.add(buildCategory(1l,"Dog Toy"));
        categories.add(buildCategory(2l,"Dog Food"));
        categories.add(buildCategory(3l,"Cat Toy"));
        categories.add(buildCategory(4l,"Cat Food"));
        categories.add(buildCategory(5l,"Fish Toy"));
        categories.add(buildCategory(6l,"Fish Food"));
        categoryRepo.saveAll(categories);
    }

    public void saveAll(List<Product>products){
        productRepo.saveAll(products);
    }



    public Tag buildTag(Long id, String name) {
        Tag inst = new Tag();
        inst.setName(name);
        inst.setId(id);
        return inst;
    }

    public Category buildCategory(Long id, String name) {
        Category inst = new Category();
        inst.setName(name);
        inst.setId(id);
        return inst;
    }
}
