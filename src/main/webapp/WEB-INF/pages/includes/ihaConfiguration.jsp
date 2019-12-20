<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<script type="text/javascript">
	var htmlLogPath = "<c:out value='${requestScope.ihaUserLogPath}' />" ;
</script>

<div id="ihaConfiguration" class="tab" >

	<form action="${pageContext.request.contextPath}/configuration" method="POST" id="ihalogin_form" >
		
		<input type="hidden" name="login" value="IhaConfiguration" />
		
		<h3 class="expedius_paneltitle">IHA POI Configuration</h3>
		
		<fieldset>
		
		<legend>IHA POI Login Information</legend>
			
		<img src="${pageContext.request.contextPath}/assets/question.jpg" width="18px" height="18px"
			id="ihaLoginInfo"
			name="ihaLoginInfo" 
			title="This user-name and password must match the user-name and password issued from the Interior Health Authority." />	
	
		<table>
			<tr>				
				<td>User Name</td> 
				<td>
					<input type="text" id="ihalogin_userName" name="userName" />
								
				</td>
				<td id="ihalogin_userName_error" ></td>
			</tr>
			<tr>
				<td>Password</td> 
				<td>
					<input type="password" id="ihalogin_password" name="password" />				
				</td>
				<td id="ihalogin_password_error" ></td>
			</tr>
			<tr>
				<td>Confirm Password</td> 
				<td>
					<input type="password" id="ihalogin_passwordConfirm" name="passwordConfirm" />				
				</td>
				<td id="ihalogin_passwordConfirm_error" ></td>
			</tr>

		</table>
		<div>
			<input type="button" id="ihalogin_applyButton" name="applyLogin" value="Apply" />				
		</div>
		
		<div id="ihaLoginDataStatusMessage" >${requestScope.ihapoiLoginStatusMessage}</div>			
		<!-- div id="ihaLoginActionMessage" >${requestScope.IhaConfiguration_loginActionMessage}</div-->	
		
		</fieldset>
		</form>
		
		<form action="${ pageContext.request.contextPath }/configuration" method="POST" id="ihalink_form" >
			
			<input type="hidden" name="config" value="IhaConfiguration" />
			
			<fieldset>
				<legend>Excelleris Links</legend>
				
			<img src="${ pageContext.request.contextPath }/assets/question.jpg" width="18px" height="18px"
				id="ihaLinkInfo"
				name="ihaLinkInfo" 
				title="Only change these links when instructed by the IHA. If the autodownloader is not working, these links can be used for a manual lab download." />
				
			<dl>
				<dt>
					<label for="path" >URI</label>
					</dt><dd>
					<input type="text" id="ihalink_path" name="path" value="${ ihaConfigurationBean.servicePath }" />
					<span id="ihalink_path_error"></span>
				</dd>
				<dt>
					<label for="loginPath" >Login</label>
					</dt><dd>
					<input type="text" id="ihalink_loginPath" name="loginPath" value="${ ihaConfigurationBean.loginPath }" />
					<span id="ihalink_loginPath_error"></span>
				</dd>
				<dt>
					<label for="fetchPath" >Fetch</label>
					</dt><dd>
					<input type="text" id="ihalink_fetchPath" name="fetchPath" value="${ ihaConfigurationBean.fetchPath }" />
					<span id="ihalink_fetchPath_error"></span>
				</dd>
				<dt>
					<label for="acknowledgePath" >Acknowledge</label>
					</dt><dd>
					<input type="text" id="ihalink_acknowledgePath" name="acknowledgePath" value="${ ihaConfigurationBean.acknowledgePath }" />
					<span id="ihalink_acknowledgePath_error"></span>
				</dd>
				<!--dt>
					<label for="logoutPath" >Logout</label>
					</dt><dd>
					<input type="text" id="ihalink_logoutPath" name="logoutPath" value="${ ihaConfigurationBean.logoutPath }" />
					<span id="ihalink_logoutPath_error"></span>
				</dd -->
			</dl>	
				<div>
					<input type="button" id="ihalink_applyButton" name="applyLinks" value="Apply" />				
				</div>
				
			<div id="ihalinkActionMessage" >${ requestScope.IhaConfiguration_linkActionMessage }</div>
			</fieldset>
		
				
			<fieldset id="ihapoiLogs">
				<legend>IHA POI Logs</legend>
				<div>
					<input type="button" id="ihaLogButton" name="ihaLogButton" value="View Log" />
				</div>	
											
				<fieldset id="ihaLogFile">
					<legend id="legendLog" >
						<input type="button" id="closeLog" name="logButton" value="Close Log" />
						<input type="button" id="refreshLog" name="logButton" value="Refresh" />
					</legend>
					
					<div id="logContainer" style="overflow-y:scroll; height:300px;" ></div>	
				</fieldset>
			</fieldset>
	</form>
</div>