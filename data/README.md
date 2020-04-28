# QuarantinePilotCovid19/data
Covid Pilot Test Repo

One time download files for use to extract geo-locations and general geo data 
were downloaded from these sources

Kaggle
concap.csv - country capitals and lat/lon

https://usafacts.org/visualizations/coronavirus-covid-19-spread-map/
covid_county_population_usafacts.csv


Sources for the various data we use for this project

**curl or wget**
"https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_deaths_global.csv"

"https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_recovered_global.csv"

"https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_confirmed_global.csv"

Possibly another source for future
https://api.covid19data.cloud/v1/jh/daily-reports?last_update_from=yyyy-mm-dd&last_update_to=yyyy-mm-dd 

wget URL > desired fileName pipes it to a json file of desired name

**manual query download**

data.world query
  https://data.world/covid-19-data-resource-hub/covid-19-case-counts/workspace/file?filename=COVID-19+Cases.csv

    covid-19-case-counts-SanDiego-confirmed-deaths.csv


  https://ourworldindata.org/grapher/full-list-total-tests-for-covid-19
    full-list-total-tests-for-covid-19.csv
