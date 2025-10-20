<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Amazin - Login</title>
    <link rel="stylesheet" href="css/style.css" />
</head>
<body>
    <header>
        <h1 class="header">Amazin.com</h1>
        <h2 class="centered">
            Welcome to the <em>smallest</em> online shop in the world!!
        </h2>
    </header>
    <nav>
        <ul>
            <c:choose>
                <c:when test="${not empty sessionScope.LOGGED_USER}">
                    <li><a href="index.jsp">Main Menu</a></li>
                </c:when>
                <c:otherwise>
                    <li><a href="#">Start</a></li>
                </c:otherwise>
            </c:choose>
            <li><a href="http://miw.uniovi.es">About</a></li>
            <li><a href="mailto:dd@email.com">Contact</a></li>
        </ul>
    </nav>
    <section>
        <article id="a01">
            <c:choose>
                <c:when test="${not empty sessionScope.LOGGED_USER}">
                    <!-- Usuario ya logueado -->
                    <label class="mytitle">Welcome back!</label><br />
                    <div class="success-message">
                        <p><strong>User:</strong> <c:out value="${sessionScope.LOGGED_USER.username}" /></p>
                        <p><strong>Role:</strong> <c:out value="${sessionScope.LOGGED_USER.role}" /></p>
                        <p>You are now logged in.</p>
                        <br/>
                        <p><a href="index.jsp" class="btn-link">Go to Main Menu</a></p>
                        <p><a href="Controller?action=LogoutAction" class="btn-link-secondary">Logout</a></p>
                    </div>
                </c:when>
                <c:otherwise>
                    <!-- Formulario de login -->
                    <label class="mytitle">Please Log In</label><br />
                    
                    <!-- Mensaje de logout exitoso -->
                    <c:if test="${logoutSuccess}">
                        <div class="success-message">
                            <p><strong>Session closed successfully</strong></p>
                            <p>You have been logged out. Please log in again to continue.</p>
                        </div>
                    </c:if>
                    
                    <!-- Mensaje de error de login -->
                    <c:if test="${loginSuccess == false}">
                        <div class="error-message">
                            <p><strong>Authentication Error</strong></p>
                            <p><c:out value="${errorMessage}" /></p>
                        </div>
                    </c:if>
                    
                    <div class="login-form">
                        <form action="Controller" method="post">
                            <input type="hidden" name="action" value="LoginAction" />
                            
                            <label for="username">Username:</label><br />
                            <input type="text" id="username" name="username" required autofocus placeholder="Enter your username" /><br /><br />
                            
                            <input type="submit" value="Login" class="btn-primary" />
                        </form>
                        
                        <div class="info-box">
                            <h4>Test Users:</h4>
                            <ul>
                                <li><strong>user1</strong> - Role: ADMIN (full access)</li>
                                <li><strong>user2</strong> - Role: USER (limited access)</li>
                            </ul>
                            <p><em>Note: No password required for this simplified version.</em></p>
                        </div>
                    </div>
                </c:otherwise>
            </c:choose>
        </article>
    </section>
    <footer>
        <strong>Master in Web Engineering (miw.uniovi.es).</strong><br />
        <em>University of Oviedo</em>
    </footer>
</body>
</html>

