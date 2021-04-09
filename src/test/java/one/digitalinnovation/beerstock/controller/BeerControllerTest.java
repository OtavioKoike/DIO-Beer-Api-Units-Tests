package one.digitalinnovation.beerstock.controller;

import one.digitalinnovation.beerstock.builder.BeerDTOBuilder;
import one.digitalinnovation.beerstock.dto.BeerDTO;
import one.digitalinnovation.beerstock.dto.QuantityDTO;
import one.digitalinnovation.beerstock.exception.BeerNotFoundException;
import one.digitalinnovation.beerstock.exception.BeerStockExceededException;
import one.digitalinnovation.beerstock.service.BeerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import java.util.Collections;

import static one.digitalinnovation.beerstock.utils.JsonConvertionUtils.asJsonString;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.mock.http.server.reactive.MockServerHttpRequest.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Para rodar essa classe eu quero utilizar uma extensão do Mockito
@ExtendWith(MockitoExtension.class)
public class BeerControllerTest {

//  API Principal
    private static final String BEER_API_URL_PATH = "/api/v1/beers";
//  Cerveja Valida
    private static final long VALID_BEER_ID = 1L;
//  Cerveja Invalida
    private static final long INVALID_BEER_ID = 2l;
//  Caminho do incremento de cerveja
    private static final String BEER_API_SUBPATH_INCREMENT_URL = "/increment";
//  Caminho do decremento de cerveja
    private static final String BEER_API_SUBPATH_DECREMENT_URL = "/decrement";

    private MockMvc mockMvc;

    //  Criar um objeto duble
    @Mock
    private BeerService beerService;

    @InjectMocks
    private BeerController beerController;

    @BeforeEach
//  Antes de cada teste, fazer a configuração do objeto mockmvc
    void setUp() {
//      Fazer o setup somente para a classe beerControler
        mockMvc = MockMvcBuilders.standaloneSetup(beerController)
//              Adicionar suporte a objetos paginaveis
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
//              Mapeamento Jackson para Json
                .setViewResolvers((s, locale) -> new MappingJackson2JsonView())
                .build();
    }

    @Test
//  Quando chamar o POST, uma cerveja é criada
    void whenPOSTIsCalledThenABeerIsCreated() throws Exception {
        // given
//      Cria uma cerveja com valores Default
        BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();

        // when
//      Usando Mockito
//      Quando criar a cerveja Default (Brahma) vai retornar a cerveja criada
        when(beerService.createBeer(beerDTO)).thenReturn(beerDTO);

        // then
//      Como se estivesse chamando o postman
        mockMvc.perform(post(BEER_API_URL_PATH)
                .contentType(MediaType.APPLICATION_JSON)
//              Conteudo (Converte Objeto para JSON)
                .content(asJsonString(beerDTO)))
//              Espera um status
                .andExpect(status().isCreated())
//              Espera um nome
                .andExpect(jsonPath("$.name", is(beerDTO.getName())))
//              Espera uma marca
                .andExpect(jsonPath("$.brand", is(beerDTO.getBrand())))
//              Espera um tipo
                .andExpect(jsonPath("$.type", is(beerDTO.getType().toString())));
    }

    @Test
//  Quando chamar o POST sem um campo obrigatorio, retornar erro
    void whenPOSTIsCalledWithoutRequiredFieldThenAnErrorIsReturned() throws Exception {
        // given
//      Cria uma cerveja com valores Default
        BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
//      Tira a marca da cerveja Default
        beerDTO.setBrand(null);

        // then
        mockMvc.perform(post(BEER_API_URL_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(beerDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
//  Quando chamamos o GET com nome valido, retornar status OK
    void whenGETIsCalledWithValidNameThenOkStatusIsReturned() throws Exception {
        // given
//      Cria uma cerveja com valores Default
        BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();

        //when
//      Quando chamar o findByName, retornar a cerveja
        when(beerService.findByName(beerDTO.getName())).thenReturn(beerDTO);

        // then
//      É passado o nome do caminho + nome da cerveja
        mockMvc.perform(MockMvcRequestBuilders.get(BEER_API_URL_PATH + "/" + beerDTO.getName())
                .contentType(MediaType.APPLICATION_JSON))
//              Espera um status OK
                .andExpect(status().isOk())
//              Espera um nome
                .andExpect(jsonPath("$.name", is(beerDTO.getName())))
//              Espera uma marca
                .andExpect(jsonPath("$.brand", is(beerDTO.getBrand())))
//              Espera um tipo
                .andExpect(jsonPath("$.type", is(beerDTO.getType().toString())));
    }

    @Test
//  Quando é chamado o GET com um nome não registrado, retorna Status não Encontrado
    void whenGETIsCalledWithoutRegisteredNameThenNotFoundStatusIsReturned() throws Exception {
        // given
//      Cria uma cerveja com valores Default
        BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();

        //when
//      Quando chamar o findByName, retornar uma excessão
        when(beerService.findByName(beerDTO.getName())).thenThrow(BeerNotFoundException.class);

        // then
//      É passado o nome do caminho + nome da cerveja
        mockMvc.perform(MockMvcRequestBuilders.get(BEER_API_URL_PATH + "/" + beerDTO.getName())
                .contentType(MediaType.APPLICATION_JSON))
//              Espera um status Not Found
                .andExpect(status().isNotFound());
    }

    @Test
//  Quando uma lista com cerveja for chamado, retornar status OK
    void whenGETListWithBeersIsCalledThenOkStatusIsReturned() throws Exception {
        // given
//      Cria uma cerveja com valores Default
        BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();

        //when
//      Quando chamar o listAll, retornar a coleção
        when(beerService.listAll()).thenReturn(Collections.singletonList(beerDTO));

        // then
        mockMvc.perform(MockMvcRequestBuilders.get(BEER_API_URL_PATH)
                .contentType(MediaType.APPLICATION_JSON))
//              Espera um Status OK
                .andExpect(status().isOk())
//              Espera os dados do primeiro elemento
                .andExpect(jsonPath("$[0].name", is(beerDTO.getName())))
                .andExpect(jsonPath("$[0].brand", is(beerDTO.getBrand())))
                .andExpect(jsonPath("$[0].type", is(beerDTO.getType().toString())));
    }

    @Test
//  Quando uma lista sem cerveja for chamado, retornar status OK
    void whenGETListWithoutBeersIsCalledThenOkStatusIsReturned() throws Exception {
        // given
//      Cria uma cerveja com valores Default
        BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();

        //when
//      Quando chamar o listAll, retornar a coleção
        when(beerService.listAll()).thenReturn(Collections.singletonList(beerDTO));

        // then
        mockMvc.perform(MockMvcRequestBuilders.get(BEER_API_URL_PATH)
                .contentType(MediaType.APPLICATION_JSON))
//              Espera um Status OK
                .andExpect(status().isOk());
    }

    @Test
//  Quando o DELETE é chamado com Id Valido, retorna status no Content
    void whenDELETEIsCalledWithValidIdThenNoContentStatusIsReturned() throws Exception {
        // given
        BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();

        //when
//      Não faça nada quando chamar o deleteById
        doNothing().when(beerService).deleteById(beerDTO.getId());

        // then
        mockMvc.perform(MockMvcRequestBuilders.delete(BEER_API_URL_PATH + "/" + beerDTO.getId())
                .contentType(MediaType.APPLICATION_JSON))
//              Espera um Status Is No Content
                .andExpect(status().isNoContent());
    }

    @Test
//  Quando o DELETE é chamado com Id invalido, retorna Status not Found
    void whenDELETEIsCalledWithInvalidIdThenNotFoundStatusIsReturned() throws Exception {
        //when
//      executar a exceção quando chamar deleteById
        doThrow(BeerNotFoundException.class).when(beerService).deleteById(INVALID_BEER_ID);

        // then
        mockMvc.perform(MockMvcRequestBuilders.delete(BEER_API_URL_PATH + "/" + INVALID_BEER_ID)
                .contentType(MediaType.APPLICATION_JSON))
//              Espera um Status Is Not Found
                .andExpect(status().isNotFound());
    }

    @Test
    void whenPATCHIsCalledToIncrementDiscountThenOKstatusIsReturned() throws Exception {
        QuantityDTO quantityDTO = QuantityDTO.builder()
                .quantity(10)
                .build();

        BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        beerDTO.setQuantity(beerDTO.getQuantity() + quantityDTO.getQuantity());

        when(beerService.increment(VALID_BEER_ID, quantityDTO.getQuantity())).thenReturn(beerDTO);

        mockMvc.perform(MockMvcRequestBuilders.patch(BEER_API_URL_PATH + "/" + VALID_BEER_ID + BEER_API_SUBPATH_INCREMENT_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(quantityDTO))).andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(beerDTO.getName())))
                .andExpect(jsonPath("$.brand", is(beerDTO.getBrand())))
                .andExpect(jsonPath("$.type", is(beerDTO.getType().toString())))
                .andExpect(jsonPath("$.quantity", is(beerDTO.getQuantity())));
    }

//    @Test
//    void whenPATCHIsCalledToIncrementGreatherThanMaxThenBadRequestStatusIsReturned() throws Exception {
//        QuantityDTO quantityDTO = QuantityDTO.builder()
//                .quantity(30)
//                .build();
//
//        BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
//        beerDTO.setQuantity(beerDTO.getQuantity() + quantityDTO.getQuantity());
//
//        when(beerService.increment(VALID_BEER_ID, quantityDTO.getQuantity())).thenThrow(BeerStockExceededException.class);
//
//        mockMvc.perform(patch(BEER_API_URL_PATH + "/" + VALID_BEER_ID + BEER_API_SUBPATH_INCREMENT_URL)
//                .contentType(MediaType.APPLICATION_JSON)
//                .con(asJsonString(quantityDTO))).andExpect(status().isBadRequest());
//    }

//    @Test
//    void whenPATCHIsCalledWithInvalidBeerIdToIncrementThenNotFoundStatusIsReturned() throws Exception {
//        QuantityDTO quantityDTO = QuantityDTO.builder()
//                .quantity(30)
//                .build();
//
//        when(beerService.increment(INVALID_BEER_ID, quantityDTO.getQuantity())).thenThrow(BeerNotFoundException.class);
//        mockMvc.perform(patch(BEER_API_URL_PATH + "/" + INVALID_BEER_ID + BEER_API_SUBPATH_INCREMENT_URL)
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(asJsonString(quantityDTO)))
//                .andExpect(status().isNotFound());
//    }
//
//    @Test
//    void whenPATCHIsCalledToDecrementDiscountThenOKstatusIsReturned() throws Exception {
//        QuantityDTO quantityDTO = QuantityDTO.builder()
//                .quantity(5)
//                .build();
//
//        BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
//        beerDTO.setQuantity(beerDTO.getQuantity() + quantityDTO.getQuantity());
//
//        when(beerService.decrement(VALID_BEER_ID, quantityDTO.getQuantity())).thenReturn(beerDTO);
//
//        mockMvc.perform(patch(BEER_API_URL_PATH + "/" + VALID_BEER_ID + BEER_API_SUBPATH_DECREMENT_URL)
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(asJsonString(quantityDTO))).andExpect(status().isOk())
//                .andExpect(jsonPath("$.name", is(beerDTO.getName())))
//                .andExpect(jsonPath("$.brand", is(beerDTO.getBrand())))
//                .andExpect(jsonPath("$.type", is(beerDTO.getType().toString())))
//                .andExpect(jsonPath("$.quantity", is(beerDTO.getQuantity())));
//    }
//
//    @Test
//    void whenPATCHIsCalledToDEcrementLowerThanZeroThenBadRequestStatusIsReturned() throws Exception {
//        QuantityDTO quantityDTO = QuantityDTO.builder()
//                .quantity(60)
//                .build();
//
//        BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
//        beerDTO.setQuantity(beerDTO.getQuantity() + quantityDTO.getQuantity());
//
//        when(beerService.decrement(VALID_BEER_ID, quantityDTO.getQuantity())).thenThrow(BeerStockExceededException.class);
//
//        mockMvc.perform(patch(BEER_API_URL_PATH + "/" + VALID_BEER_ID + BEER_API_SUBPATH_DECREMENT_URL)
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(asJsonString(quantityDTO))).andExpect(status().isBadRequest());
//    }
//
//    @Test
//    void whenPATCHIsCalledWithInvalidBeerIdToDecrementThenNotFoundStatusIsReturned() throws Exception {
//        QuantityDTO quantityDTO = QuantityDTO.builder()
//                .quantity(5)
//                .build();
//
//        when(beerService.decrement(INVALID_BEER_ID, quantityDTO.getQuantity())).thenThrow(BeerNotFoundException.class);
//        mockMvc.perform(patch(BEER_API_URL_PATH + "/" + INVALID_BEER_ID + BEER_API_SUBPATH_DECREMENT_URL)
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(asJsonString(quantityDTO)))
//                .andExpect(status().isNotFound());
//    }
}
