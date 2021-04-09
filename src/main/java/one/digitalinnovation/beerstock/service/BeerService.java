package one.digitalinnovation.beerstock.service;

import lombok.AllArgsConstructor;
import one.digitalinnovation.beerstock.dto.BeerDTO;
import one.digitalinnovation.beerstock.entity.Beer;
import one.digitalinnovation.beerstock.exception.BeerAlreadyRegisteredException;
import one.digitalinnovation.beerstock.exception.BeerNotFoundException;
import one.digitalinnovation.beerstock.exception.BeerStockExceededException;
import one.digitalinnovation.beerstock.mapper.BeerMapper;
import one.digitalinnovation.beerstock.repository.BeerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

// Indicar que está classe vai ser gerenciada pelo spring
// Para utiliza-la so precisa injeta-la onde desejar
@Service
//Inclui um construtor automaticamente (Lombok)
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class BeerService {

    private final BeerRepository beerRepository;
    private final BeerMapper beerMapper = BeerMapper.INSTANCE;

//  Criação de cerveja
    public BeerDTO createBeer(BeerDTO beerDTO) throws BeerAlreadyRegisteredException {
//      Verifica se a cerveja ja foi cadastrada no sistema
        verifyIfIsAlreadyRegistered(beerDTO.getName());
//      Quando cria uma cerveja, passa o padrao DTO validando os inputs
        Beer beer = beerMapper.toModel(beerDTO);
//      Mas é preciso converter para um formato que o repository conversa
        Beer savedBeer = beerRepository.save(beer);
        return beerMapper.toDTO(savedBeer);
    }

//  Busca por nome
    public BeerDTO findByName(String name) throws BeerNotFoundException {
        Beer foundBeer = beerRepository.findByName(name)
                .orElseThrow(() -> new BeerNotFoundException(name));
        return beerMapper.toDTO(foundBeer);
    }

//  Listagem
    public List<BeerDTO> listAll() {
        return beerRepository.findAll()
                .stream()
                .map(beerMapper::toDTO)
                .collect(Collectors.toList());
    }

//  Delete
    public void deleteById(Long id) throws BeerNotFoundException {
        verifyIfExists(id);
        beerRepository.deleteById(id);
    }

//  --------------------------------------------------------------------------------------------------
//  Metodos auxiliares
    private void verifyIfIsAlreadyRegistered(String name) throws BeerAlreadyRegisteredException {
//      Busca no banco de dados
        Optional<Beer> optSavedBeer = beerRepository.findByName(name);
//      Se estiver presente, lança uma excessão
        if (optSavedBeer.isPresent()) {
            throw new BeerAlreadyRegisteredException(name);
        }
    }

//  Verificação se ja existe
    private Beer verifyIfExists(Long id) throws BeerNotFoundException {
        return beerRepository.findById(id)
                .orElseThrow(() -> new BeerNotFoundException(id));
    }

//  Incremento
    public BeerDTO increment(Long id, int quantityToIncrement) throws BeerNotFoundException, BeerStockExceededException {
        Beer beerToIncrementStock = verifyIfExists(id);
        int quantityAfterIncrement = quantityToIncrement + beerToIncrementStock.getQuantity();
        if (quantityAfterIncrement <= beerToIncrementStock.getMax()) {
            beerToIncrementStock.setQuantity(beerToIncrementStock.getQuantity() + quantityToIncrement);
            Beer incrementedBeerStock = beerRepository.save(beerToIncrementStock);
            return beerMapper.toDTO(incrementedBeerStock);
        }
        throw new BeerStockExceededException(id, quantityToIncrement);
    }
}
