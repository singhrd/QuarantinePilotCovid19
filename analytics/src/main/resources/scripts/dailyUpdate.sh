#!/bin/sh

dateToday=`date +"%Y-%m-%d"`
echo dateToday

baseDir="/home/rajdeep/workspace/QuarantinePilotCovid19/"
echo "Starting in ${baseDir}"

baseSourceDir="https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19"

countyDataSourceDir="https://raw.githubusercontent.com/nytimes/covid-19-data/master/"
sanDiegoCountyDataSourceDir="wget https://opendata.arcgis.com/datasets/"
baseFileSuffixCountryLevel="global.csv"
sanDiegoCountyWebFileName="854d7e48e3dc451aa93b9daf82789089_0.csv"
sanDiegoCountyFileName="sandiegozipcodesCOVID19.csv"

echo "Country data Source is ${baseSourceDir}"
echo "State and county data source is ${baseCountyDataDir}"
echo "San Diego data Source is ${SanDiegoCountyFileName}"

fatalityFileNameCountryLevel="${baseSourceDir}_deaths_${baseFileSuffixCountryLevel}"
confirmedFileNameCountryLevel="${baseSourceDir}_confirmed_${baseFileSuffixCountryLevel}"
recoveredFileNameCountryLevel="${baseSourceDir}_recovered_${baseFileSuffixCountryLevel}"
dataFileNameCountyLevel="${countyDataSourceDir}us-counties.csv"
dataFileNameSanDiegZipCodeLevel="${sanDiegoCountyDataSourceDir}${sanDiegoCountyWebFileName}"


# wget $fatalityFileNameCountryLevel

# echo "Finished getting Country level ${fatalityFileNameCountryLevel} file from JH github source"

# wget $confirmedFileNameCountryLevel

# echo "Finished getting Country level ${confirmedFileNameCountryLevel} file from JH github source"

# wget $recoveredFileNameCountryLevel
    
# echo "Finished getting Country level ${recoveredFileNameCountryLevel} file from JH github source"

# wget $dataFileNameCountyLevel

# echo "Finished getting County and State Level ${dataFileNameCountyLevel} file from NY Times source"

wget $dataFileNameSanDiegZipCodeLevel -O $sanDiegoCountyFileName

echo "Finished getting San Diego ${dataFileNameSanDiegZipCodeLevel} file from Open data Arcgis source"

mv *.csv "${baseDir}data/csv/current/"

# now run the analytics code and it should put the data in the right ui folder

cd ../../../../

sbt run

# then add the new data files, generate a canned commit message with the current
# date and push 

git add "${baseDir}data/csv/current/"
git add "${baseDir}ui/src/assets/"

commitMessage="${dateToday}-daily-refresh"

git commit -m $commitMessage

echo $commitMessage

git push
