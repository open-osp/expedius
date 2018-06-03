#!/bin/sh

##----------------------------------------------------------------------
## Expedius Uninstall script
##
## Author: Dennis Warren @ Colcamex Resources
## Date: February 2012
## script is used to uninstall Expedius Lab Auto Downloader. Any
## downloaded lab files will be left at /var/lib/expedius/excelleris/
##
##----------------------------------------------------------------------

APP_DATA=\/var\/lib\/expedius

# Remove plug-in entries

if [ -f ${APP_DATA}/expedius.properties ];
	then
		echo "Removing plug-in from Oscar..."

		while read -r line; 
			do
				case $line in
					EMR_CONTEXT_PATH=*) eval $line ;;
					TOMCAT_ROOT=*) eval $line ;;
					*) ;;
				esac
		done < ${APP_DATA}/expedius.properties
	
		if [ -f "${CATALINA_HOME}${EMR_CONTEXT_PATH}.properties" ];

			then

				EMR_PROPERTIES=${CATALINA_HOME}${EMR_CONTEXT_PATH}.properties

			else

				echo " ERROR: Oscar properties file NOT found at ${CATALINA_HOME}${EMR_CONTEXT_PATH}.properties"

			while read -p "Enter FULL path to Oscar properties file (ie. /usr/share/tomcat6/oscar.properties): " OSCAR_PROPERTIES
				do

					if [ -f ${OSCAR_PROPERTIES} ];
						then
							EMR_PROPERTIES=${OSCAR_PROPERTIES}
							break
						else
							echo " ERROR: Oscar properties file not found at ${OSCAR_PROPERTIES}"
					fi

			done
		fi
		
	else
		echo "Expedius is not installed on this server."
		break
fi

# from oscar

JAR_FILE=${TOMCAT_ROOT}${EMR_CONTEXT_PATH}/WEB-INF/lib/expediusWs.jar

if [ -f ${JAR_FILE} ];
	then
		sudo rm -r $JAR_FILE
fi

# from oscar properties...

sudo cp -p ${EMR_PROPERTIES} ${EMR_PROPERTIES}.save
sed -e '/ModuleNames=Expedius/d' -e '/http_expedius_endpoint=.*/d' $EMR_PROPERTIES > oscartemp
sudo mv oscartemp $EMR_PROPERTIES

# remove directories. Leaving /var/lib/expedius/excelleris/*

if [ -d "${APP_DATA}/.appdata" ]; 
	then
		echo "Removing application directories..."
		sudo rm -r ${APP_DATA}/.appdata
		sudo rm -r ${APP_DATA}/.ssl
		sudo rm ${APP_DATA}/expedius.properties
fi

# remove war file

EXPEDIUS_WAR=${TOMCAT_ROOT}/Expedius.war
if [ -f ${EXPEDIUS_WAR} ];
	then
		echo "Undeploying war file..."
		sudo mv $EXPEDIUS_WAR $EXPEDIUS_WAR~
fi

echo "Done. Please start/restart Tomcat."
echo Any downloaded lab files are located at: ${APP_DATA}/excelleris
