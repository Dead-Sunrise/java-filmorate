package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.RatingMPA;
import ru.yandex.practicum.filmorate.storage.mappers.MPARowMapper;
import ru.yandex.practicum.filmorate.storage.films.mpa.MPADbStorage;

import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({MPADbStorage.class, MPARowMapper.class})
class MPADbStorageTest {

    @Autowired
    private MPADbStorage mpaDbStorage;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM films");
        jdbcTemplate.update("DELETE FROM mpa_ratings");
        jdbcTemplate.update("MERGE INTO mpa_ratings(id, name) VALUES (1, 'G'), (2, 'PG'), (3, 'PG-13')");
    }

    @Test
    void findAllMpaTest() { //Тест получения всех мpa
        Collection<RatingMPA> allMpa = mpaDbStorage.findAll();
        assertThat(allMpa).hasSize(3);
        assertThat(allMpa).extracting(RatingMPA::getName)
                .containsExactlyInAnyOrder("G", "PG", "PG-13");
    }

    @Test
    void getMPAByIdTest() { //Тест получения конкретного mpa по id
        Optional<RatingMPA> mpa = mpaDbStorage.getMPAById(1L);
        assertThat(mpa).isPresent();
        assertThat(mpa.get().getName()).isEqualTo("G");
    }

    @Test
    void getMPAByInvalidId() { //Тест получения mpa с несуществующим id
        Optional<RatingMPA> mpa = mpaDbStorage.getMPAById(999L);
        assertThat(mpa).isEmpty();
    }
}