<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<body id="configurationpage">	
<jsp:include page="includes/header.jsp"></jsp:include>

<div class="body">
<jsp:include page="includes/menu.jsp"></jsp:include>

	<div id="configuration" class="panel" >
	
		<c:if test="${ requestScope.excellerisOn eq true and requestScope.ihapoiOn eq true }">
			<ul>
				<li><a href="#excellerisConfiguration">Excelleris</a></li>
				<li><a href="#ihaConfiguration">IHA POI</a></li>
			</ul>
		</c:if>
		
		<c:if test="${ requestScope.excellerisOn eq true }">
			<jsp:include page="includes/excellerisConfiguration.jsp"></jsp:include>
		</c:if>
		
		<c:if test="${ requestScope.ihapoiOn eq true }">
			<jsp:include page="includes/ihaConfiguration.jsp"></jsp:include>
		</c:if>
	</div>
</div>

<jsp:include page="includes/footer.jsp"></jsp:include>



