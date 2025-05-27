package com.devsuperior.dsmovie.services;

import com.devsuperior.dsmovie.dto.MovieDTO;
import com.devsuperior.dsmovie.entities.MovieEntity;
import com.devsuperior.dsmovie.repositories.MovieRepository;
import com.devsuperior.dsmovie.services.exceptions.DatabaseException;
import com.devsuperior.dsmovie.services.exceptions.ResourceNotFoundException;
import com.devsuperior.dsmovie.tests.MovieFactory;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(SpringExtension.class)
public class MovieServiceTests {

    @InjectMocks
    private MovieService service;

    @Mock
    private MovieRepository movieRepository;

    private Long existingMovieId;
    private Long nonExistingMovieId;
    private Long dependentMovieId;
    private MovieEntity movie;
    private MovieDTO movieDTO;
    private PageImpl<MovieEntity> page;

    @BeforeEach
    void setup() {
        existingMovieId = 1L;
        nonExistingMovieId = 200L;
        dependentMovieId = 3L;
        movie = MovieFactory.createMovieEntity();
        movieDTO = new MovieDTO(movie);
        page = new PageImpl<>(List.of(movie));

        Mockito.when(movieRepository.searchByTitle(any(), any())).thenReturn(page);
        Mockito.when(movieRepository.findById(existingMovieId)).thenReturn(Optional.of(movie));
        Mockito.when(movieRepository.findById(nonExistingMovieId)).thenReturn(Optional.empty());
        Mockito.when(movieRepository.save(any())).thenReturn(movie);
        Mockito.when(movieRepository.getReferenceById(existingMovieId)).thenReturn(movie);
        Mockito.when(movieRepository.getReferenceById(nonExistingMovieId)).thenThrow(EntityNotFoundException.class);
        Mockito.when(movieRepository.existsById(existingMovieId)).thenReturn(true);
        Mockito.when(movieRepository.existsById(dependentMovieId)).thenReturn(true);
        Mockito.when(movieRepository.existsById(nonExistingMovieId)).thenReturn(false);
        Mockito.doThrow(ResourceNotFoundException.class).when(movieRepository).deleteById(nonExistingMovieId);
        Mockito.doThrow(DataIntegrityViolationException.class).when(movieRepository).deleteById(dependentMovieId);
        Mockito.doNothing().when(movieRepository).deleteById(existingMovieId);

    }

    @Test
    public void findAllShouldReturnPagedMovieDTO() {
        Pageable pageable = PageRequest.of(0, 12);
        Page<MovieDTO> result = service.findAll("Test", pageable);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.getTotalPages());
        Assertions.assertEquals(1, result.getTotalElements());
        Assertions.assertEquals(1, result.getNumberOfElements());
        Assertions.assertEquals(0, result.getNumber());
        Assertions.assertEquals(1, result.getSize());
        Mockito.verify(movieRepository, Mockito.times(1)).searchByTitle(Mockito.anyString(), any(Pageable.class));
    }

    @Test
    public void findByIdShouldReturnMovieDTOWhenIdExists() {
        MovieDTO result = service.findById(existingMovieId);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(existingMovieId, result.getId());
        Assertions.assertEquals("Test Movie", result.getTitle());
        Assertions.assertEquals(0.0, result.getScore());
        Assertions.assertEquals(0, result.getCount());
    }

    @Test
    public void findByIdShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
        Assertions.assertThrows(ResourceNotFoundException.class, () -> service.findById(nonExistingMovieId));
    }

    @Test
    public void insertShouldReturnMovieDTO() {
        MovieDTO result = service.insert(movieDTO);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(existingMovieId, result.getId());
        Assertions.assertEquals("Test Movie", result.getTitle());
        Assertions.assertEquals(0.0, result.getScore());
        Assertions.assertEquals(0, result.getCount());
        Mockito.verify(movieRepository, Mockito.times(1)).save(Mockito.any(MovieEntity.class));
    }

    @Test
    public void updateShouldReturnMovieDTOWhenIdExists() {
        MovieDTO result = service.update(existingMovieId, movieDTO);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(existingMovieId, result.getId());
        Assertions.assertEquals("Test Movie", result.getTitle());
        Assertions.assertEquals(0.0, result.getScore());
        Assertions.assertEquals(0, result.getCount());
        Mockito.verify(movieRepository, Mockito.times(1)).save(Mockito.any(MovieEntity.class));
        Mockito.verify(movieRepository, Mockito.times(1)).getReferenceById(existingMovieId);
    }

    @Test
    public void updateShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
        Assertions.assertThrows(ResourceNotFoundException.class, () -> service.update(nonExistingMovieId, movieDTO));
    }

    @Test
    public void deleteShouldDoNothingWhenIdExists() {
        Assertions.assertDoesNotThrow(() -> service.delete(existingMovieId));
    }

    @Test
    public void deleteShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
        Assertions.assertThrows(ResourceNotFoundException.class, () -> service.delete(nonExistingMovieId));
    }

    @Test
    public void deleteShouldThrowDatabaseExceptionWhenDependentId() {
        Assertions.assertThrows(DatabaseException.class, () -> service.delete(dependentMovieId));
    }
}
