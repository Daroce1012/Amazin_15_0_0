<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Amazin</title>
<link rel="stylesheet" href="css/style.css" />
</head>
<body>
	<header>
		<h1 class="header">Amazin.com</h1>
		<h2 class="centered">
			<c:choose>
				<c:when test="${not empty sessionScope.LOGGED_USER}">
					Welcome <strong><c:out value="${sessionScope.LOGGED_USER.username}" /></strong> 
					(<c:out value="${sessionScope.LOGGED_USER.role}" />)
				</c:when>
				<c:otherwise>
					Welcome to the <em>smallest</em> online shop in the world!!
				</c:otherwise>
			</c:choose>
		</h2>
	</header>
	<nav>
		<ul>
			<li><a href="#">Start</a></li>
			<li><a href="http://miw.uniovi.es">About</a></li>
			<li><a href="mailto:dd@email.com">Contact</a></li>
			<li><a href="Controller?action=LogoutAction">Logout</a></li>
		</ul>
	</nav>
	<section>
		<article id="a01">
			<label class="mytitle">Choose an option:</label><br /> 
			<a href="Controller?action=ShowBooksAction">Show Catalog</a><br /> 
			<a href="Controller?action=ShowSpecialOfferAction">Show Special Offers!</a> 
		</article>
	</section>
	<footer>
		<strong> Master in Web Engineering (miw.uniovi.es).</strong><br /> <em>University
			of Oviedo </em>
	</footer>
</body>
</html>

