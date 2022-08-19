package fi.virnex.juhav.forex_to_db.DAO;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;

import java.net.URL;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;

import com.fasterxml.jackson.databind.ObjectMapper;
// import com.fasterxml.jackson.databind.json.JsonMapper;

import fi.virnex.juhav.forex_to_db.DTO.ForexResponseDTO;

// parsing of JSON-response String from the external forex API done here.

// possible json parsing error information is stored in the object.

public class ForexDataFromJson {

	String json = null;

	boolean successFromApi = false;

	boolean jsonParsingSuccess = false;

	// in case parsing of the json fails
	String nameOfException = null;

	ForexResponseDTO forexResponseDTO = null;

	// sample json from apilayer.com Exchange Rate API
	/* 
{
"success": true,
"query": {
    "from": "EUR",
    "to": "USD",
    "amount": 1
},
"info": {
    "timestamp": 1659023224,
    "rate": 1.016327
},
"date": "2022-07-28",
"result": 1.016327
}
	 */

	public ForexDataFromJson(String json, ObjectMapper objectMapper) {

		this.json = json;	// store input for possible json parsing error case

		JsonNode map = null;

		try {
			map = objectMapper.readValue(json, JsonNode.class);

			// JsonNode nameNode = node.get("name");

			successFromApi = (boolean) map.get("success").asText().toLowerCase().equals("true");

			JsonNode query = map.get("query");

			String fromCurrency = query.get("from").asText();
			String toCurrency   = query.get("to").asText();
			int fromAmount = query.get("amount").asInt();	// amount of from-currency

			JsonNode info = map.get("info");

			// the app logic requires time hh:mm:ss information, so timestamp from forex API is converted to DateTime.
			// the json response from API contains only date (without time) and the timestamp that is used here.
			long timestamp = info.get("timestamp").asLong();
			String rateDate = Jutil.getDateTimeAsString( Jutil.getDateTimeFromTimestamp(timestamp) );

			BigDecimal bdExchangeRate = (BigDecimal) info.get("rate").decimalValue();
			double exchangeRate =  bdExchangeRate.doubleValue();

			// the API gives only date without time - therefore timestamp above used instead
			String unusedDateString = (String) map.get("date").asText();

			BigDecimal toAmountInBigDecimal = (BigDecimal) map.get("result").decimalValue();
			double toAmount = toAmountInBigDecimal.doubleValue();	// to-amount

			forexResponseDTO = new ForexResponseDTO();

			forexResponseDTO.setRateDate(rateDate);
			forexResponseDTO.setFromAmount(fromAmount);
			forexResponseDTO.setFromCurrency(fromCurrency);
			forexResponseDTO.setToAmount(toAmount);
			forexResponseDTO.setToCurrency(toCurrency);	
			forexResponseDTO.setExchangeRate(exchangeRate);

			jsonParsingSuccess = true;
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			nameOfException = "JsonMappingException";	
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			nameOfException = "JsonProcessingException";
		} catch (Exception e) {
			nameOfException = e.getClass().getName();
		}
	}

	public String getJson() {
		return json;
	}

	public boolean isSuccessFromApi() {
		return successFromApi;
	}

	public boolean isJsonParsingSuccess() {
		return jsonParsingSuccess;
	}

	public String getNameOfException() {
		return nameOfException;
	}

	public ForexResponseDTO getForexResponseDTO() {
		return forexResponseDTO;
	}

	@Override
	public String toString() {
		return "ForexDataFromJson [forexResponseDTO=" + forexResponseDTO + ", successFromApi=" + successFromApi + ", jsonParsingSuccess="
				+ jsonParsingSuccess + ", nameOfException=" + nameOfException + ", json=" + json
				+ "]";
	}

}

