package pe.edu.utp.tickets.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pe.edu.utp.tickets.dto.CategoryRequest;
import pe.edu.utp.tickets.dto.CategoryResponse;
import pe.edu.utp.tickets.exception.DuplicateResourceException;
import pe.edu.utp.tickets.model.Category;
import pe.edu.utp.tickets.repository.CategoryRepository;

@Service
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<CategoryResponse> findAll() {
        return categoryRepository.findAllByOrderByNameAsc().stream()
                .map(CategoryResponse::from)
                .toList();
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        if (categoryRepository.existsByNameIgnoreCase(request.name())) {
            throw new DuplicateResourceException("Ya existe la categoría " + request.name());
        }
        Category category = categoryRepository.save(new Category(request.name(), request.description()));
        return CategoryResponse.from(category);
    }
}