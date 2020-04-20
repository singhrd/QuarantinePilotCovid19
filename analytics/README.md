# QuarantinePilotCovid19/analytics
Covid Pilot Test Analytics Repo

The primary objective of this repo is to generate the following artifacts for
the UI repo in json format.

1. CovidSnapshot - This includes the daily report and the cumulative report.
Both have the same schema. It is basically the number of {confirmed, recovered,active,deaths}
at a given location (specified by lat,long) on or until a given date.

Their is location marker information also in the CovidSnapshot in province_state and
country fields. Currently we don't have tests data so we have made it optional 
when we can add it from an additional source.

The snapshots are written in a single json structure which is a list of CovidSnapshot.


2. Annotation - This includes simple and useful time series artifacts we have 
created from the atomic numbers being reported and their rolling averages.

More specifically, we have:

a. Spread rate - how fast the epidemic is spreading
b. Growth rate - change in the spread rate
c. Positive detection rate - percent of tests that are confirmed infected

