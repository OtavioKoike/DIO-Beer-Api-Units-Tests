package one.digitalinnovation.beerstock.service;

import one.digitalinnovation.beerstock.builder.BeerDTOBuilder;
import one.digitalinnovation.beerstock.dto.BeerDTO;
import one.digitalinnovation.beerstock.entity.Beer;
import one.digitalinnovation.beerstock.exception.BeerAlreadyRegisteredException;
import one.digitalinnovation.beerstock.exception.BeerNotFoundException;
import one.digitalinnovation.beerstock.exception.BeerStockExceededException;
import one.digitalinnovation.beerstock.mapper.BeerMapper;
import one.digitalinnovation.beerstock.repository.BeerRepository;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Para rodar essa classe eu quero utilizar uma extensão do Mockito
@ExtendWith(MockitoExtension.class)
public class BeerServiceTest {

    private static final long INVALID_BEER_ID = 1L;

//  Criar um objeto duble
    @Mock
    private BeerRepository beerRepository;

    private BeerMapper beerMapper = BeerMapper.INSTANCE;

    @InjectMocks
    private BeerService beerService;

    @Test
//  Quando uma cerveja é informada, então a cerveja deve ser criada
    void whenBeerInformedThenItShouldBeCreated() throws BeerAlreadyRegisteredException {
        // given
//      Cria uma cerveja com valores Default
        BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
//      Converte para o modelo Beer
        Beer expectedSavedBeer = beerMapper.toModel(expectedBeerDTO);

        // when
//      Usando Mockito
//      Quando buscar a cerveja Default (Brahma) vai retornar um vazio
        Mockito.when(beerRepository.findByName(expectedBeerDTO.getName())).thenReturn(Optional.empty());
//      Vai salvar a cerveja Default e retorna-la
        Mockito.when(beerRepository.save(expectedSavedBeer)).thenReturn(expectedSavedBeer);

        //then
//      Realmente criando a cerveja Default no banco de dados chamando o service
        BeerDTO createdBeerDTO = beerService.createBeer(expectedBeerDTO);


//      Verificando se foi realizado com sucesso (Usando Hamcrest)
        MatcherAssert.assertThat(createdBeerDTO.getId(), Matchers.is(equalTo(expectedBeerDTO.getId())));
        MatcherAssert.assertThat(createdBeerDTO.getName(), Matchers.is(equalTo(expectedBeerDTO.getName())));
        MatcherAssert.assertThat(createdBeerDTO.getQuantity(), Matchers.is(equalTo(expectedBeerDTO.getQuantity())));

        MatcherAssert.assertThat(createdBeerDTO.getQuantity(), Matchers.is(greaterThan(2)));

//      Verificando se foi realizado com sucesso (Usando Asserts)
//        assertEquals(expectedBeerDTO.getId(), createdBeerDTO.getId());
//        assertEquals(expectedBeerDTO.getName(), createdBeerDTO.getName());
    }

    @Test
//  Quando uma cerveja já criada é informada, uma exceçao é lançada
    void whenAlreadyRegisteredBeerInformedThenAnExceptionShouldBeThrown() {
        // given
//      Cria uma cerveja com valores Default
        BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
//      Converte para o modelo Beer
        Beer duplicatedBeer = beerMapper.toModel(expectedBeerDTO);

        // when
//      Usando Mockito
//      Quando buscar a cerveja Default (Brahma) vai retornar um duplicatedBeer
        when(beerRepository.findByName(expectedBeerDTO.getName())).thenReturn(Optional.of(duplicatedBeer));

        // then
//      Essa excessão é lançada quando em uma outra thread chamar createBeer
        assertThrows(BeerAlreadyRegisteredException.class, () -> beerService.createBeer(expectedBeerDTO));
    }

    @Test
//  Quando um nome valido de cerveja, retorna a cerveja
    void whenValidBeerNameIsGivenThenReturnABeer() throws BeerNotFoundException {
        // given
//      Cria uma cerveja com valores Default
        BeerDTO expectedFoundBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
//      Converte para o modelo Beer
        Beer expectedFoundBeer = beerMapper.toModel(expectedFoundBeerDTO);

        // when
//      Usando Mockito
//      Quando busca o nome da cerveja Default, retorna a cerveja
        when(beerRepository.findByName(expectedFoundBeer.getName())).thenReturn(Optional.of(expectedFoundBeer));

        // then
//      Realmente busca a cerveja Default no banco de dados chamando o service
        BeerDTO foundBeerDTO = beerService.findByName(expectedFoundBeerDTO.getName());

//      Verifica se é igual ao esperado
        assertThat(foundBeerDTO, is(equalTo(expectedFoundBeerDTO)));
    }

    @Test
//  Quando não tem o nome registrado, retornar uma excessao
    void whenNotRegisteredBeerNameIsGivenThenThrowAnException() {
        // given
//      Cria uma cerveja com valores Default
        BeerDTO expectedFoundBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();

        // when
//      Quando busca o nome da cerveja Default, retorna vazio
        when(beerRepository.findByName(expectedFoundBeerDTO.getName())).thenReturn(Optional.empty());

        // then
//      Essa excessão é lançada quando em uma outra thread chamar findByName
        assertThrows(BeerNotFoundException.class, () -> beerService.findByName(expectedFoundBeerDTO.getName()));
    }

    @Test
//  Quando chamar ListBeer, retornar lista de cervejas
    void whenListBeerIsCalledThenReturnAListOfBeers() {
        // given
//      Cria uma cerveja com valores Default
        BeerDTO expectedFoundBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
//      Converte para o modelo Beer
        Beer expectedFoundBeer = beerMapper.toModel(expectedFoundBeerDTO);

        //when
//      Quando chamar findAll, retornar uma coleção
        when(beerRepository.findAll()).thenReturn(Collections.singletonList(expectedFoundBeer));

        //then
//      Cria uma lista chamando o listAll
        List<BeerDTO> foundListBeersDTO = beerService.listAll();

//      Verificando se ela não é vazia
        assertThat(foundListBeersDTO, is(not(empty())));
//      Verificando se o primeiro elemento é a cerveja esperada
        assertThat(foundListBeersDTO.get(0), is(equalTo(expectedFoundBeerDTO)));
    }

    @Test
//  Quando chamar ListBeer, retornar lista vazia de cervejas
    void whenListBeerIsCalledThenReturnAnEmptyListOfBeers() {
        //when
//      Quando chamar findAll, retornar uma colecao vazia
        when(beerRepository.findAll()).thenReturn(Collections.EMPTY_LIST);

        //then
//      Cria uma lista chamando o listAll
        List<BeerDTO> foundListBeersDTO = beerService.listAll();

//      Verificando se a lista é vazia
        assertThat(foundListBeersDTO, is(empty()));
    }

    @Test
//  Quando é chamado a exclusão com id valido, a cerveja vai ser deletado
    void whenExclusionIsCalledWithValidIdThenABeerShouldBeDeleted() throws BeerNotFoundException{
        // given
//      Cria uma cerveja com valores Default
        BeerDTO expectedDeletedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
//      Converte para o modelo Beer
        Beer expectedDeletedBeer = beerMapper.toModel(expectedDeletedBeerDTO);

        // when
//      Quando o findById é chamado, retornar a cerveja
        when(beerRepository.findById(expectedDeletedBeerDTO.getId())).thenReturn(Optional.of(expectedDeletedBeer));
        doNothing().when(beerRepository).deleteById(expectedDeletedBeerDTO.getId());
        //      Como não retorna nada, não vai fazer nada

        // then
//      Então chama o deleteById
        beerService.deleteById(expectedDeletedBeerDTO.getId());

//      Apenas verifica se passou uma vez pelo findById
        verify(beerRepository, times(1)).findById(expectedDeletedBeerDTO.getId());
//      Apenas verifica se passou uma vez pelo deleteById
        verify(beerRepository, times(1)).deleteById(expectedDeletedBeerDTO.getId());
    }

//  Feito por Otávio Koike
    @Test
//  Quando não não, retornar uma excessao
    void whenExclusionIsCalledWithoutValidIdThenThrowAnException() {
        // given
//      Cria uma cerveja com valores Default
        BeerDTO expectedDeletedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();

        // when
//      Quando busca o nome da cerveja Default, retorna vazio
        when(beerRepository.findById(expectedDeletedBeerDTO.getId())).thenReturn(Optional.empty());


        // then
//      Essa excessão é lançada quando em uma outra thread chamar findByName
        assertThrows(BeerNotFoundException.class, () -> beerService.deleteById(expectedDeletedBeerDTO.getId()));
    }


    @Test
//  Quando Incremento é chamado, incrementa stock da cerveja
    void whenIncrementIsCalledThenIncrementBeerStock() throws BeerNotFoundException, BeerStockExceededException {
        //given
//      Cria uma cerveja com valores Default
        BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
//      Converte para o modelo Beer
        Beer expectedBeer = beerMapper.toModel(expectedBeerDTO);

        //when
//      1° Refinamento, buscar a cerveja
        when(beerRepository.findById(expectedBeerDTO.getId())).thenReturn(Optional.of(expectedBeer));
//      2° Refinamento, salvar a cerveja
        when(beerRepository.save(expectedBeer)).thenReturn(expectedBeer);

//      Quantas cervejas vou incrementar
        int quantityToIncrement = 10;
//      Cervejas totais depois do incremento
        int expectedQuantityAfterIncrement = expectedBeerDTO.getQuantity() + quantityToIncrement;

        // then
//      Chama o incremento passando o id da cerveja Default e a quantidade a ser incrementada
        BeerDTO incrementedBeerDTO = beerService.increment(expectedBeerDTO.getId(), quantityToIncrement);

//      Verifica se a quantidade depois do incremento é igual a quantidade da cerveja
        assertThat(expectedQuantityAfterIncrement, equalTo(incrementedBeerDTO.getQuantity()));
//      Verifica se a quantidade depois do incremento é menor a quantidade maxima
        assertThat(expectedQuantityAfterIncrement, lessThan(expectedBeerDTO.getMax()));
    }

    @Test
//  Quando o incremento é maior que o maximo, lança exceção
    void whenIncrementIsGreatherThanMaxThenThrowException() {
        //given
//      Cria uma cerveja com valores Default
        BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
//      Converte para o modelo Beer
        Beer expectedBeer = beerMapper.toModel(expectedBeerDTO);

        //when
//      1° Refinamento, buscar a cerveja
        when(beerRepository.findById(expectedBeerDTO.getId())).thenReturn(Optional.of(expectedBeer));

        // then
//      Incremento maior que o max
        int quantityToIncrement = 80;
//      lança excessao quando chama incremento
        assertThrows(BeerStockExceededException.class, () -> beerService.increment(expectedBeerDTO.getId(), quantityToIncrement));
    }

    @Test
//  Quando a soma do incremento é maior que o maximo, lança uma excessao
    void whenIncrementAfterSumIsGreatherThanMaxThenThrowException() {
        //given
//      Cria uma cerveja com valores Default
        BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
//      Converte para o modelo Beer
        Beer expectedBeer = beerMapper.toModel(expectedBeerDTO);

        //When
//      1° Refinamento, buscar a cerveja
        when(beerRepository.findById(expectedBeerDTO.getId())).thenReturn(Optional.of(expectedBeer));

        // Then
//      Incremento
        int quantityToIncrement = 45;
//      lança excessao ao incrementar
        assertThrows(BeerStockExceededException.class, () -> beerService.increment(expectedBeerDTO.getId(), quantityToIncrement));
    }

    @Test
//  Quando incremento é chamado com id Invalido, lança excessao
    void whenIncrementIsCalledWithInvalidIdThenThrowException() {
        int quantityToIncrement = 10;

//      busca um id invalido
        when(beerRepository.findById(INVALID_BEER_ID)).thenReturn(Optional.empty());

//      lança uma excessao quando chamar o incremento
        assertThrows(BeerNotFoundException.class, () -> beerService.increment(INVALID_BEER_ID, quantityToIncrement));
    }

    @Test
    void whenDecrementIsCalledThenDecrementBeerStock() throws BeerNotFoundException, BeerStockExceededException {
        BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer expectedBeer = beerMapper.toModel(expectedBeerDTO);

        when(beerRepository.findById(expectedBeerDTO.getId())).thenReturn(Optional.of(expectedBeer));
        when(beerRepository.save(expectedBeer)).thenReturn(expectedBeer);

        int quantityToDecrement = 5;
        int expectedQuantityAfterDecrement = expectedBeerDTO.getQuantity() - quantityToDecrement;
        BeerDTO incrementedBeerDTO = beerService.decrement(expectedBeerDTO.getId(), quantityToDecrement);

        assertThat(expectedQuantityAfterDecrement, equalTo(incrementedBeerDTO.getQuantity()));
        assertThat(expectedQuantityAfterDecrement, greaterThan(0));
    }

    @Test
    void whenDecrementIsCalledToEmptyStockThenEmptyBeerStock() throws BeerNotFoundException, BeerStockExceededException {
        BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer expectedBeer = beerMapper.toModel(expectedBeerDTO);

        when(beerRepository.findById(expectedBeerDTO.getId())).thenReturn(Optional.of(expectedBeer));
        when(beerRepository.save(expectedBeer)).thenReturn(expectedBeer);

        int quantityToDecrement = 10;
        int expectedQuantityAfterDecrement = expectedBeerDTO.getQuantity() - quantityToDecrement;
        BeerDTO incrementedBeerDTO = beerService.decrement(expectedBeerDTO.getId(), quantityToDecrement);

        assertThat(expectedQuantityAfterDecrement, equalTo(0));
        assertThat(expectedQuantityAfterDecrement, equalTo(incrementedBeerDTO.getQuantity()));
    }

    @Test
    void whenDecrementIsLowerThanZeroThenThrowException() {
        BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer expectedBeer = beerMapper.toModel(expectedBeerDTO);

        when(beerRepository.findById(expectedBeerDTO.getId())).thenReturn(Optional.of(expectedBeer));

        int quantityToDecrement = 80;
        assertThrows(BeerStockExceededException.class, () -> beerService.decrement(expectedBeerDTO.getId(), quantityToDecrement));
    }

    @Test
    void whenDecrementIsCalledWithInvalidIdThenThrowException() {
        int quantityToDecrement = 10;

        when(beerRepository.findById(INVALID_BEER_ID)).thenReturn(Optional.empty());

        assertThrows(BeerNotFoundException.class, () -> beerService.decrement(INVALID_BEER_ID, quantityToDecrement));
    }
}
