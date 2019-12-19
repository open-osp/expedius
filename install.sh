#!/bin/sh

##----------------------------------------------------------------------
## Expedius Install script
##
## Author: Dennis Warren @ Colcamex Resources
## Date: February 2012
## script is used to install Expedius Lab Auto Downloader.
##
##----------------------------------------------------------------------

# init variables
TOMCAT_PATH=${CATALINA_BASE}\/webapps
ADMIN_EMAIL=none
APP_DATA=\/var\/lib\/expedius

# set up application directories
# file permissions for Tomcat rw = 755
if [ ! -d "${APP_DATA}/.appdata" ]; 
	then
		echo "Setting up file structure..."

		sudo mkdir ${APP_DATA}
		sudo mkdir ${APP_DATA}/hl7
		sudo mkdir ${APP_DATA}/.appdata
		sudo mkdir ${APP_DATA}/.ssl
		sudo mkdir ${APP_DATA}/logs
		sudo chown -R tomcat6 ${APP_DATA}
		sudo chgrp -R tomcat6 ${APP_DATA}
		sudo chmod -R 755 ${APP_DATA}

fi

# get administrators email address.
echo "\nEnabling email service requires a smtp server, such as Postfix \nto be installed on this server."
read -p  "Enable email service [y/n]: " EMAIL_SERVICE
if [ ${EMAIL_SERVICE} = "y" ];
	then
		EMAIL_SERVICE="yes"
		read -p "Enter an administration email address [default=none]: " ADMIN_EMAIL
fi

# populate the properties file   
#read -p "Email address for administrator? " ADMIN_EMAIL

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

while read -p "Enter the context path for your Oscar install (ie. Oscar_12): " OSCAR_CONTEXT
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


echo "Deploying Expedius..."

# put the war file in place
	sudo cp ./Expedius.war ${TOMCAT_PATH}
	
# clean up

echo Install complete.