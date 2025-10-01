INSTALLATION INSTRUCTIONS FOR EXPEDIUS

AUTHOR: DENNIS WARREN
COMPANY: COLCAMEX RESOURCES
SUPPORT: dwarren@colcamex.com
WEBSITE: www.colcamex.com
COPY RIGHT: Colcamex Resources 2012-2013

--------------------------------------------------

PREREQUISITES:

*  CATALINA_BASE and CATALINA_HOME environment variables are set.
*  Tomcats deployment folder has the standard name "webapps".
*  Oscar is installed, configured and deployed at least once.
*  An Excelleris account

INSTALLATION

1.  un-tar the ExpediusRC file.

2.  Run the installation script, install.sh, from inside the ExpediusRC directory
		- sh install.sh
		- answer the questions when prompted.
		
3.  Add a new user to the Tomcat users file.
		- sudo nano $CATALINA_BASE/conf/tomcat-users.xml
		- add a username and password for the user who will be logging into Expedius
		
4.  Restart Tomcat.
		- sudo /etc/init.d/tomcat6 restart

5.	Open a browser and navigate to Expedius (yes with a capital 'E').
		- http://[server address]:8080/Expedius
		
6.	Log-in with the log-in data you set up in tomcat-users.xml.

7.	Configure Expedius on the Configuration page with the security key and log-in information provided by Excelleris. 

8.	Start the downloader - sit back and relax.

NOTES

If you alter, re-deploy, change context paths or upgrade Oscar you will need to run the install.sh script again. You will NOT loose 
lab files by doing this. 

All HL7 lab files are down-loaded to /var/lib/expedius/excelleris/.  If a lab transfer to Oscar should fail, this is where you 
will find the down-loaded file. This file can be manually uploaded to Oscar via Oscar's upload method.

Basic logs are found in 2 locations. User viewable logs are provided in the user GUI. A text copy is stored at 
/var/lib/expedius/logs/.

Full troubleshooting logs are recorded to catalina.out. 

Before reporting an issue please copy and paste the error into your email from both logs.
	
Expedius is able to email an administrator when a down-load fails to transfer labs to Oscar's inbox. This feature is still
experimental. Your server will require an email server such as Postfix to implement.

TROUBLES?

If Oscar fails to deploy (start up) after installing Expedius you can run the uninstall.sh script. 
	- copy the logs and email them to me. 
	- stop Tomcat: sudo /etc/init.d/tomcat6 stop
	- run uninstall.sh: sh uninstall.sh
	- start Tomcat: sudo /etc/init.d/tomcat6 start
	- recover any down-loaded lab files from: /var/lib/expedius/excelleris/
	
Expedius install backs up your Oscar properties file when installed.  You can restore Oscar properties from [oscar].properties.save 
in the same folder where the Oscar properties file is located.
	- sudo cp -p [oscar].properties.save [oscar].properties

Email me: dwarren@colcamex.com


