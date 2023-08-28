package io.swagger;


import com.chtrembl.petstore.pet.model.*;
import io.swagger.repository.CategoryRepository;
import io.swagger.repository.PetRepository;
import io.swagger.repository.TagRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PetCacheService {
    static final Logger log = LoggerFactory.getLogger(PetCacheService.class);

    @Autowired
    private TagRepository tagRepo;

    @Autowired
    private CategoryRepository categoryRepo;

    @Autowired
    private PetRepository petRepo;

    @Cacheable("pets")
    public List<Pet> findAll(){
        List<Pet> products = petRepo.findAll();
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
        categories.add(buildCategory(1l,"Dog"));
        categories.add(buildCategory(2l,"Cat"));
        categories.add(buildCategory(3l,"Fish"));
        categoryRepo.saveAll(categories);
    }

    public void saveAll(List<Pet>products){
        petRepo.saveAll(products);
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
