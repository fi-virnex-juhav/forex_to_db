package fi.virnex.juhav.forex_to_db.DTO;

import java.text.SimpleDateFormat;

/*
Example of a json response from the API on web:

{
"success": true,
"query": {
	"from": "EUR",
	"to": "USD",
	"amount": 1
},
"info": {
	"timestamp": 1652957583,
	"rate": 1.052443
},
"date": "2022-05-19",
"result": 1.052443
}
 */

// changed java Date to String everywhere as it causes errors, parsing+DB


public class ForexResponseDTO {
	
    // for parsing and printing current date & time
	
	private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
	static {
		simpleDateFormat.setLenient(false);		//does this work always with @Autowired?
	}
	
	private static final SimpleDateFormat YYYYMMDD = new SimpleDateFormat("yyyyMMdd");
	private static final SimpleDateFormat YYYY_MM_DD = new SimpleDateFormat("yyyy-MM-dd");
	
	private static final SimpleDateFormat YYYYMMDD_HHMMSS = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
	private static final SimpleDateFormat YYYY_MM_DD_HHMMSS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	static {
		YYYYMMDD.setLenient(false);				// do NOT roll over when date out of range
		YYYY_MM_DD.setLenient(false);
		YYYYMMDD_HHMMSS.setLenient(false);
		YYYY_MM_DD_HHMMSS.setLenient(false);
	}
	
	private String rateDate;
	
	// private Date	rateDate;

	private int		fromAmount;		// amount of "from currency"	[1, 2, 3, ... ]
	private String	fromCurrency;	// id of "from currency":	["USD", "EUR", "SEK", ...]
	private double	toAmount;		// amount of "to currency"	e.g. 1.37
	private String	toCurrency;		// id of "to currency":		["EUR", "SEK", "USD", ...]
	private double	exchangeRate;	// amount of "to currency" for 1 unit of "from currency"

	public static final String toCurrencyString(double d) {
		String dAsString = Double.toString(d);
		int dotIndex = dAsString.indexOf(".");
		int length = dAsString.length();	
		if ( d >= 1.0 ) {
			return dAsString.substring(0,Math.min(dotIndex+3, length));
		}
		
		if ( dAsString.startsWith("0.") == false ) {
			return dAsString;
		}
		
		int index1 = dotIndex + 1;
		if ( index1 >= length ) {
			return dAsString;
		}

		while ( dAsString.charAt(index1) == '0' ) {
			if ( index1 + 1 < length) {
				index1++;
			}
			else {
				return dAsString;
			}
		}
		return dAsString.substring(0, Math.min(index1 + 3, length) ); 
	}
	
	public ForexResponseDTO() {}

	public String getRateDate() {
		return rateDate;
	}
	
	public void setRateDate(String rateDate) {
		this.rateDate = rateDate; 
	}
	
	/*
	public Date getRateDate() {
		return rateDate;
	}
	
	public String getRateDate(SimpleDateFormat simpleDateFormat) {
		return simpleDateFormat.format(rateDate);
	}

	public void setRateDate(Date rateDate) {
		this.rateDate = rateDate;
	}
	
	// e.g.YYYYMMDD_HHMMSS
	
	public void setRateDate(String rateDate, SimpleDateFormat simpleDateFormat) {
		simpleDateFormat.setLenient(false);
		try {
			this.rateDate = simpleDateFormat.parse(rateDate);
		} catch (Exception e) {
			System.out.println("ForexResponseDTO.setRateDate failed parsing String : " + rateDate);
			System.exit(1);		// no mercy, to protect database from corruption
		}
	}
	
	*/

	public int getFromAmount() {
		return fromAmount;
	}

	public void setFromAmount(int fromAmount) {
		this.fromAmount = fromAmount;
	}

	public String getFromCurrency() {
		return fromCurrency;
	}

	public void setFromCurrency(String fromCurrency) {
		this.fromCurrency = fromCurrency;
	}

	public double getToAmount() {
		return toAmount;
	}

	public void setToAmount(double toAmount) {
		this.toAmount = toAmount;
	}

	public String getToCurrency() {
		return toCurrency;
	}

	public void setToCurrency(String toCurrency) {
		this.toCurrency = toCurrency;
	}

	public double getExchangeRate() {
		return exchangeRate;
	}

	public void setExchangeRate(double exchangeRate) {
		this.exchangeRate = exchangeRate;
	}

	@Override
	public String toString() {
		return "ForexResponseDTO [rateDate=" + rateDate + ", fromAmount=" + fromAmount + ", fromCurrency=" + fromCurrency
				+ ", toAmount=" + toAmount + ", toCurrency=" + toCurrency + ", exchangeRate=" + exchangeRate + "]";
	}
	
	public String toJson() {
		return "{"
				  + "\"" + "rate_date" + "\"" + ":" + "\"" + rateDate + "\"" 
			+ "," + "\"" + "from_amount" + "\"" + ":" + fromAmount
			+ "," + "\"" + "from" + "\"" + ":" + "\"" + fromCurrency + "\""
 			+ "," + "\"" + "to_amount" + "\"" + ":" + toAmount
			+ "," + "\"" + "to" + "\"" + ":" + "\"" + toCurrency + "\""
			+ "," + "\"" + "rate" + "\"" + ":" + exchangeRate
			+ "}";
	}

}
