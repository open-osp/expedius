/**
* Author: Dennis Warren 
* Coclamex Resources
* www.colcamex.com
* dwarren@colcamex.com
*
* Date: Sept 2012
*/


$(document).ready(function() {
	
	$("#configuration * ").css({"font-size": "12px","color": "black"});
	
	// Tabs on the configuration page ----------------------------->
	 $(function() {
		 $( "#configuration" ).tabs();
	 });
	
	// Status bar...  ----------------------------->
	$.fn.progressBar = function() {
		 $( "#progressbar" ).progressbar({
			 value: 37
		 });
	}

	$().progressBar();
	
	// Disable manual download and start while poll is running ----------------------------->
	$.fn.disableDownload = function(status) {
		if(status == "true") {
			$("#download").attr("disabled", true);
			$("#start").attr("disabled", true);
			$("stop").attr("disabled", false);
		} else {
			$("#download").attr("disabled", false);
			$("#start").attr("disabled", false);
			$("stop").attr("disabled", true);
		}
	}
	
	$().disableDownload(status);

	
	// tool tip configuration with qTip plug-in ----------------------------->
	// sets all the ? with tool tips from its title attribute.
	$('img[title]').qtip();
	
	// page refresh command -----------------------------> 
	$('#refreshButton').click(function() {
	    //location.reload(true);
		var currentAction = $("#statusPanel").attr("action");
		var newAction = currentAction.substring(0, currentAction.lastIndexOf('/') + 1) + "status";
		$("#statusPanel").attr("action", newAction);
		$("#statusPanel").submit();
	});
		
	// view logfile actions ----------------------------->	
	$("#logFile").hide();
	$("#ihaLogFile").hide();

	$('input:button[name="logButton"]').click(function(){
		if(htmlLogPath != "") {
			var servlet = $(this).attr("id");
			
			if(servlet == "logButton") {			
				
				$("#logFile").show();			
				$("#logButton").hide();

				$.get(htmlLogPath, function(data) {
					  $("#logContainer").html(data);
				});
				
			} if (servlet == "closeLog") {
				
				if(!false) {
					$("#logFile").hide();
					$("#logButton").show();
				}
				
			} if (servlet == "refreshLog") {
				$.get(htmlLogPath, function(data) {
					  $("#logContainer").html(data);
				});
			}
		}
	});
	
	// error dismissing ----------------------------->
	$("#dismissError").click(function(){
		 if(confirm("Are you sure this error has been resolved?")) {

			 $("<input>").attr({type: "hidden", id: "dismisserror", name: "dismisserror", value: this.value}).appendTo("form");
			 
			 var currentAction = $("#statusPanel").attr("action");
			var newAction = currentAction.substring(0, currentAction.lastIndexOf('/') + 1) + "status";
			$("#statusPanel").attr("action", newAction);
			 $("#statusPanel").submit();
			 
		 } else {
			 $("#dismissError").attr("checked",null)
		 }		
	});
	
	
	
	// Password checker ----------------------------->
	$.fn.confirmPasswords = function(id) {

		var modify = id.split("_");
		var passOne = $("#"+modify[0]+"_password").val();
		var passTwo = $("#"+modify[0]+"_passwordConfirm").val();
		var confirm = true;
		
		$("#"+modify[0]+"_passwordConfirm_error").text(null);
		
		if(passOne === passTwo) {
			$("#"+modify[0]+"_passwordConfirm_error").text("Passwords Match");
			$("#"+modify[0]+"_passwordConfirm_error").css("color", "green");
		} else {
			$("#"+modify[0]+"_passwordConfirm_error").text("Password Mismatch");
			$("#"+modify[0]+"_passwordConfirm_error").css("color", "red");
			$("#"+modify[0]+"_passwordConfirm").val(null);			
			$("#"+modify[0]+"_password").val(null);
			confirm = false;
		}
		
		return confirm;
	}

	// Verify null fields on submit ----------------------------->
	
	$("input[name='applyLinks'], input[name='applyCertificate'], input[name='applyLogin']").click(function() {
		
		var id = $(this).attr("id");

		var prefix = (id.split("_"))[0]+"_";
		var formId = "#"+prefix+"form";
		var submit = true;

		$.each($(formId + " input"), function(key, value){	
			
			var item = value.value;
			$("#"+value.id+"_error").text("");
			
			if( (item == "")||(item == null) ) {
				$("#"+value.id+"_error").text("Required");
				$("#"+value.id+"_error").css("color", "red");
				submit = false;
			}
		});

		if( (submit)&&($().confirmPasswords(id)) ) {
			$(formId).submit();
		} else {
			return;
		}

	});
	
	
	// Disable features not chosen by radio ----------------------------->
	
	$.fn.toggleOptions = function(element) {
		
		var id = $(element).attr("id");
		var parentId = '#'+(id.split("_"))[1]
		var altParentId = null;
		var sourceValue = $(element).val();
		
		$.each($("input[name='pollSetting']"), function(key, value){
			if(value.value != sourceValue) {
				altParentId = '#'+(value.id.split("_"))[1];
			}
		});
		
		if(altParentId != null) {
			$(parentId).css("color", "black");
			$(altParentId).css("color","#ccc");
			
			$(parentId + " td").css("color", "black");
			$(altParentId + " td").css("color","#ccc");
			
			$(parentId + " select").attr("disabled",false);
			$(altParentId + " select").attr("disabled",true);
			
		}
	}
	
	if($("#scheduleForm").is(':visible')) {
		$().toggleOptions("input[name='pollSetting']:[checked]");
	}	
	
	$("input[name='pollSetting']").change(function(){
		$().toggleOptions($(this));
	});
	
	
});
