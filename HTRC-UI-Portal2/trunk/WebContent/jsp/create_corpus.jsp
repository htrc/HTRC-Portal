<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib prefix="s" uri="/struts-tags" %>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>HATHI TRUST RESEARCH CENTER ANALYSIS AND TOOLS</title>
</head>
<body style="height : 100%; width : 100%;" >
	<s:form action="ListAnalysisAction" method="post"  >
		<table width="100%" height="100%" border="1">
			<tr>
				<td>
					<div style="text-align:right">
					<input type="button" onClick="javascript:window.close();" value="-" >
					<input type="button" onClick="javascript:window.maximize();" value="+" >
					<input type="button" onClick="javascript:window.close();" value="X" style="background-color:#c00;">
					</div>
				</td>
			</tr>
			<tr style="height : 75%; width : 100%;">
				<td> <img alt="HATHI TRUST" src="C:\Users\Swati\HATHI\HTRCPortal2\WebContent\images\hathi.jpg" style="height:8%; width: 10%;"> </td>
			</tr>
			<tr style="height : 75%; width : 100%;">
				<td>
					<a href ="">Home</a> &emsp;&emsp; 
					<a href ="">About </a>&emsp;&emsp;  
					<a href ="">Analysis/DoResearch</a>
					&emsp;&emsp; <a href =""> Help</a></td>
	 		</tr>
	 		
	 		<tr>
	 			<th> <br>CREATE A CORPUS</th>
	 		</tr>
	 	</table>
	 	
	 	<table width="100%" height="100%" border="0">
	 		<tr>
	 			<td>
	 				<br>
	 					<fieldset >
	 						<legend><b>Select Specific Collections</b></legend>
	 						<p/> <p/><p><br> &emsp;&emsp;
							<text>Collections</text>	
							<jsp:useBean id="obj" class="org.indiana.edu.List_Collections_Action" scope="page"/>
						    <c:foreach items="${collections}" var="collections" >
    					    <select>
    						<option>${collections[0]}</option>
    						</select>
    						</c:foreach>
							</p> 
						 </fieldset>
	 				<br>
	 			</td>
	 		</tr>
	 	</table>
	 <s:submit method="execute" key="label.submit" value="SUBMIT" align="center" />
	 </s:form>
	 </body>
	 </html>
	
