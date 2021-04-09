package one.digitalinnovation.beerstock.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// Restorna um status de 400
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BeerAlreadyRegisteredException extends Exception{

    public BeerAlreadyRegisteredException(String beerName) {
        super(String.format("Beer with name %s already registered in the system.", beerName));
    }
}
