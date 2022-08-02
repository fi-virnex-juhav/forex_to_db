package fi.virnex.juhav.forex_to_db.DAO;

import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;

import fi.virnex.juhav.forex_to_db.DTO.ForexRequestDTO;
import fi.virnex.juhav.forex_to_db.DTO.ForexResponseDTO;
import net.pwall.json.JSONSimple;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.net.URL;

// apikey = "vGLeIBxS5cqpW6nbrEEIIbzWGmmCAbEF";  // apikey on 2022-06-20

// only static methods and final constants on class level here

public class ForexDAO {


	// example old apikey = "vGLeIBxS5cqpW6nbrEEIIbzWGmmCAbEF";

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
				//.addHeader("apikey", "vGLeIBxS5cqpW6nbrEEIIbzWGmmCAbEF")
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

	//OLD UNUSED, MAYBE USEFUL LATER SOMEWHERE
	private static Optional<Object> parseServerError(String serverError) {

		Optional<Object> returnValueIfNull = Optional.ofNullable((Object) serverError);

		if (returnValueIfNull.isEmpty()) {
			String bug = "--- BUG1 outside of remote Exchange Rate API server: ForexDAO.parseServerError called with a null String";
			Optional<Object> optionalBug = Optional.of( (Object) bug);
			return optionalBug;
		}

		// my current API server side error:
		// ... "{"message":"You have exceeded your daily\/monthly API rate limit. Please review and upgrade your subscription plan at https:\/\/promptapi.com\/subscriptions to continue."}"

		String msg = serverError.replace("\"", "");
		msg = msg.replace(":", "");
		msg = msg.replace("{", "");
		msg = msg.replace("}", "");
		String expectedSubstring = "message";
		int index = msg.indexOf(expectedSubstring);
		if (index >= 0 ) {
			msg = msg.substring(index + expectedSubstring.length());
		}
		return Optional.of(msg);
	}

	private static ForexResponseDTO forexResponseDTO = new ForexResponseDTO();

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

	public static final Optional<Object> fetch( 
			ForexRequestDTO forexRequestDTO,
			String apikey) 
					throws ParseException
					, URISyntaxException
					, Exception {

		System.out.println("ForexDAO.fetch() started with apikey:" + apikey);

		String json = null;

		json = getCurrencyExchangeJson( forexRequestDTO.getFromAmount(), forexRequestDTO.getFromCurrency(), forexRequestDTO.getToCurrency(), apikey);

		if ( json == null ) {
			System.out.println("--- json == " + json);
			return Optional.of(json);
		} 

		if ( json.startsWith(EXCEPTION_HEAD_JSON) || json.startsWith(ERROR_HEAD_JSON)) {
			System.out.println("--- json : " + json );
			return Optional.of(json);
		}

		System.out.println("+++ json: " + json );

		// from here on any problem is in parsing of the json format	

		Optional<Object> response = Optional.ofNullable(null);

		try {
			// JsonParser springParser = JsonParserFactory.getJsonParser();

			@SuppressWarnings("unchecked")
			Map<String,?> map = (Map<String, ?>) JSONSimple.parse(json);

			boolean success = (boolean) map.get("success");

			Map<String, ?> query = (Map<String, ?>) map.get("query");

			String fromCurrency = (String) query.get("from");
			String toCurrency   = (String) query.get("to");
			int fromAmount = (int) query.get("amount");

			Map<String,?> info = (Map<String, ?>) map.get("info");

			// the app logic requires time hh:mm:ss information, so timestamp from forex API is converted to DateTime.
			// the json response from API contains only date (without time) and the timestamp that is used here.
			long timestamp = (int) info.get("timestamp");

			String rateDate = getDateTimeAsString(getDateTimeFromTimestamp(timestamp));

			BigDecimal bdExchangeRate = (BigDecimal) info.get("rate");

			double exchangeRate =  bdExchangeRate.doubleValue();

			// String dateString = (String) map.get("date");

			// System.out.println( "dateString : " + dateString );

			/*
			// DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

			Date date = dateFormat.parse(dateString);  
			 */

			// String rateDate = (String) map.get("date"); // there is no time information, timestamp used above

			BigDecimal bdToAmount = (BigDecimal) map.get("result");

			double toAmount = bdToAmount.doubleValue();	// to-amount

			forexResponseDTO.setRateDate(rateDate);
			forexResponseDTO.setFromAmount(fromAmount);
			forexResponseDTO.setFromCurrency(fromCurrency);
			forexResponseDTO.setToAmount(toAmount);
			forexResponseDTO.setToCurrency(toCurrency);	
			forexResponseDTO.setExchangeRate(exchangeRate);

			System.out.println("+++ response : " + forexResponseDTO.toJson() );

			response = Optional.of(forexResponseDTO);

		} catch (Exception e) {
			String jsonParsingErrorMessage = "Exception while parsing json in ForexDAO.fetch() : " + e.toString();
			System.out.println("--- " + jsonParsingErrorMessage);
			response = Optional.of( EXCEPTION_HEAD_JSON + e.toString() + EXCEPTION_TAIL_JSON );
		}

		return response;
	}

	// Beef of the burger }

}
