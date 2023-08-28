package io.swagger;

import com.chtrembl.petstore.product.model.Category;
import com.chtrembl.petstore.product.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
