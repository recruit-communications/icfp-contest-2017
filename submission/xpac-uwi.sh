#!/bin/bash

if (($# < 1)); then
	echo "./xpac-uwi.sh MeijinIDAI"
	exit
fi
AINAME=$1
DAT=`date "+%m%d-%H%M"`
sed -e "s/#AINAME/$AINAME/g" install_uwi_template > install
sed -e "s/#AINAME/$AINAME/g" punter_uwi_template > punter
./packaging.sh uwi-$AINAME-$DAT

