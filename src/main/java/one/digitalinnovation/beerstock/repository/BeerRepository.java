package one.digitalinnovation.beerstock.repository;

import one.digitalinnovation.beerstock.entity.Beer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// Tem a responsabilidade de conversar com o banco de dados
// Criar, Atualizar, Deletar, Buscar ...
public interface BeerRepository extends JpaRepository<Beer, Long> {

    Optional<Beer> findByName(String name);
}
