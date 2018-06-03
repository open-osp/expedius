<%@ page isErrorPage="true" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%--jsp:include page="../template/header.jsp" flush="false"/--%>

<div id="genError">
	<h3>Error</h3>
	
	<p>
		We are not sure what you are trying to acheive so we sent you 
		this error page instead.
	</p>
	<p>
		<%= exception.getMessage() %>
	</p>
</div>

<%--jsp:include page="../template/footer.jsp"  flush="true"/--%>