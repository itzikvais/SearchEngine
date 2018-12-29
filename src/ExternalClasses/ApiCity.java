package ExternalClasses;

import Parse.IParse;
import Parse.ParseNumber;
import eu.fayder.restcountries.v1.domain.Country;
import eu.fayder.restcountries.v1.rest.CountryService;

import java.util.List;

public class ApiCity {
    private CountryService countryService;
    private Country country;
    private String currency;
    private String population;
    private String city;

    public ApiCity(String city) {
        this.city = city;
        countryService = CountryService.getInstance();
        List<Country> countryList = countryService.getByCapital(city);
        if(countryList == null || countryList.size() == 0){
            country = null;
        }
        else {
            country = countryList.get(0);
        }
    }

    public String getCountry() {
        if (country != null)
            return country.getName();
        return null;
    }

    public String getCurrency() {
        if (country != null && currency == null){
            currency = country.getCurrencies().get(0);
        }
        return currency;
    }

    public String getPopulation() {
        if (country != null && population == null){
            Integer populationNum = country.getPopulation();
            IParse numParser = new ParseNumber(populationNum.toString(),null);
            String populationNumAfterParse = numParser.parse();
            population = populationNumAfterParse;
        }
        return population;
    }

    @Override
    public String toString() {
        return  city + "," + getCountry() + "," + getCurrency() + "," + getPopulation() + "#";
    }
}