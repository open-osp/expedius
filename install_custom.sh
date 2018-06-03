#!/bin/sh

##----------------------------------------------------------------------
## Expedius Install script
##
## Author: Dennis Warren @ Colcamex Resources
## Date: November 2015
## Script used for a custom Expedius install.
##
##----------------------------------------------------------------------

# init variables
TOMCAT_PATH=${CATALINA_BASE}\/webapps
APP_DATA=\/var\/lib\/expedius
OSCAR_WS=NULL

echo "Adding plugin to Oscar..."

if [ ! -d "${TOMCAT_PATH}" ];
	then
		while read -p "Error: Catalina Base not defined. Enter path for Tomcat webapps directory (ie. /var/lib/tomcat6/webapps): " TOMCAT_BASE
		do	
			if [ -d "${TOMCAT_BASE}" ];
				then
					TOMCAT_PATH=${TOMCAT_BASE}
					break
				else
					echo " ERROR: Tomcat webapps not found at: ${TOMCAT_BASE}"
			fi
		done					
fi

while read -p "Enter your current context path for Oscar (ie. Oscar_12): " OSCAR_CONTEXT
do	
	if [ -d "${TOMCAT_PATH}/${OSCAR_CONTEXT}" ];
		then
			sed -e "s|EMR_CONTEXT_PATH=.*|EMR_CONTEXT_PATH=\/${OSCAR_CONTEXT}|g" -e "s|ADMIN_EMAIL=.*|ADMIN_EMAIL=${ADMIN_EMAIL}|g" -e "s|EMAIL_ON=.*|EMAIL_ON=${EMAIL_SERVICE}|g" -e "s|LOG_PATH=.*|LOG_PATH=\/var\/lib\/expedius\/logs|g" -e "s|TOMCAT_ROOT=.*|TOMCAT_ROOT=${TOMCAT_PATH}|g" expedius.properties > tmpexpedius
			sudo mv tmpexpedius /var/lib/expedius/expedius.properties
			break
		else
			echo " ERROR: Oscar install not found at ${TOMCAT_PATH}/${OSCAR_CONTEXT}"
	fi
done

echo "Adding plugin configuration to Oscar properties..."

if [ -f "${CATALINA_HOME}/${OSCAR_CONTEXT}.properties" ];

	then

# set the default Oscar properties path.

		TEMP=${CATALINA_HOME}/${OSCAR_CONTEXT}.properties

	else

# get the custom path.

		echo " ERROR: Oscar properties file not found at ${CATALINA_HOME}/${OSCAR_CONTEXT}.properties"

		while read -p "Enter FULL path to Oscar properties file (ie. /usr/share/tomcat6/oscar.properties): " OSCAR_PROPERTIES
		do

			if [ -f ${OSCAR_PROPERTIES} ];
				then
					TEMP=${OSCAR_PROPERTIES}
					break
				else
					echo " ERROR: Oscar properties file not found at ${OSCAR_PROPERTIES}"
			fi

		done
fi

# modify Oscar's property file.

sudo cp -p ${TEMP} ${TEMP}.save
sed -e '/ModuleNames=Expedius/d' -e '/http_expedius_endpoint=.*/d' $TEMP > oscartemp
echo http_expedius_endpoint=//127.0.0.1:8080/${OSCAR_CONTEXT}/ws >> oscartemp
echo 'ModuleNames=Expedius' >> oscartemp
sudo mv oscartemp $TEMP

echo "Deploying Expedius..."

# put the war file in place
	sudo cp ./Expedius.war ${TOMCAT_PATH}

# put the jar file in place
	sudo cp ./expediusWs.jar ${TOMCAT_PATH}/${OSCAR_CONTEXT}/WEB-INF/lib
		
# clean up

sudo rm ./Expedius.war
sudo rm ./expediusWs.jar
sudo rm ./expedius.properties
sudo rm ./README.txt
sudo rm ./install.sh

echo Install complete.