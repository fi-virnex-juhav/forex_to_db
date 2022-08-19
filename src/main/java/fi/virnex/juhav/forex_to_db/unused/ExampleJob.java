package fi.virnex.juhav.forex_to_db.unused;
/*
package org.javacodegeeks;

import org.quartz.Job;

import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;

import net.pwall.json.JSONSimple;

import java.io.FileInputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

public class ExampleJob implements Job {
	
	// params.apikey=xxx in application.properties

	private static final String JSON = 
			"{ " 
					+ "\"" + "from" + "\"" + ": " + "\"" + "USD" + "\"" + ","
					+ "\"" + "to"   + "\"" + ": " + "\"" + "NOK" + "\"" + "," 
					+ "\"" + "rate" + "\"" + ": " + 7.0 
					+ " }";
	
	private static final Properties applicationProperties = getApplicationProperties();

	private static final Properties getApplicationProperties() {
	  Properties properties = new Properties();
	  try {
		  //InputStream input = QuartzJobParamExample.class.getClassLoader().getResourceAsStream("application.properties");
		  InputStream input = ExampleJob.class.getClassLoader().getResourceAsStream("application.properties");
		  properties.load(input);
		  input.close();
	  } catch (Exception e) {
		  System.out.println("ExampleJob.getApplicationProperties() failed loading file application.properties: " + e);		  
	  }
	  return properties;
	}
	

	@Override
	public void execute(JobExecutionContext jobExecutionContext) {
		System.out.println("Job executed at: " + LocalDateTime.now().toString());
		JobDataMap dataMap = jobExecutionContext.getJobDetail().getJobDataMap();

		String apikey = applicationProperties.getProperty("forex_api.apikey");
		
		apikey = "DUMMY";		// !!! !!! !!!
		
		System.out.println("apikey: " + apikey);
		
		ForexRequestDTO forexRequestDTO = new ForexRequestDTO();
		forexRequestDTO.setFromAmount(1);
		forexRequestDTO.setFromCurrency("EUR");
		forexRequestDTO.setToCurrency("USD");
		
		System.out.println(forexRequestDTO.toString());
		
		try {
			System.out.println("before fetch");
			printSomething();
			Optional<Object> response = ForexDAO.fetch(forexRequestDTO, apikey);
			printSomething();
			System.out.println("after fetch");
			if (response.isPresent()) {
				if ( ((Object) response.get()).getClass() == org.javacodegeeks.ForexResponseDTO.class ) {
				   ForexResponseDTO forexResponseDTO = (ForexResponseDTO) response.get();
				   System.out.println(forexResponseDTO.toForexRateJsonString());
				} else {
					System.out.println("Error: " + response.toString());
				}
			} else {
				System.out.println("Error: " + response.toString());
			}
		} catch (Exception e1) {
			System.out.println("Exception " + e1 + " in ExampleJob during ForexDAO.fetch()");
		}
				
		// System.out.println("before parse");
		// Object o = JSONSimple.parse(JSON);
		// System.out.println("after parse");
		// System.out.println(o.toString());
		
		// System.out.println("User.dir: " + System.getProperty("user.dir"));

		try {
			@SuppressWarnings("unchecked")
			Map<String,?> map = (Map<String, ?>) JSONSimple.parse(JSON);
			System.out.println( map.get("from"));
			System.out.println( map.get("to"));
			System.out.println( map.get("rate"));
		} catch (Exception e) {
			System.out.println("Exception " + e + " parsing JSON " + JSON);
		}
		//fetch parameters from JobDataMap
		String param1 = dataMap.getString("PARAM_1_NAME");
		String param2 = dataMap.getString("PARAM_2_NAME");
		String param3 = dataMap.getString("PARAM_3_NAME");
		
		// String user_dir = System.getProperty("user.dir");
		// System.out.println("System property user.dir=" + user_dir);

		System.out.println("1. parameter value : " + param1);
		System.out.println("2. parameter value : " + param2);
		System.out.println("3. parameter value : " + param3);

	}

	private void printSomething() {
		System.out.println("printing something using a method outside of job execute");
	}

}
*/
