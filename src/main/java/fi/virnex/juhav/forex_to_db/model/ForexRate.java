package fi.virnex.juhav.forex_to_db.model;

import java.io.Serializable;
// import java.util.HashMap;
import java.util.LinkedHashMap; // I want to decide the order of key/value pairs shown
import java.util.Map;
// import java.text.ParseException;
// import java.text.SimpleDateFormat;
// import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

// import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "forex_rate")
public class ForexRate implements Serializable {
	
	
	// private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
	
	// String dateAsString = "01-03-2007 11:11:00";

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private long id;	
    
    @Column(name="rate_date", nullable = false)
    private String rateDate;			// lost much time without solution to parse date&time.
    // private Date rateDate;			// rateDate, time consuming problems with parsing ...

    @Column(name = "from_currency", nullable = false)
    private String fromCurrency;

    @Column(name = "to_currency", nullable = false)
    private String toCurrency;
    
    @Column(name = "exchange_rate", nullable = false)
    private double exchangeRate;
	
	// constructors
	
   
	public ForexRate() {}
	
	// toString variants

	public String toJsonStringNoId() {
		return "{"
				+ "\"" + "date" + "\"" + ":" + "\"" + rateDate + "\","
				+ "\"" + "from" + "\"" + ":" + "\"" + fromCurrency + "\","
				+ "\"" + "to"   + "\"" + ":" + "\"" + toCurrency   + "\","
				+ "\"" + "rate" + "\"" + ":" + exchangeRate 
				+ "}";
	}
	
    /*
	   
    {
      "date":"2022-07-05 00:00:00",	// intended format
      "from":"USD",
      "to":"SEK",
      "rate":10.0 
    }  
    
     */
	
	public String toJsonStringWithId() {
		return "{"
				+ "\"" + "id" + "\"" + ":" + id + ","
				+ "\"" + "date" + "\"" + ":" + "\"" + rateDate + "\","
				+ "\"" + "from" + "\"" + ":" + "\"" + fromCurrency + "\","
				+ "\"" + "to"   + "\"" + ":" + "\"" + toCurrency   + "\","
				+ "\"" + "rate" + "\"" + ":" + exchangeRate 
				+ "}";
	}
	
    /*
	   
    {
      "id":1,
      "date":"2022-07-05 00:00:00",	// intended format
      "from":"USD",
      "to":"SEK",
      "rate":10.0 
    }  
    
     */
	
	@Override
	public String toString() {
		
		return this.toJsonStringWithId();
	}
	
	/* 
	   work-around to still open null parameter problem with 
	   simple and short standard implementation of ForexCronController post end-point
	   with repository.save()
	*/
	
	public Map<String,?> toMap() {
		Map<String,Object> map = new LinkedHashMap<String,Object>();
		
		map.put("rate_date", rateDate);
		map.put("from_currency", fromCurrency);
		map.put("to_currency", toCurrency);
		map.put("exchange_rate", exchangeRate);
		
		System.out.println("ForexRate.toMap type info: key rate_date value class : " + map.get("rate_date").getClass() );
		System.out.println("ForexRate.toMap type info: key exchange_rate value class : " + map.get("exchange_rate").getClass() );
		
		return map;	
	}
	
	// getters & setters
	 
		public long getId() {
			return id;
		}

		public void setId(long id) {
			// System.out.println( "ForexRate.setId(" + id + ")" );
			this.id = id;
		}
	
	public String getRateDate() {
		return rateDate;
	}

	public void setRateDate(String rateDate) {
		// System.out.println( "ForexRate.setRateDate(" + rateDate + ")" );
		this.rateDate = rateDate;
	}

	public String getFromCurrency() {
		return fromCurrency;
	}

	public void setFromCurrency(String fromCurrency) {
		// System.out.println( "ForexRate.setFromCurrency(" + fromCurrency + ")" );
		this.fromCurrency = fromCurrency;
	}

	public String getToCurrency() {
		return toCurrency;
	}

	public void setToCurrency(String toCurrency) {
		// System.out.println( "ForexRate.setToCurrency(" + toCurrency + ")" );
		this.toCurrency = toCurrency;
	}

	public double getExchangeRate() {
		return exchangeRate;
	}

	public void setExchangeRate(double exchangeRate) {
		// System.out.println( "ForexRate.setExchangeRate(" + exchangeRate + ")" );
		this.exchangeRate = exchangeRate;
	}  
    
}

