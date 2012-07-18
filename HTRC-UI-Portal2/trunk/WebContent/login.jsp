<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib prefix="s" uri="/struts-tags" %>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>WELCOME</title>
</head>
<body>
<s:form action = "ListCollectionsAction" method="post">
<fieldset>
<legend> Login </legend>
<s:textfield name="username" label="Username"></s:textfield>
<s:password name="password" label="Password"> </s:password>
<s:submit method="execute" key="label.submit" value="Submit" align="center" />
</fieldset>
</s:form>
</body>
</html>