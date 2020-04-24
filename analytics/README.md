# QuarantinePilotCovid19
### Analytics

This repo ingests data from static set of csv files and generates artifacts for
the UI repo in json format.

### Analytics outputs

1.  **CovidSnapshot** Daily or cumulative report based on a given source - the number of {confirmed, recovered,active,deaths, tests}, at a given location (specified by lat,long) on or until a given date.
Their is location marker information also in the CovidSnapshot in province_state and
country fields. Currently we don't have tests data so we have made it optional 
when we can add it from an additional source.

2.  **CovidSnapshot** A List of CovidSnapshots for different time and locations.


3.  **Annotation** This captures derived time series metrics we  
create from the atomic observed numbers being reported.

More specifically, we have:

-  a. *Spread rate* - how fast the epidemic is spreading. The exponential 
-  b. *Growth rate* - change in the spread rate. This gives information on the inflection point.		
-  c. *Positive detection rate* - percent of tests that are confirmed infected

