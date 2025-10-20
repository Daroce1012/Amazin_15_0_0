<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Amazin - Acceso No Autorizado</title>
    <link rel="stylesheet" href="css/style.css" />
</head>
<body>
    <header>
        <h1 class="header">Amazin.com</h1>
        <h2 class="centered">Access Denied</h2>
    </header>
    <nav>
        <ul>
            <li><a href="index.jsp">Start</a></li>
            <li><a href="http://miw.uniovi.es">About</a></li>
            <li><a href="mailto:dd@email.com">Contact</a></li>
            <li><a href="Controller?action=LogoutAction">Logout</a></li>
        </ul>
    </nav>
    <section>
        <article id="a01">
            <label class="mytitle">Access Denied</label><br />
            <p><c:out value="${errorMessage}" /></p>
            <br/>
            <a href="${linkUrl}"><c:out value="${linkText}" /></a>
        </article>
    </section>
    <footer>
        <strong>Master in Web Engineering (miw.uniovi.es).</strong><br />
        <em>University of Oviedo</em>
    </footer>
</body>
</html>

