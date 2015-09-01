<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page session="false" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<html>
<head>
	<title>nano-google(готов искать)</title>
	<link href="resources/css/wait.css" rel="stylesheet" type="text/css">
	<link href="resources/images/favicon.ico" rel="shortcut icon" >
</head>
<script 	src="resources/js/jquery-2.1.4.min.js"></script> 
<script 	src="resources/js/wait.js"></script> 
 <body>
 
	<div id="centered">
	<div class="logo"></div>
	<div  id="wait" class="think">Дайте подумать</div>
	<div id="cnt" class="thinksmall">&nbsp;</div>
	<input class="button1" value="Прервать" tabindex="1"  type="submit" onclick='BreakCnt();'>
			
 </div>
</body>
</html>
