package fi.virnex.juhav.forex_to_db.non_web_service;

import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.TimeZone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import fi.virnex.juhav.forex_to_db.ForexToDbApplication;
import fi.virnex.juhav.forex_to_db.DAO.ForexDAO;
import fi.virnex.juhav.forex_to_db.DTO.ForexRequestDTO;
import fi.virnex.juhav.forex_to_db.DTO.ForexResponseDTO;
import fi.virnex.juhav.forex_to_db.model.ForexRate;
import fi.virnex.juhav.forex_to_db.repository.IForexRepository;

@Component
public class NonWebService {
	
	private long totalNumberOfFailedQueries = 0;
	
	private final void add1ToTotalNumberOfFailedQueries() {
	// avoid problem around the year 3000+ ;)
	  if ( totalNumberOfFailedQueries < Long.MAX_VALUE ) {
		  totalNumberOfFailedQueries++;
	  } else {
	  	  System.out.println("class NonWebService resetting totalNumberOfFailedQueries from " + totalNumberOfFailedQueries + " to 0 to avoid overflow.");
	  	  totalNumberOfFailedQueries = 0;
	  }
	}
		
	private long totalNumberOfSuccessfulQueries = 0;	
	
	private final void add1ToTotalNumberOfSuccessfulQueries() {
	// avoid problem around the year 3000+ ;)
		  if ( totalNumberOfSuccessfulQueries < Long.MAX_VALUE ) {
			  totalNumberOfSuccessfulQueries++;
		  } else {
		  	  System.out.println("class NonWebService resetting totalNumberOfSuccessfulQueries from " + totalNumberOfSuccessfulQueries + " to 0 to avoid overflow.");
		  	  totalNumberOfSuccessfulQueries = 0;
		  }
	}
	
    // A reduced set of currency conversion combinations.
    // This is to avoid exceeding the daily/monthly limit of free queries.
    // apilayer.com forex exchange rate data web API has limits for free of charge use. 
    private static String fromCurrencies[] = {"EUR", "USD"};	// {"USD", "USD", "EUR", "EUR", "SEK", "SEK"};
	private static String toCurrencies[]   = {"USD", "EUR"};	// {"EUR", "SEK", "USD", "SEK", "USD", "EUR"};
    
    
	@Autowired
	private IForexRepository repository;
	
	// applicationProperties was used to get the apikey, but not needed anymore.
    // private static final Properties applicationProperties = getApplicationProperties();
	// Left here as comment for possible need of a model for other applications.
    /*
	// getApplicationProperties() was used to get the apikey, not needed anymore.
	private static final Properties getApplicationProperties() {
	  Properties properties = new Properties();
	  try {
		  InputStream input = ForexToDbApplication.class.getClassLoader().getResourceAsStream("application.properties");
		  properties.load(input);
		  input.close();
	  } catch (Exception e) {
		  System.out.println("ExampleJob.getApplicationProperties() failed loading file application.properties: " + e);		  
	  }
	  return properties;
	}
	*/
	
    private static boolean firstRunOfNonWebService = true;
    	
	// If the DB table is empty, add there 3 fabricated dummy data rows.
	// This is to enable demo runs without exceeding the daily/monthly
    // free limit of forex data API queries while working on development.
	private void initForexDbWithDummyData() {
		
		if ( getNumberOfStoredForexDataRows() > 0 ) {
			return;
		}
		
		String[] rateDates  = { "2022-01-01 12:00:00", "2022-01-03 12:00:00", "2022-01-07 12:00:00" };
		String fromCurrency = "EUR";
		String toCurrency = "NOK";
		double[] rates = { 10.1, 10.3, 10.7 };
		
		for (int i= 0; i < 3 ; i++) {
			ForexRate forexRateBeforeSave = new ForexRate();
			
			forexRateBeforeSave.setRateDate( rateDates[i] );
			forexRateBeforeSave.setFromCurrency( fromCurrency );
			forexRateBeforeSave.setToCurrency( toCurrency );
			forexRateBeforeSave.setExchangeRate( rates[i] );
			
			// at save DB generates the id and save returns the row object with the id.
			ForexRate forexRateAfterSave = repository.save( forexRateBeforeSave );
			System.out.println("Added dummy row to DB: " + forexRateAfterSave.toJsonStringWithId() );
		}
		
		System.out.println("DB rows after initForexDbWithDummyData: ");
		System.out.println( getAllForexDataRows().toString() );
	}
	
	public void collectAndSaveForexDataEveryHour(String apikey, long runMaxHoursCount, long totalFailedQueriesLimitToStopApplication) {
		  
    	long periodicRepetitionCount = 0;
    	
    	boolean immediateFirstCollection = true;
    	
    	// if runMaxHoursCount == -1 then repeat forever.
    	while ( (runMaxHoursCount == -1) || (periodicRepetitionCount < runMaxHoursCount )) {
    	    		
       		// The first collection is run immediately and not counted.
    		// Only periodic repetitions are counted.
    		
    		collectAndSaveOnceEachCurrencyCombination(apikey, totalFailedQueriesLimitToStopApplication);	// first collection immediately independent of local time
    		
    		if ( immediateFirstCollection ) {
    			immediateFirstCollection = false;
    		} else {
    			// Avoid problem around year 3000+ ;)
    			if ( periodicRepetitionCount < Long.MAX_VALUE ) {
    				periodicRepetitionCount++;
    			} else {
    				System.out.println("ForexToDbApplication.collectAndSaveForexDataEveryHour() resetting periodicRepetitionCount from " + periodicRepetitionCount + " to 0 to avoid value overflow");
     				periodicRepetitionCount = 0;
    			}
    		}
    		
    		// sleep only if collection will be repeated at least once more.
     		if ( (runMaxHoursCount == -1) || (periodicRepetitionCount < runMaxHoursCount) ) {
     			try {
     				// sleep until next full hour.
     				Thread.sleep( getSleepTimeToNextFullHourInMs() );	
     			} catch (InterruptedException ie) {
     				System.out.println("Exception " + ie + " at " + localDateTimeToHhMmSs( LocalDateTime.now() ));
     			}
     		}
     		
     	} // end of while
    }   
   
	private void collectAndSaveOnceEachCurrencyCombination(String apikey, long totalFailedQueriesLimitToStopApplication) {
    	
    	for (int i = 0; i < fromCurrencies.length ; i++ ) {
    	
    		if ( totalNumberOfFailedQueries < totalFailedQueriesLimitToStopApplication ) {
    			fetchAndSaveForexDataRow(fromCurrencies[i], toCurrencies[i], apikey);
    		} else {
    			break;	// limit load to apilayer forex data API web site when queries fail for any reason. 
    		}
    	
    	}
    	System.out.println();
    	System.out.println( "forex_db database table rows:");
    	System.out.println( getAllForexDataRows().toString().replaceAll("}",  "}" + System.lineSeparator() ) );
    	System.out.println();
    }
	
	public void fetchAndSaveForexDataRow(String fromCurrency, String toCurrency, String apikey) {  // (e.g. from "EUR" to "USD")
		
		// Do not load apilayer.com web forex exchange rate web site with unnecessary queries known to fail.
		if ( apikey.equals("DUMMY") ) {
			add1ToTotalNumberOfFailedQueries();
			return;
		}
		
		boolean fetchAndSaveForexDataRowSuccesfull = false;
		
		// to be removed from any production version, now it helps to see if real or dummy apikey is in use !!!
		System.out.println("NonWebService.fetchAndSaveForexData read docker environment variable apikey: " + apikey);

		// insert some fabricated rows to DB table to enable demo - also with dummy or invalid apikey.
		if ( firstRunOfNonWebService && (getNumberOfStoredForexDataRows() == 0 )) {
			
			initForexDbWithDummyData();
				
			firstRunOfNonWebService = false;
		}
		
		ForexRequestDTO forexRequestDTO = new ForexRequestDTO();
		forexRequestDTO.setFromAmount(1);
		forexRequestDTO.setFromCurrency(fromCurrency);
		forexRequestDTO.setToCurrency(toCurrency);
		
		// System.out.println(forexRequestDTO.toString());
		
		try {
			Optional<Object> response = ForexDAO.fetch(forexRequestDTO, apikey);

			if (response.isPresent()) {
				if ( ((Object) response.get()).getClass() == fi.virnex.juhav.forex_to_db.DTO.ForexResponseDTO.class ) {
				   ForexResponseDTO forexResponseDTO = (ForexResponseDTO) response.get();
				   
				   ForexRate forexRateBeforeSave = new ForexRate();

				   // now before save we have no id column value for the row
				   forexRateBeforeSave.setRateDate( forexResponseDTO.getRateDate() );
				   forexRateBeforeSave.setFromCurrency( forexResponseDTO.getFromCurrency() );
				   forexRateBeforeSave.setToCurrency( forexResponseDTO.getToCurrency() );
				   forexRateBeforeSave.setExchangeRate(forexResponseDTO.getExchangeRate() );
				   
				   ForexRate forexRateAfterSave = repository.save(forexRateBeforeSave);
				   
				   // now after save we have id column value for the row set by the repository (DB).
				   
				   fetchAndSaveForexDataRowSuccesfull = true;
				   
				   add1ToTotalNumberOfSuccessfulQueries();
				   				   
				   System.out.println("new row after fetchAndSaveForexData: " + forexRateAfterSave.toJsonStringWithId());
				} else {
					System.out.println("Error: " + response.toString());
				}
			} else {
				System.out.println("Error: " + response.toString());
			}
		} catch (Exception e1) {
			System.out.println("Exception " + e1 + " in ExampleJob during ForexDAO.fetch()");
		}
		
		if ( fetchAndSaveForexDataRowSuccesfull == false ) {
			add1ToTotalNumberOfFailedQueries();
		}

	}
		
	public List<ForexRate> getAllForexDataRows() {
	    	return repository.findAll();
	}
	
	public long getNumberOfStoredForexDataRows() {
		List<ForexRate> forexDataRowsFromDb = repository.findAll();
		if ( forexDataRowsFromDb == null ) return 0;	// this should never happen, instead empty list.
		return forexDataRowsFromDb.size();
	}
    
    private String localDateTimeToHhMmSs(LocalDateTime localDateTime) { 
    	String time = dTo0dIfArgumentLessThan10(localDateTime.getHour()) + ":" + dTo0dIfArgumentLessThan10(localDateTime.getMinute()) + ":" + dTo0dIfArgumentLessThan10(localDateTime.getSecond());
    	return time;
    }
    
    //Get a String value of int h adding 0 in front in case h < 10.
    //Like hour format from h to hh.
    //This is used to format 1 digit (< 10) date/time values to 2 digit representation.
    //E.g. when day of month or month or hour equals 7 return 07 as java String.  
    private String dTo0dIfArgumentLessThan10(int h) { 
    	if ( h < 10 ) {
    		return "0" + h;
    	}
    	return "" + h;
    }
    
    private long getSleepTimeToNextFullHourInMs() { 

    	LocalDateTime now = LocalDateTime.now();
    	System.out.println("now: " + localDateTimeToHhMmSs(now));
        LocalDateTime oneHourLater = now.plusHours(1).minusMinutes(now.getMinute()).minusSeconds(now.getSecond()).minusNanos(now.getNano());
        System.out.println("next full hour: " + localDateTimeToHhMmSs(oneHourLater));
        Duration duration = Duration.between(now, oneHourLater);
        long sleepTime = duration.toMillis();
        long TotalSleepTimeSeconds = sleepTime/1000;
        long sleepTimeMinutes = TotalSleepTimeSeconds / 60;
        long sleepTimeSeconds = TotalSleepTimeSeconds % 60;     
    	System.out.println("sleep time to next full hh: " + sleepTimeMinutes + " min " + sleepTimeSeconds + " seconds");
    	return sleepTime;  

    }

}
