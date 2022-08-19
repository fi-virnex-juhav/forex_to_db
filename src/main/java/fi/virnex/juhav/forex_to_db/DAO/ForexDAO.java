package fi.virnex.juhav.forex_to_db.DAO;

import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.TimeZone;

import com.fasterxml.jackson.databind.ObjectMapper;

import fi.virnex.juhav.forex_to_db.DTO.ForexRequestDTO;
import fi.virnex.juhav.forex_to_db.DTO.ForexResponseDTO;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.net.URISyntaxException;

// apikey = "xxx";  // apikey on 2022-06-20

// only static methods and final constants on class level here

public class ForexDAO {

	private static final ObjectMapper objectMapper = new ObjectMapper();
	
	// example old apikey = "xxx";

	private static final String EXCEPTION_HEAD_JSON = "{Exception:";
	private static final String ERROR_HEAD_JSON = "{\"message\":";

	private static final String EXCEPTION_TAIL_JSON = "}";

	private static final String requestUrlTemplate = "http://api.apilayer.com/exchangerates_data/convert?amount=<fromAmount>&from=<fromCurrency>&to=<toCurrency>"; // &apikey=<apikey>";

	private static final String getUrlString(ForexRequestDTO forexRequestDTO, String apikey) {

		String url = requestUrlTemplate
				.replace("<fromAmount>",  Integer.toString( forexRequestDTO.getFromAmount()))
				.replace("<fromCurrency>", forexRequestDTO.getFromCurrency() )
				.replace("<toCurrency>", forexRequestDTO.getToCurrency());

		//		.replace("<apikey>", apikey );		// moved to header

		return url;
	}

	public static String getCurrencyExchangeJson(int amount, String from, String to, String apikey ) {
		OkHttpClient client = new OkHttpClient().newBuilder().build();

		String urlTemplate = "https://api.apilayer.com/exchangerates_data/convert?amount=<amount>&from=<from>&to=<to>";
		String urlString = urlTemplate
				.replace("<amount>", Integer.toString(amount))
				.replace("<from>", from )
				.replace("<to>", to );

		Request request = new Request.Builder()
				.url(urlString)
				.addHeader("apikey", apikey )
				//.addHeader("apikey", "xxx")
				.method("GET", null)
				.build();

		Response response;
		try {
			response = client.newCall(request).execute();
			String responseBody = response.body().string();
			System.out.println( "+++ responseBody : " + responseBody );

			return responseBody;

		} catch (Exception e) {				// (IOException e) {
			System.out.println("--- Exception in ForexDAO.getCurrencyExchangeJson() : " + e.toString());
			e.printStackTrace();
			return EXCEPTION_HEAD_JSON + e.toString() + EXCEPTION_TAIL_JSON; 
		}
	}
	
	// private static ForexResponseDTO forexResponseDTO = new ForexResponseDTO();

	private static final LocalDateTime getDateTimeFromTimestamp(long timestamp) {
		if (timestamp == 0)
			return null;
		return LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp), 
				TimeZone.getDefault().toZoneId());
	}

	private static final String getDateTimeAsString(LocalDateTime localDateTime) {
		return 		localDateTime.getYear() 
				+ "-" + IfLessThan10thenPrefix0(localDateTime.getMonthValue())
				+ "-" + IfLessThan10thenPrefix0(localDateTime.getDayOfMonth())
				+ " " + IfLessThan10thenPrefix0(localDateTime.getHour())
				+ ":" + IfLessThan10thenPrefix0(localDateTime.getMinute())
				+ ":" + IfLessThan10thenPrefix0(localDateTime.getSecond());
	}

	// convert e.g. "7" to "07" for month/dayOfMonth/hour/min/seconds
	private static final String IfLessThan10thenPrefix0(int h) {	
		if ( h < 10 ) {
			return "0" + h;		
		}
		return "" + h;
	}



	// Beef of the burger {
	
	// return either a ForexResponseDTO object or an error String in json format.

	public static final Optional<Object> fetch( 
			ForexRequestDTO forexRequestDTO,
			String apikey) 
					throws ParseException
					, URISyntaxException
					, Exception {

		System.out.println("ForexDAO.fetch() started with apikey:" + apikey);

		String json = null;

		json = getCurrencyExchangeJson( forexRequestDTO.getFromAmount(), forexRequestDTO.getFromCurrency(), forexRequestDTO.getToCurrency(), apikey);
		
		ForexDataFromJson forexDataFromJson = new ForexDataFromJson(json, objectMapper);
		
		if (forexDataFromJson.isSuccessFromApi() == false ) {
			return Optional.of( "{ ExternalApiReturnedSuccess: " +  false + "}" );
		}
		
		if ( forexDataFromJson.isJsonParsingSuccess() == false ) {
			return	Optional.of( 
					"{ " 
					+ "ExceptionWhileParsingJson: " + forexDataFromJson.getNameOfException() + " ,"
					+ "json: " + forexDataFromJson.getJson()
					+ " }" 
					);
		}

		System.out.println("+++ json: " + forexDataFromJson.getJson() );

		ForexResponseDTO forexResponseDTO = forexDataFromJson.getForexResponseDTO();

		System.out.println("+++ response : " + forexResponseDTO.toJson() );

		return Optional.of(forexResponseDTO);

	}

	// Beef of the burger }

}
