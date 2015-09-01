<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page session="false" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="f" uri="http://www.springframework.org/tags/form" %>
<html>
<head>
	<title>nano-google(готов искать)</title>
	<link href="resources/css/rootpage.css" rel="stylesheet" type="text/css">
	<link href="resources/images/favicon.ico" rel="shortcut icon" >
</head>
<body>
<div class="top"></div>
<div class="main">
	<div class="logo"><div style="color:#777;font-size:16px;font-weight:bold;position:relative;left:218px;top:55px" >Готов искать!</div></div>
	<div class="search"><div class="line1">Введите фразу для поиска</div>
		<f:form commandName="txtForm" action="search" method="post">
<div class="line2">		<f:input path="searchtext" name="searchtext" type="text" class="text" value="" /> 
</div>
	<div class="line2">		<button class="button1" value="Поиск" aria-label="Поиск в NanoGoogle" name="btnG" type="submit"> 
	<span class="sbico"></span> </button>
</div>
		</f:form>
	</div>
</div>
</body>
</html>
