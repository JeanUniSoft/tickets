package pe.edu.utp.tickets.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pe.edu.utp.tickets.dto.CategoryRequest;
import pe.edu.utp.tickets.dto.CategoryResponse;
import pe.edu.utp.tickets.exception.DuplicateResourceException;
import pe.edu.utp.tickets.model.Category;
import pe.edu.utp.tickets.repository.CategoryRepository;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    @DisplayName("UT-01 create guarda la categoría cuando el nombre no existe")
    void create_whenNameIsNew_savesCategory() {
        // Arrange
        when(categoryRepository.existsByNameIgnoreCase("Redes")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        CategoryResponse response = categoryService.create(new CategoryRequest("Redes", "Internet y wifi"));

        // Assert
        assertThat(response.name()).isEqualTo("Redes");
        assertThat(response.active()).isTrue();
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    @DisplayName("UT-02 create lanza DuplicateResourceException cuando el nombre ya existe")
    void create_whenNameExists_throwsDuplicate() {
        when(categoryRepository.existsByNameIgnoreCase("Redes")).thenReturn(true);

        assertThatThrownBy(() -> categoryService.create(new CategoryRequest("Redes", "Internet y wifi")))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Ya existe la categoría Redes");

        verify(categoryRepository, never()).save(any(Category.class));
    }
}