#!/bin/sh

dateToday=`date +"%Y-%m-%d"`

echo dateToday

baseDir="/home/rajdeep/workspace/QuarantinePilotCovid19/"
echo $baseDir

baseSourceDir="https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19"

wrefCountyData="https://raw.githubusercontent.com/nytimes/covid-19-data/master/us-counties.csv"

echo $baseSourceDir
echo $baseCountyDataDir
baseFileSuffix="global.csv"

fatalityFileName="${baseSourceDir}_deaths_${baseFileSuffix}"
confirmedFileName="${baseSourceDir}_confirmed_${baseFileSuffix}"
recoveredFileName="${baseSourceDir}_recovered_${baseFileSuffix}"

echo $fatalityFileName
wget $fatalityFileName

echo "Finished getting ${fatalityFileName} from JH github source"

wget $confirmedFileName

echo "Finished getting ${confirmedFileName} from JH github source"

wget $recoveredFileName
    
echo "Finished getting ${recoveredFileName} from JH github source"

wget $wrefCountyData

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
