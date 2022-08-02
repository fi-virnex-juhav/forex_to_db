Virnex Oy / juha valimaki 2022-07-17

# User Guide to store hourly Currency Exchange Rate data to a database by forex_to_db application

### Description of the forex_to_db application

An external API provides Currency Exchange Rate data in JSON format.

forex_to_db application performs hourly queries to the forex API and stores the data in a database (DB).

A dockerized postgreSQL database stores the data in a persistent volume on the local host file system.

forex_to_db is a dockerized java / Spring Boot application.

A set of currency combinations is stored in the source code, but the set can be made dynamic in a later version:
EUR to USD, EUR to SEK, USD to EUR, USD to SEK, SEK to USD, SEK to EUR for example.

As the Forex API has limits for cost free queries per day & per month, I reduced later the currency combinations to 2:
EUR to USD and USD to EUR 

In case the DB table forex_rate is empty, it will be initialized by 3 rows of dummy fake data for EUR to NOK exchange rates.

Every collection round queries once the exchange rate for every currency combination.

The first collection round is performed immediately at application start-up independent of the local time.

The following collection rounds are run at each next full hour (hh:00:00)

Note: 
The DB table column rate_date with date and time "yyyy-MM-dd hh:mm:ss" is based on the time stamp returned by the Forex API.
The rate_date column value depends on the time the Forex API has updated the row in their own DB.
Even our queries run hourly, the rate_date column does not match our query time.

Example: rows of data stored in the forex_db database table forex_rate:
{"id":1,"date":"2022-01-01 12:00:00","from":"EUR","to":"NOK","rate":10.1}       // 1
{"id":2,"date":"2022-01-03 12:00:00","from":"EUR","to":"NOK","rate":10.3}       // 1
{"id":3,"date":"2022-01-07 12:00:00","from":"EUR","to":"NOK","rate":10.7}       // 1
{"id":4,"date":"2022-07-26 14:05:05","from":"EUR","to":"USD","rate":1.013074}   // 2 
{"id":5,"date":"2022-07-26 14:05:05","from":"USD","to":"EUR","rate":0.987095}   // 2
{"id":6,"date":"2022-07-26 14:59:03","from":"EUR","to":"USD","rate":1.013002}   // 3
{"id":7,"date":"2022-07-26 14:59:03","from":"USD","to":"EUR","rate":0.987165}   // 3

denotations above:
// 1 dummy initialization row with fake data.
// 2 real data row the first immediate query to Forex API.
// 3 real data row from the query run at the next full hour hh:00:00 after our application started.

### How to start the forex_to_db application?

Open a terminal on your local host and give the following command:

apikey=xxx run_max_hours=z docker-compose up

where:
replace xxx with your valid apikey
or
replace xxx with DUMMY to show only fabricated fake + possible valid old data in the DB.

AND

replace z by number of hours to run [-1,0,1,...]:
run_max_hours=-1 -> run queries once immediately and repeat at each full hour forever.
run_max_hours=0  -> run queries only just once immediately, never repeat.
run_max_hours=1  -> run queries once immediately and repeat only once at the next full hour.
...
run_max_hours=7  -> run queries once immediately and repeat at every next full hour 7 times.
...

apikey=DUMMY enables running the application without loading the external apilayer.com web site.
During development this option is important to avoid exceeding the daily/monthly limits for free of charge queries to the Forex API.
With apikey=DUMMY (or other invalid apikey) the process is seen to repeat every hour, but no new data is received. Fake (+ possible valid old data) rows in the DB table is anyway shown on the terminal after every round.

With other than DUMMY invalid apikey the query is sent but error message is received from the external API - ignored - no new data is stored.

### How to stop the forex_to_db application?

On a terminal give following command:

docker-compose down

### Resul

### possible steps for further development of the forex_to_db demo SW:

#### define the currency combinations to be queried outside of the source code. E.g. in application.properties file.

#### Turn the collection interval from fixed 1 hour into an environment variable.

#### Write a simple python (e.g. streamlit) application to show exchange rates as graphs (x=date_time, y=exchange_rate) from the forex_db.



