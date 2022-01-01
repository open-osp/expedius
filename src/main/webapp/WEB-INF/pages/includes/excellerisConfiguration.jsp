<jsp:useBean id="excellerisConfigurationBean" scope="request" type="com.colcamex.www.bean.ConfigurationBeanInterface"/>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
	
<div id="excellerisConfiguration" class="tab" >

	<form action="${ pageContext.request.contextPath }/configuration" 
		method="POST" id="login_form" >
		
		<input type="hidden" name="login" value="ExcellerisConfiguration" />
	
		<h3 class="expedius_paneltitle">Excelleris Configuration</h3>
	
		<fieldset>
		
		<legend>Excelleris Login Information</legend>
			
		<img src="${ pageContext.request.contextPath }/assets/question.jpg" width="18px" height="18px"
			id="loginInfo" title="This user-name and password must match the user-name and password issued from Excelleris." />
			
		
		<table>
			<tr>				
				<td>User Name</td> 
				<td>
					<input type="text" id="login_userName" name="userName" />
								
				</td>
				<td id="login_userName_error" ></td>
			</tr>
			<tr>
				<td>Password</td> 
				<td>
					<input type="password" id="login_password" name="password" />				
				</td>
				<td id="login_password_error" ></td>
			</tr>
			<tr>
				<td>Confirm Password</td> 
				<td>
					<input type="password" id="login_passwordConfirm" name="passwordConfirm" />				
				</td>
				<td id="login_passwordConfirm_error" ></td>
			</tr>

		</table>
		<div>
			<input type="button" id="login_applyButton" name="applyLogin" value="Apply" />				
		</div>
		
		<div id="loginDataStatusMessage" >${ requestScope.excellerisLoginStatusMessage }</div>			
		<div id="loginActionMessage" >${ requestScope.loginActionMessage }</div>	
		
		</fieldset>
	
	</form>
	<form method="POST" 
		action="${ pageContext.request.contextPath }/fileUpload" 
		id="cert_form" 
		enctype="multipart/form-data" >
		
		<input type="hidden" name="certificate" value="ExcellerisConfiguration" />
		
		<fieldset>
			<legend>Excelleris Certificate Install</legend>
			
			<img src="${ pageContext.request.contextPath }/assets/question.jpg" width="18px" height="18px"
				id="certificateInfo"
				name="certificateInfo" 
				title="Upload the Excelleris security certificate here. Enter the password for the certificate twice." />
			
			<table>
			<tr>
				<td>Upload Certificate</td>
				<td>
					<input type="file" id="cert_upload" name="file" />

				</td>
				<td id="cert_upload_error" ></td>
			</tr>
			<tr>
				<td>Certificate Password</td>
				<td><input type="password" id="cert_password" name="password" /></td>
				<td id="cert_password_error" ></td>
			</tr>
			<tr>
				<td>Confirm Password</td>
				<td>
					<input type="password" id="cert_passwordConfirm" name="passwordConfirm"  />					
				</td>
				<td id="cert_passwordConfirm_error" ></td>
			</tr>

			</table>
			
			<div>
				<input type="button" id="cert_applyButton" name="applyCertificate" value="Install"/>					
			</div>
			
			<div id="certificateStatusMessage" >${ requestScope.excellerisCertStatusMessage }</div>
			<div id="certificateActionMessage" >${ requestScope.message }</div>
			
		</fieldset>
	</form>
	
	<form action="${ pageContext.request.contextPath }/configuration" 
		method="POST" 
		id="link_form" >
		
		<input type="hidden" name="config" value="ExcellerisConfiguration" />
		
		<fieldset>
			<legend>Excelleris Links</legend>
			
		<img src="${ pageContext.request.contextPath }/assets/question.jpg" width="18px" height="18px"
			id="linkInfo" title="Only change these links when instructed by Excelleris. If the autodownloader is not working, these links can be used for a manual lab download." />
			
		<dl>
			<dt>
				<label for="link_path" >URI</label>
				</dt><dd>
				<input type="text" id="link_path" name="path" value="${ excellerisConfigurationBean.servicePath }" readonly/>
				<span id="link_path_error"></span>
			</dd>
			<dt>
				<label for="link_loginPath" >Login</label>
				</dt><dd>
				<input type="text" id="link_loginPath" name="loginPath" value="${ excellerisConfigurationBean.loginPath }" readonly/>
				<span id="link_loginPath_error"></span>
			</dd>
			<dt>
				<label for="link_fetchPath" >Fetch</label>
				</dt><dd>
				<input type="text" id="link_fetchPath" name="fetchPath" value="${ excellerisConfigurationBean.fetchPath }" readonly/>
				<span id="link_fetchPath_error"></span>
			</dd>
			<dt>
				<label for="link_acknowledgePath" >Acknowledge</label>
				</dt><dd>
				<input type="text" id="link_acknowledgePath" name="acknowledgePath" value="${ excellerisConfigurationBean.acknowledgePath }" readonly/>
				<span id="link_acknowledgePath_error"></span>
			</dd>
			<dt>
				<label for="link_logoutPath" >Logout</label>
				</dt><dd>
				<input type="text" id="link_logoutPath" name="logoutPath" value="${ excellerisConfigurationBean.logoutPath }" readonly/>
				<span id="link_logoutPath_error"></span>
			</dd>
		</dl>	

		</fieldset>

	</form>	
</div>


