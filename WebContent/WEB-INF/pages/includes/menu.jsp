<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<nav>
	<ul>
		<li id="statusbutton" ><a href="${pageContext.request.contextPath}/status" >Status</a></li>
		<li id="schedulebutton" ><a href="${pageContext.request.contextPath}/schedule" >Schedule</a></li>
		<li id="configurationbutton" ><a href="${pageContext.request.contextPath}/configuration" >Settings</a></li>
	</ul>
	
</nav>