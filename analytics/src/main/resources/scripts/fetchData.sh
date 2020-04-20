wget "https://query.data.world/s/qgufokiphdezngsbyimut2psfgo7th"
echo finished downloading
echo Now copying the file
cp "qgufokiphdezngsbyimut2psfgo7th" "/tmp"
mv "qgufokiphdezngsbyimut2psfgo7th" "download.csv"
cp "download.csv" "../../../../../data/csv/"
