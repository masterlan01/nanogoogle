<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ page session="false"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="f" uri="http://www.springframework.org/tags/form"%>


<html>
<head>
<title>nano-google(приветствие)</title>
<link href="resources/css/index.css" rel="stylesheet" type="text/css">
<link href="resources/images/favicon.ico" rel="shortcut icon" >
</head>
<body>
<div class="top"></div>
<div class="main">
	<div class="logo"><div style="color:#777;font-size:16px;font-weight:bold;position:relative;left:218px;top:55px" >Готов индексировать!</div></div>
	<div class="search">
	<f:form action="index" method="post" commandName="urlForm" >
	<div class="line1">Укажите URL для индексации  </div>
	<div class="line1"></div>
	<div class="line1"></div>
	<div class="line1"></div>
	<div class="line1">Глубина</div>
	<div class="line2">
		<f:input  path="url"  name="url"  type="text" class="text"/>		
	</div>
	<div class="line2">
		<f:input path="level" name="level" value="1" type="text" class="index" />
	</div>
	<div class="line2">
		<input type="submit" value="Поехали !" class="button1"/>	
	</div>
	
	<div class="line1"><span class="error"><f:errors path="url" /></span><span class="error"><f:errors path="level" cssclass="error">Не коррекно указана глубина сканирования</f:errors></span></div>
	<div><label class="nextlbl"><f:checkbox path="onlysite"   class="gn-checkbox"/> Индексировать только указанный сайт</label>
	<label><f:checkbox path="morfo"   class="gn-checkbox"/> Учитывать морфологию русского языка</label></div>
	</f:form>
	</div>
</div>

</body>
</html>
