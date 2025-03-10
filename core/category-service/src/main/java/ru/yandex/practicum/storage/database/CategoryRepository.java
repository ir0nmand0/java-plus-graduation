package ru.yandex.practicum.storage.database;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {

}