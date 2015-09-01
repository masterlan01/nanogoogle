<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ page session="false"%>
<%@ page contentType="text/html; charset=UTF-8"%>
<%@taglib prefix="f" uri="http://www.springframework.org/tags/form"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>nano-google(результаты поиск)</title>
<link href="resources/css/search.css" rel="stylesheet" type="text/css">
<link href="resources/images/favicon.ico" rel="shortcut icon" >
</head>
<body>
<div class="document">
	<div class="top">
		<br>
		<div class="logo"></div>
		<div class="search">
			<f:form method="post" commandName="txtForm" action="search">
				<f:input path="searchtext" type="text" class="text" value="" tabindex="1" />
				<button class="button1" value="Поиск"  name="btnG" type="submit"> <span class="sbico"></span> </button>
			</f:form>
				<button class="button2" value="Индекс"  name="btnGN"  onclick='window.location.href =  "/nanogoogle/index" ;' type="submit"> <span class="sbicogn"></span> </button>
</div>
	</div>

	<div class="header">
			<c:choose>
				<c:when test="${order eq 1}">
						<div class="headeritem"> <a href="${searchUri}&page=${currentPage}&order=score">По релевантности</a> </div>
				</c:when>
				<c:otherwise>
						<div class="headeritemon">По релевантности</div>
				</c:otherwise>
			</c:choose>
			<c:choose>
				<c:when test="${order eq 1}">
						<div class="headeritemon">По алфавиту</div>
				</c:when>
				<c:otherwise>
						<div class="headeritem"> <a href="${searchUri}&page=${currentPage}&order=text">По алфавиту</a></div>
				</c:otherwise>
			</c:choose>
	</div>

	<div class="main">

				<c:choose>
						<c:when test="${totalPages eq 0}">
							<div class="mainitem">По запросу <span class="text">"${searchText}"</span> ничего не найдено. </div>
						</c:when>
						<c:otherwise>
							<div class="mainitem">Результаты поиска:   контекст  <span class="text">"${searchText}"</span>  найден в  ${totalDocs} ${totalText}</div>
						</c:otherwise>
					</c:choose>

		<c:forEach var="list" items="${listResult}">
			<div class="mainitem">
				<a href="${list.url}" target="_blank" class="style1">${list.title}</a>
			</div>
			<div class="style2">${list.url}</div>
			<div>${list.content}</div>
		</c:forEach>


		<c:if test="${totalPages gt 1}">
		<hr noshade>
		<table class="nav" >
			<tbody>
				<tr>
					<c:choose>
						<c:when test="${currentPage gt 1}">
							<td class="prev1"><a
								href="${searchUri}&page=${currentPage-1}"><span class="prev"></span>Назад</a></td>
						</c:when>
						<c:otherwise>
							<td class="prev1"><a
								href="${searchUri}&page=${currentPage-1}"><span class="prev"></span>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</a></td>
						</c:otherwise>
					</c:choose>
					<c:forEach var="pg" items="${pages}">
						<c:choose>
							<c:when test="${pg == currentPage}">
								<td><a href="${searchUri}&page=${pg} "><span class="pagecurrent"></span>${pg} </a></td>
							</c:when>
							<c:otherwise>
								<td><a href="${searchUri}&page=${pg} "><span class="page"></span>${pg} </a></td>
							</c:otherwise>
						</c:choose>
					</c:forEach>		
					<c:set var="nextPage" value="${currentPage+1}"/> 
					<c:if test="${currentPage eq  totalPages}">  <c:set var="nextPage" value="${currentPage}"/>   </c:if>		
					<c:choose>
						<c:when test="${currentPage lt  totalPages}">
							<td class="next1"><a
								href="${searchUri}&page=${nextPage}"><span class="next"></span>Вперед</a></td>
						</c:when>
						<c:otherwise>
							<td class="next1"><a href="${searchUri}&page=${nextPage}"><span class="next"></span>&nbsp;</a></td>
						</c:otherwise>
					</c:choose>
					</tr>
			</tbody>
		</table>
	</c:if>
	<hr noshade>
	</div>
	<div class="aux"></div>
	</div>
	<div class="footer"></div>
	</body>
</html>