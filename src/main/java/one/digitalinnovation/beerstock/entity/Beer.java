package one.digitalinnovation.beerstock.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import one.digitalinnovation.beerstock.enums.BeerType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

//Gera Getters, Setters, equals e hashCode automaticamente (Lombok)
@Data
//Descreve a entidade do JPA fazendo mapeamento (atributos das tabelas do banco de dados)
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Beer {

//  Para cadastro no banco de dados
    @Id
//   Tipo Identity
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

//  Para não poder ser nulo e unico no banco de dados
    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String brand;

    @Column(nullable = false)
//  Toda cerveja terá um limite a ser incrementado
    private int max;

    @Column(nullable = false)
//  Uma cerveja só poderá ser incrementada se quantity <= max
    private int quantity;

//
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BeerType type;


}
