package com.devsuperior.dsmovie.services;

import com.devsuperior.dsmovie.dto.MovieDTO;
import com.devsuperior.dsmovie.dto.ScoreDTO;
import com.devsuperior.dsmovie.entities.MovieEntity;
import com.devsuperior.dsmovie.entities.ScoreEntity;
import com.devsuperior.dsmovie.entities.UserEntity;
import com.devsuperior.dsmovie.repositories.MovieRepository;
import com.devsuperior.dsmovie.repositories.ScoreRepository;
import com.devsuperior.dsmovie.services.exceptions.ResourceNotFoundException;
import com.devsuperior.dsmovie.tests.MovieFactory;
import com.devsuperior.dsmovie.tests.ScoreFactory;
import com.devsuperior.dsmovie.tests.UserFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

@ExtendWith(SpringExtension.class)
public class ScoreServiceTests {

    @InjectMocks
    private ScoreService service;

    @Mock
    private UserService userService;

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private ScoreRepository scoreRepository;

    private Long existingMovieId;
    private Long nonExistingMovieId;
    private MovieDTO movieDTO;
    private ScoreDTO scoreDTO;
    private MovieEntity movie;
    private ScoreEntity score;
    private UserEntity user;

    @BeforeEach
    void setup() {
        existingMovieId = 1L;
        nonExistingMovieId = 200L;
        movieDTO = MovieFactory.createMovieDTO();
        movie = MovieFactory.createMovieEntity();
        scoreDTO = ScoreFactory.createScoreDTO();
        score = ScoreFactory.createScoreEntity();
        user = UserFactory.createUserEntity();
        score.setMovie(movie);
        score.setUser(user);
        score.setValue(ScoreFactory.scoreValue);
        movie.getScores().add(score);

        Mockito.when(userService.authenticated()).thenReturn(user);
        Mockito.when(scoreRepository.saveAndFlush(score)).thenReturn(score);
        Mockito.when(movieRepository.findById(existingMovieId)).thenReturn(Optional.of(movie));
        Mockito.when(movieRepository.findById(nonExistingMovieId)).thenReturn(Optional.empty());
        Mockito.when(movieRepository.save(movie)).thenReturn(movie);
        Mockito.when(scoreRepository.save(score)).thenReturn(score);
    }

    @Test
    public void saveScoreShouldReturnMovieDTO() {

        MovieDTO result = service.saveScore(scoreDTO);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(existingMovieId, result.getId());
        Assertions.assertEquals(movieDTO.getTitle(), result.getTitle());
        Assertions.assertNotNull(score.getValue());
    }

    @Test
    public void saveScoreShouldThrowResourceNotFoundExceptionWhenNonExistingMovieId() {
        Assertions.assertThrows(ResourceNotFoundException.class, () -> service.saveScore(new ScoreDTO(nonExistingMovieId, ScoreFactory.scoreValue)));
    }
}
