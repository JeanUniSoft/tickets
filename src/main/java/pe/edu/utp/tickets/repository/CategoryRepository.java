package pe.edu.utp.tickets.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import pe.edu.utp.tickets.model.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsByNameIgnoreCase(String name);

    List<Category> findAllByOrderByNameAsc();
}