#!/bin/sh

dateToday=`date +"%Y-%m-%d"`

echo dateToday

#/home/rajdeep/workspace/QuarantinePilotCovid19/

baseDir="../../../../"
echo $baseDir

baseSourceDir="https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19"

echo $baseSourceDir

baseFileSuffix="global.csv"

confirmedFileName="${baseSourceDir}_deaths_${baseFileSuffix}"

echo $confirmedFileName
wget $confirmedFileName

echo "Finished getting fatality file from JH github source"

wget "${baseSourceDir}_confirmed_${baseFileSuffix}"

echo "Finished getting confirmed file from JH github source"

wget "${baseSourceDir}_recovered_${baseFileSuffix}"
    
echo "Finished getting recovered file from JH github source"


mv *.csv "${baseDir}data/csv/current/"

# now run the analytics code and it should put the data in the right ui folder

cd ../../../../

sbt run

# then generate a canned add/commit with the current date
# then push 

git add "${baseDir}data/csv/current/"
git add "${baseDir}ui/src/assets/"

commitMessage2="${dateToday}-Updating-data-from-jhu-source-AND-analytics-artifacts-with-latest-data-pull"

git commit -m $commitMessage2

echo $commitMessage2

git push
