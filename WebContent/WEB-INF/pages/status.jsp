<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<script type="text/javascript">
	var status = '<c:out value="${controllerBean.status}"/>';
</script>

<body id="statuspage" >
	<header>
	
	<jsp:include page="includes/header.jsp"></jsp:include>
	</header>
		<div class="body">
		
	<jsp:include page="includes/menu.jsp"></jsp:include>

	<div id="status" class="panel" >
		<form action="${pageContext.request.contextPath}/controller" method="POST" id="statusPanel" >
			<h3>eLab Expeditor</h3>	
				
			<fieldset id="statusField">
			
				<legend>Status</legend>

				<div id="statusWindow" >
				
					<c:if test="${controllerBean.status}" >
						<h2 style="color:green;">Running</h2>
					</c:if>
					<c:if test="${not controllerBean.status}" >
						<h2 style="color:red;">Stopped</h2>
					</c:if>
						
					<c:if test="${controllerBean.status}" >
					
						<h4>Poll Schedule</h4>
							<p>
							<c:if test="${controllerBean.pollSetting eq 1}" >
								Frequency every: <c:out value="${controllerBean.pollInterval / 3600}" /> hour(s).
							</c:if>
							
							<c:if test="${controllerBean.pollSetting eq 2}" >
								Time of Day
							</c:if>
							</p>
					</c:if>	
					<c:if test="${not empty controllerBean.lastDownLoad}" >
						<h4>Last Run</h4>
						<p><c:out value="${controllerBean.lastDownLoad}" /></p>
					</c:if>	
					<c:if test="${controllerBean.status}" >	
						<h4>Next Run</h4>			
						<p><c:out value="${controllerBean.nextDownLoad}" /></p>
						
					</c:if>						
				</div>
		
				<div id="actionErrors">	
					
					<c:if test="${! empty(errorMessages)}">			
						<ul id="errorMessages">
							<c:forEach items="${errorMessages}" var="error"> 
						 		<li style="color:red;"><c:out value="${error}" /></li>
						 	</c:forEach>
					 	</ul>
				 	</c:if>	 					 	
				</div>
				
				<div class="buttonGroup" >
					<input type="button" id="refreshButton" name="refreshButton" value="Refresh Status" />
				</div>
				
				<c:if test="${! empty(dismissableErrorMessages)}">	
					<table id="dismissableErrorMessages">
						<tr><th class="column1"> &nbsp;</th><th style="color:red;">Severe Error(s) Have Occurred</th></tr>
						<c:forEach items="${ dismissableErrorMessages }" var="disError" varStatus="loop" > 
					 		<tr>					 			
					 			<td class="column1">
					 				<input type="checkbox" id="dismissError" name="yes" value="<c:out value="${ loop.index }" />" />
					 				dismiss
					 			</td>
					 			<td><c:out value="${ disError }" /><td>			
					 		</tr>
					 	</c:forEach>
				 	</table>
				</c:if>	
								
			</fieldset>

			<fieldset id="timerControls">
				<legend>Auto Download</legend>
				<span style="font-size:80%; margin-left:10px; padding-top:10px;">Automatically retrieve labs at scheduled times. <a href="${pageContext.request.contextPath}/schedule">set schedule</a></span>
					<div class="buttonGroup">
						<input type="submit" id="stop" name="control" value="Stop" />
						<input type="submit" id="start" name="control" value="Start" />
					</div>
				</fieldset>
			<fieldset id="manualControls">
				<legend>Manual Download</legend>
					
					<div class="buttonGroup">
						<input type="submit" id="download" name="control" value="Get Labs Now" />
					</div>
					
					<c:if test="${empty message}" >	
						<span style="font-size:80%; margin-left:10px; padding-top:10px; ">Retrieve labs now.</span>
					</c:if>
					
					<c:if test="${not empty message}" >				
						<span style="color:green;">
							<c:out value="${message}" />							
						</span>
					</c:if>

			</fieldset>
		
			<!-- div id="progressbar" style="width:90%; margin-top:10px; margin-left:10px;"></div-->								
		
		</form>
	</div>
	</div>
	<jsp:include page="includes/footer.jsp"></jsp:include>
	
