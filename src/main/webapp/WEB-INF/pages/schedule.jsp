<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<jsp:include page="includes/header.jsp"></jsp:include>

<body id="schedulepage">

	<div class="body">
<jsp:include page="includes/menu.jsp"></jsp:include>

<div id="schedule" class="panel" >

	<form action="${pageContext.request.contextPath}/schedule" method="post" id="scheduleForm">

		<h3 class="expedius_paneltitle">Download Schedule</h3>
			
		<fieldset id="frequency">
		<legend>		
			<label for="pollSetting">Frequency</label>		
		</legend>
		
		<input type="radio" 
					id="pollSetting_frequency" 
					name="pollSetting" 
					value="1" 
					${(controllerBean.pollSetting eq 1) ? 'checked="checked"' : ''} />
		
		<img src="${pageContext.request.contextPath}/assets/question.jpg" width="18px" height="18px"
					id="frequencyInfo"
					name="frequencyInfo" 
					title="Select the frequency required for Expedius to check the lab server(s) (i.e.: Excelleris) for new labs." />
		
			<label for="pollInterval" >Fetch labs every </label>
			
			
				<select id="pollInterval" name="pollInterval" >
					<option id="time600" value="600" 
						${(controllerBean.pollInterval eq 600) ? 'selected="selected"' : ''} >
						10min
					</option>
					<option id="time3600" value="3600" 
						${(controllerBean.pollInterval eq 3600) ? 'selected="selected"' : ''} >
						60min
					</option>
					<option id="time7200" value="7200" 
						${(controllerBean.pollInterval eq 7200) ? 'selected="selected"' : ''} >
						2hours
					</option>
					<option id="time14400" value="14400" 
						${(controllerBean.pollInterval eq 14400) ? 'selected="selected"' : ''} >
						4hours
					</option>
					<option id="time28800" value="28800" 
						${(controllerBean.pollInterval eq 28800) ? 'selected="selected"' : ''} >
						8hours
					</option>
					<option id="time57600" value="57600" 
						${(controllerBean.pollInterval eq 57600) ? 'selected="selected"' : ''} >
						16hours
					</option>
				</select>
		
		</fieldset>
		
		
		
		<fieldset id="timeofday" >	
			<legend>	
				Time of day
			</legend>
			
			<input type="radio" 
			id="pollSetting_timeofday" 
			name="pollSetting" 
			value="2"  
			${(controllerBean.pollSetting eq 2) ? 'checked="checked"' : ''}
			/>
				Fetch Labs At:
				
				<img src="${pageContext.request.contextPath}/assets/question.jpg" 
					width="18px" 
					height="18px"
					id="timeInfo"
					name="timeInfo" 
					title="Select up to 4 times in one day required for Expedius to check the lab server(s) (i.e.: Excelleris) for new labs. " />
				
				<table>
					<tr><td>Time 1 </td> 
						<td align="right"> 
							<select id="pollTime_OneHour" name="pollTime_OneHour" >
								<option value="">--</option>
								<c:forEach items="${controllerBean.hourArray}" var="hour">
									<option value="${hour}" ${(controllerBean.pollTimeOneHour eq hour) ? 'selected="selected"' : ''} >
										<c:out value="${hour}"></c:out>
									</option>
								</c:forEach>
							</select>
							</td><td>
							:
							</td><td>
							<select id="pollTime_OneMinute" name="pollTime_OneMinute" >
								<option value="" >--</option>
								<option value="00"  ${(controllerBean.pollTimeOneMin eq 0) ? 'selected="selected"' : ''} >00</option>
								<option value="30"  ${(controllerBean.pollTimeOneMin eq 30) ? 'selected="selected"' : ''} >30</option>
							</select>
						</td>
	
					</tr>
					<tr>
						<td>Time 2 </td> 
						<td align="right"> 
							<select id="pollTime_TwoHour" name="pollTime_TwoHour" >
								<option value="" >--</option>
								<c:forEach items="${controllerBean.hourArray}" var="hour">
									<option value="${hour}" ${(controllerBean.pollTimeTwoHour eq hour) ? 'selected="selected"' : ''} >
										<c:out value="${hour}"></c:out>
									</option>
								</c:forEach>
							</select>
							</td><td>
							:
							</td><td>
							<select id="pollTime_TwoMinute" name="pollTime_TwoMinute" >
								<option value="" >--</option>
								<option value="00" ${(controllerBean.pollTimeTwoMin eq 0) ? 'selected="selected"' : ''} >00</option>
								<option value="30" ${(controllerBean.pollTimeTwoMin eq 30) ? 'selected="selected"' : ''} >30</option>
							</select>
						</td>
					</tr>
					<tr>
						<td>Time 3 </td> 
						<td align="right"> 
							<select id="pollTime_ThreeHour" name="pollTime_ThreeHour" >
								<option value="" >--</option>
								<c:forEach items="${controllerBean.hourArray}" var="hour">
									<option value="${hour}" ${(controllerBean.pollTimeThreeHour eq hour) ? 'selected="selected"' : ''} >
										<c:out value="${hour}"></c:out>
									</option>
								</c:forEach>
							</select>
							</td><td>
							:
							</td><td>
							<select id="pollTime_ThreeMinute" name="pollTime_ThreeMinute" >
								<option value="" >--</option>
								<option value="00" ${(controllerBean.pollTimeThreeMin eq 0) ? 'selected="selected"' : ''} >00</option>
								<option value="30" ${(controllerBean.pollTimeThreeMin eq 30) ? 'selected="selected"' : ''} >30</option>
							</select>
						</td>
					</tr>
					<tr>
						<td>Time 4 </td> 
						<td align="right"> 
							<select id="pollTime_FourHour" name="pollTime_FourHour" >
								<option value="" >--</option>
								<c:forEach items="${controllerBean.hourArray}" var="hour">
									<option value="${hour}" ${(controllerBean.pollTimeFourHour eq hour) ? 'selected="selected"' : ''} >
										<c:out value="${hour}"></c:out>
									</option>
								</c:forEach>
							</select>
							</td><td>
							:
							</td><td>
							<select id="pollTime_FourMinute" name="pollTime_FourMinute" >
								<option value="" >--</option>
								<option value="00" ${(controllerBean.pollTimeFourMin eq 0) ? 'selected="selected"' : ''} >00</option>
								<option value="30" ${(controllerBean.pollTimeFourMin eq 30) ? 'selected="selected"' : ''} >30</option>
							</select>
						</td>
					</tr>
				</table>
				<div id="timeErrorMessage" class="error" ><c:out value="${timeError}" /></div>
		</fieldset>
		
		<fieldset>
			
			<input type="checkbox" 
					name="startWithServer" 
					id="startWithServer"
					value="true"
					${(controllerBean.startWithServer) ? 'checked="checked"' : ''}
					/>
			<label for="startWithServer" >start Expedius when server boots.</label>
			
			<img src="${pageContext.request.contextPath}/assets/question.jpg" 
					width="18px" 
					height="18px"
					id="serverStart"
					name="serverStart" 
					title="Checking this ensures that Expedius will continue to 
						poll lab servers even after the server which hosts Expedius 
						is rebooted or shutdown.
						(Only works if Expedius was running prior to reboot or shutdown.)" />
		
			
		</fieldset>
		
		<div class="buttonGroup" id="buttonGroupSchedule" >
		
			<input type="submit" id="saveSettings" name="schedule" value="Save" />
		
			<div id="saved" style="color:green;" >
				<c:out value="${savedStatusMessage}" />
			</div>
					
		</div>
	</form>			
</div>
</div>
<jsp:include page="includes/footer.jsp"></jsp:include>
