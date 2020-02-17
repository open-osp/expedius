#!/bin/sh

##----------------------------------------------------------------------
## Expedius Install script
##
## Author: Dennis Warren @ Colcamex Resources
## Date: February 2012
## Updated: January 2020
## This script is used to install Expedius Lab Auto Downloader onto
## a server environment that supports Oscar EMR version 15*
##----------------------------------------------------------------------

# init variables
TOMCAT_PATH=${CATALINA_BASE}\/webapps
ADMIN_EMAIL=none
APP_DATA=\/var\/lib\/expedius

# check if user account has root access.
if [ "$(id -u)" != "0" ];
then
    echo `basename "$0"` "script must be run as root" 1>&2
    exit 1
fi

# find the highest version Tomcat running on this server
TOMCAT=$(ps aux | grep org.apache.catalina.startup.Bootstrap | grep -v grep | awk '{ print $1 }')
if [ -z "$TOMCAT" ]; then
    #Tomcat is not running, find the highest installed version
    if [ -f /usr/share/tomcat9/bin/version.sh ] ; then
    TOMCAT=tomcat9
    else
         if [ -f /usr/share/tomcat8/bin/version.sh ] ; then
         TOMCAT=tomcat8
         else
              if [ -f /usr/share/tomcat7/bin/version.sh ] ; then
              TOMCAT=tomcat7
             fi
         fi
    fi
fi

# set up the base application resource directories
if [ ! -d "${APP_DATA}/.appdata" ]; 
	then
		echo "Setting up file structure..."

		sudo mkdir ${APP_DATA}
		sudo mkdir ${APP_DATA}/hl7
		sudo mkdir ${APP_DATA}/.appdata
		sudo mkdir ${APP_DATA}/.ssl
		sudo mkdir ${APP_DATA}/log
fi

echo "Setting configuration properties"

# get administrators email address.
echo "\nEnabling email service requires a smtp server, such as Postfix \nto be installed on this server."
read -p  "Enable email service [y/n]: " EMAIL_SERVICE
if [ ${EMAIL_SERVICE} = "y" ];
	then
		EMAIL_SERVICE="yes"
		read -p "Enter an administration email address [default=none]: " ADMIN_EMAIL
fi
if [ ${EMAIL_SERVICE} = "n" ];
	then
		EMAIL_SERVICE="no"
fi

# capture new Expedius login information from the user.
read -p "Enter a new user name for Expedius: " EXPEDIUS_USERNAME
read -p "Enter a new password for Expedius: " EXPEDIUS_PASSWORD

# Confirm that the default CATALINA_BASE path is correct. If not correct, then try to build it.   
if [ ! -d "${TOMCAT_PATH}" ];
	then
		TOMCAT_PATH=\/var\/lib\/${TOMCAT}\/webapps
fi

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

# set up the Expedius user login information into the Tomcat Users file. 
if [ -d "${TOMCAT_PATH}" ];
	then
		sed -e "s|.*</tomcat-users>.*|\<role rolename=\"expedius\"\/\>\<user username=\"${EXPEDIUS_USERNAME}\" password=\"${EXPEDIUS_PASSWORD}\" roles=\"expedius\"\/\>\<\/tomcat-users\>|g" ${TOMCAT_PATH}/../conf/tomcat-users.xml > ./tomcat-users.xml
		sudo mv -f ./tomcat-users.xml ${TOMCAT_PATH}/../conf/
fi 

# Capture Oscar API login information from the user. 
read -p "Enter the Oscar EMR user name for Expedius: " OSCAR_USERNAME
read -p "Enter the Oscar EMR password for Expedius: " OSCAR_PASSWORD
read -p "Enter the Oscar EMR provider number for Expedius: " OSCAR_NUMBER
read -p "Enter the domain name and port for Oscar EMR (ie: localhost:8443): " OSCAR_DN

# get the Oscar context path and then set everything in the expedius.properties file.
while read -p "Enter the context path for your Oscar install (ie. oscar_myclinic): " OSCAR_CONTEXT
do	
	if [ -d "${TOMCAT_PATH}/${OSCAR_CONTEXT}" ];
		then
			sed -e "s|EMR_CONTEXT_PATH=.*|EMR_CONTEXT_PATH=${OSCAR_CONTEXT}|g" -e "s|ADMIN_EMAIL=.*|ADMIN_EMAIL=${ADMIN_EMAIL}|g" -e "s|EMAIL_ON=.*|EMAIL_ON=${EMAIL_SERVICE}|g" -e "s|LOG_PATH=.*|LOG_PATH=${APP_DATA}\/logs\/|g" -e "s|TOMCAT_ROOT=.*|TOMCAT_ROOT=${TOMCAT_PATH}|g" -e "s|EXCELLERIS=.*|EXCELLERIS=true|g" -e "s|IHAPOI=.*|IHAPOI=false|g" -e "s|EMR_HOST_NAME=.*|EMR_HOST_NAME=${OSCAR_DN}|g" -e "s|EMR_WS_USERNAME=.*|EMR_WS_USERNAME=${OSCAR_USERNAME}|g" -e "s|EMR_WS_PASSWORD=.*|EMR_WS_PASSWORD=${OSCAR_PASSWORD}|g" -e "s|SERVICE_NUMBER=.*|SERVICE_NUMBER=${OSCAR_NUMBER}|g" -e "s|TRUSTSTORE_URL=.*|TRUSTSTORE_URL=${APP_DATA}\/.ssl\/expedius_trust.jks|g" -e "s|KEYSTORE_URL=.*|KEYSTORE_URL=${APP_DATA}\/.ssl\/expedius_key.jks|g" -e "s|DATA_PATH=.*|DATA_PATH=${APP_DATA}\/.appdata\/|g" -e "s|HL7_SAVE_PATH=.*|HL7_SAVE_PATH=${APP_DATA}\/hl7\/|g" -e "s|ACKNOWLEDGE_DOWNLOADS=.*|ACKNOWLEDGE_DOWNLOADS=true|g" expedius.properties > tmpexpedius
			sudo mv tmpexpedius ${APP_DATA}\/expedius.properties
			break
		else
			echo " ERROR: Oscar install not found at ${TOMCAT_PATH}/${OSCAR_CONTEXT}"
	fi
done

# set up trust store with an Oscar SSL trust cert
echo "Installing the Oscar EMR SSL certificate into the Expedius TrustStore..."
openssl s_client -showcerts -connect ${OSCAR_DN} </dev/null 2>/dev/null|openssl x509 -outform PEM >${OSCAR_CONTEXT}.pem
keytool -import -v -trustcacerts -alias oscar -file ${OSCAR_CONTEXT}.pem -keystore ${APP_DATA}\/.ssl\/expedius_truststore.pkcs12 -storepass 3mr1esting89! -storetype PKCS12

echo "Deploying Expedius..."

# put the war file in place
sudo cp ./Expedius* ${TOMCAT_PATH}\/Expedius.war

# assign tomcat permissions to the resource directory
sudo chown -R ${TOMCAT}:${TOMCAT} ${APP_DATA}
	
# clean up

echo "Install complete. Log into Expedius at https:/${OSCAR_DN}/Expedius to complete setup"