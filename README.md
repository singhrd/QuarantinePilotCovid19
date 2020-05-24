# QuarantinePilotCovid19
### Covid snapshots and annotations with Angular visualization

This project is a simple pilot to ingest covid data from a few sources, create 
some meaningful metrics and make them available for comparison via Angular dashboard.

### How do I run the analytics?

Analytics code is written in scala and can be 
simply run  by typing the following sbt command on a terminal inside the analytics 
home directory. Additional targets will be added in second iteration.
  - `sbt run`  
  
### What does the analytics code do?

The Analytics code handles the following:

  - *Ingestion* Daily pull of data from several sources
  - *Transform* Convert it into a json schema amenable for Angular consumption 
  - *Annotate* Add additional metrics for the epidemic described in the *Annotation Section*
  
### CovidSnapshots

The Underlying schema for the CovidSnapshots (daily or cumulative) is basically 
derived in a straight forward way from the csv. 

  - confirmed - number of cases confirmed positive for COVID19 via testing
  - recovered - number of confirmed cases that resolved with recovery
  - deaths - number of confirmed cases that resolved with demise
  - active - number of confirmed cases that have not resolved or lead to demise
  - *active = confirmed - deaths - recovered* 
  
The data from the source is cumulative but we also derive the daily cases for 
each category. Note that there is a potential lag in the deaths/recovery 
from the confirmed cases (possibly in weeks), so the active calculation is slightly 
more meaningful for the cumulative snapshot.


   
### Annotations

We extract some additiona features from the CovidSnapshots, we call metrics.
There are three metrics we derive:

#### Spread rate

We assumes sigmoid distribution for confirmed cases and estimates
spread rate as follows 
  - Spread Rate = (Log[Confirmed cases at time t] - Log[Confirmed cases at time t-delta])/delta
  
#### Daily Growth rate

This metric provides an indicator for the inflection point. A value switch from greater than 1.0
to less than 1.0 and staying below 1.0 indicates that we are close to an inflection point.
  - Daily Growth rate = [Confirmed cases at time t/Confirmed cases at time t-delta]^(1/delta)


#### Fatality rate

As mentioned before there is a lag between the confimed cases and the resolution 
of those confirmed cases by several weeks. We provide a crude measure for now

   - Fatality rate = [Cumulative deaths at time t]/[Cumulative confirmed cases at time t]
     

 
    
